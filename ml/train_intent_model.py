#!/usr/bin/env python3
"""Train a small, reproducible Multinomial Naive Bayes intent model using only Python's standard library."""

from __future__ import annotations

import argparse
import collections
import json
import math
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TOKEN_RE = re.compile(r"[a-z0-9']+")


def tokenize(text: str) -> list[str]:
    return TOKEN_RE.findall(text.lower())


def read_jsonl(path: Path) -> list[dict[str, str]]:
    rows = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        row = json.loads(line)
        if not isinstance(row.get("text"), str) or not isinstance(row.get("intent"), str):
            raise ValueError(f"{path}:{line_number}: each row needs text and intent strings")
        rows.append(row)
    return rows


def train(rows: list[dict[str, str]]) -> dict:
    classes = sorted({row["intent"] for row in rows})
    vocabulary = sorted({token for row in rows for token in tokenize(row["text"])})
    class_counts = collections.Counter(row["intent"] for row in rows)
    token_counts = {label: collections.Counter() for label in classes}
    token_totals = {label: 0 for label in classes}
    single_token_overrides = {}

    for row in rows:
        tokens = tokenize(row["text"])
        label = row["intent"]
        token_counts[label].update(tokens)
        token_totals[label] += len(tokens)
        if len(tokens) == 1:
            single_token_overrides[tokens[0]] = label

    priors = {
        label: math.log(class_counts[label] / len(rows))
        for label in classes
    }
    likelihoods = {}
    for label in classes:
        denominator = token_totals[label] + len(vocabulary)
        likelihoods[label] = {
            token: math.log((token_counts[label][token] + 1) / denominator)
            for token in vocabulary
        }

    return {
        "model_version": 1,
        "algorithm": "multinomial_naive_bayes",
        "language": "en",
        "classes": classes,
        "vocabulary": vocabulary,
        "log_priors": priors,
        "log_likelihoods": likelihoods,
        "single_token_overrides": single_token_overrides,
        "min_known_tokens": 2,
        "confidence_threshold": 0.42,
        "margin_threshold": 0.06,
    }


def predict(model: dict, text: str) -> tuple[str, float]:
    tokens = tokenize(text)
    overrides = model["single_token_overrides"]
    if len(tokens) == 1 and tokens[0] in overrides:
        return overrides[tokens[0]], 1.0

    vocabulary = set(model["vocabulary"])
    known = [token for token in tokens if token in vocabulary]
    if len(known) < model["min_known_tokens"]:
        return "UNKNOWN", 0.0

    scores = {}
    for label in model["classes"]:
        score = model["log_priors"][label]
        token_probs = model["log_likelihoods"][label]
        score += sum(token_probs[token] for token in known)
        scores[label] = score

    maximum = max(scores.values())
    exponentials = {label: math.exp(score - maximum) for label, score in scores.items()}
    total = sum(exponentials.values())
    ranked = sorted(
        ((label, value / total) for label, value in exponentials.items()),
        key=lambda item: item[1],
        reverse=True,
    )
    best_label, confidence = ranked[0]
    second_confidence = ranked[1][1] if len(ranked) > 1 else 0.0
    if confidence < model["confidence_threshold"]:
        return "UNKNOWN", confidence
    if confidence - second_confidence < model["margin_threshold"]:
        return "UNKNOWN", confidence
    return best_label, confidence


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--train", type=Path, default=ROOT / "ml/data/train.jsonl")
    parser.add_argument("--eval", type=Path, default=ROOT / "ml/data/eval.jsonl")
    parser.add_argument("--output", type=Path, default=ROOT / "app/src/main/assets/intent_model.json")
    args = parser.parse_args()

    train_rows = read_jsonl(args.train)
    eval_rows = read_jsonl(args.eval)
    if not train_rows or not eval_rows:
        raise SystemExit("Training and evaluation datasets must both contain examples.")

    model = train(train_rows)
    correct = 0
    confusion = collections.Counter()
    for row in eval_rows:
        predicted, _ = predict(model, row["text"])
        confusion[(row["intent"], predicted)] += 1
        correct += predicted == row["intent"]

    accuracy = correct / len(eval_rows)
    print(f"Training examples: {len(train_rows)}")
    print(f"Evaluation examples: {len(eval_rows)}")
    print(f"Evaluation accuracy: {accuracy:.1%}")
    for (expected, predicted), count in sorted(confusion.items()):
        print(f"  expected={expected} predicted={predicted}: {count}")

    if accuracy < 0.80:
        raise SystemExit("Evaluation accuracy below 80%; refusing to export the model.")

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(model, separators=(",", ":")), encoding="utf-8")
    print(f"Model written to: {args.output}")


if __name__ == "__main__":
    main()

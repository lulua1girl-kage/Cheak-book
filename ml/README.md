# Local intent model

This folder contains a small English intent-classification experiment for Nori.

- Algorithm: Multinomial Naive Bayes with Laplace smoothing.
- Runtime dependency: Python standard library only.
- Training data: synthetic examples written for this project; no personal chat history is included.
- Evaluation set: held-out synthetic examples, including unknown requests.
- Output: `app/src/main/assets/intent_model.json`.

Run from the repository root:

```bash
python ml/train_intent_model.py
```

The script refuses to export when evaluation accuracy is below 80%. That threshold is only a basic regression gate on this small synthetic test set; it does not establish real-world language understanding or generalization.

The Android app uses this model as a small intent router. Low-confidence or ambiguous input remains UNKNOWN and should prompt clarification. The deterministic fallback remains available if the model asset is absent.

Do not add private conversation exports to this training set. Any future examples based on real interactions require explicit review and removal of identifying or sensitive information.

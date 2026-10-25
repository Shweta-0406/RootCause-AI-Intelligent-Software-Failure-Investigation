# RootCause AI — Small ML Prototype

This folder contains a separate, minimal machine-learning prototype for classifying application log text into broad probable categories.

## Categories
- `DATABASE_OR_DEPENDENCY`
- `SERVICE_CONNECTIVITY`
- `MEMORY_EXHAUSTION`
- `GENERAL_APPLICATION`

## Important limitation
The included 32 examples are illustrative demo data. They are not real production logs and are far too small to establish reliable real-world performance. The reported test score is only a demonstration. The model predicts a category; it does not prove the true root cause. Keep the existing rule-based RootCause AI application unchanged while testing this prototype separately.

## Run on Windows
Open a terminal in this folder and run:

```bat
python -m venv .venv
.venv\Scripts\activate
python -m pip install -r requirements.txt
python train_model.py
python predict.py "ERROR service=payments Database connection timeout"
```

If `python` is not recognized, try `py` in place of `python`.

## How it works
`TfidfVectorizer` converts log text into numeric features. `LogisticRegression` learns associations between those features and the example labels. The fitted pipeline is saved to `rootcause_log_model.joblib`.

## Next steps before integration
1. Replace demo examples with properly labelled, representative logs.
2. Split by application/time or use a separate held-out dataset to reduce leakage.
3. Measure precision, recall, and confusion matrix on real held-out logs.
4. Only then expose predictions through a small Python API and connect it to Spring Boot.

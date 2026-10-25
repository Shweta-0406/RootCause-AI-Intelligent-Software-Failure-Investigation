"""Run a single prediction from the saved RootCause AI prototype."""
from pathlib import Path
import sys
import joblib

MODEL_PATH = Path(__file__).resolve().parent / "rootcause_log_model.joblib"

if not MODEL_PATH.exists():
    print("Model not found. Run: python train_model.py")
    raise SystemExit(1)

message = " ".join(sys.argv[1:]).strip()
if not message:
    message = input("Paste a log message: ").strip()
if not message:
    print("No log message provided.")
    raise SystemExit(1)

model = joblib.load(MODEL_PATH)
probabilities = model.predict_proba([message])[0]
index = probabilities.argmax()
print({
    "category": str(model.classes_[index]),
    "confidence": round(float(probabilities[index]), 3),
    "note": "Demo prediction only; verify against logs and evidence."
})

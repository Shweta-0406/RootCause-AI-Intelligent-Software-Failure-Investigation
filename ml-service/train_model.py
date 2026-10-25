"""
RootCause AI - small ML prototype
Trains a text classifier to predict a probable log category.
This is a demo model using a small illustrative dataset, not production-grade.
"""
from pathlib import Path
import json
import joblib
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report

ROOT = Path(__file__).resolve().parent
DATA_PATH = ROOT / "data" / "sample_logs.json"
MODEL_PATH = ROOT / "rootcause_log_model.joblib"

with DATA_PATH.open("r", encoding="utf-8") as f:
    rows = json.load(f)

texts = [row["message"] for row in rows]
labels = [row["category"] for row in rows]

X_train, X_test, y_train, y_test = train_test_split(
    texts, labels, test_size=0.25, random_state=42, stratify=labels
)

model = Pipeline([
    ("tfidf", TfidfVectorizer(ngram_range=(1, 2), lowercase=True, sublinear_tf=True)),
    ("classifier", LogisticRegression(max_iter=1000, class_weight="balanced"))
])
model.fit(X_train, y_train)

predictions = model.predict(X_test)
print("Prototype evaluation on a tiny illustrative test split")
print("Accuracy:", round(accuracy_score(y_test, predictions), 3))
print(classification_report(y_test, predictions, zero_division=0))

# Refit on all examples for the demo model saved for prediction.
model.fit(texts, labels)
joblib.dump(model, MODEL_PATH)
print(f"\nSaved model to: {MODEL_PATH}")

def explain_prediction(message: str):
    probabilities = model.predict_proba([message])[0]
    best_index = probabilities.argmax()
    return {
        "category": str(model.classes_[best_index]),
        "confidence": round(float(probabilities[best_index]), 3),
        "note": "Demo prediction only. Confidence is not a guarantee of correctness."
    }

print("\nExample predictions:")
for example in [
    "ERROR service=payments Database connection timeout",
    "ERROR Connection refused by target service",
    "FATAL Java heap space OutOfMemoryError",
]:
    print(example, "=>", explain_prediction(example))

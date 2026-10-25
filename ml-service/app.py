
from fastapi import FastAPI, HTTPException, UploadFile, File
from pydantic import BaseModel
from collections import Counter
import joblib
from pathlib import Path

app = FastAPI(title="RootCause AI ML API")

BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "rootcause_log_model.joblib"

model = joblib.load(MODEL_PATH)

MAX_FILE_SIZE = 2 * 1024 * 1024  # 2 MB


class LogRequest(BaseModel):
    message: str


@app.get("/")
def home():
    return {
        "status": "running",
        "service": "RootCause AI ML API"
    }


def predict_message(message: str):
    prediction = model.predict([message])[0]
    confidence = 0.0

    if hasattr(model, "predict_proba"):
        probabilities = model.predict_proba([message])[0]
        confidence = float(max(probabilities))

    return {
        "category": str(prediction),
        "confidence": round(confidence, 3)
    }


@app.post("/predict")
def predict_log(request: LogRequest):
    message = request.message.strip()

    if not message:
        raise HTTPException(
            status_code=400,
            detail="Log message cannot be empty"
        )

    result = predict_message(message)

    return {
        **result,
        "note": "Experimental prediction. Verify against actual log evidence."
    }


@app.post("/predict-file")
async def predict_log_file(file: UploadFile = File(...)):
    filename = (file.filename or "").lower()

    if not filename.endswith((".log", ".txt")):
        raise HTTPException(
            status_code=400,
            detail="Only .log and .txt files are supported"
        )

    content = await file.read(MAX_FILE_SIZE + 1)

    if len(content) > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=413,
            detail="File is too large. Maximum size is 2 MB."
        )

    if not content.strip():
        raise HTTPException(
            status_code=400,
            detail="Uploaded file is empty"
        )

    try:
        text = content.decode("utf-8-sig")
    except UnicodeDecodeError:
        raise HTTPException(
            status_code=400,
            detail="File must use UTF-8 text encoding"
        )

    lines = [
        line.strip()
        for line in text.splitlines()
        if line.strip()
    ]

    if not lines:
        raise HTTPException(
            status_code=400,
            detail="No non-empty log lines found"
        )

    results = []

    try:
        for line_number, line in enumerate(lines, start=1):
            prediction = predict_message(line)

            results.append({
                "lineNumber": line_number,
                "message": line,
                "category": prediction["category"],
                "confidence": prediction["confidence"]
            })
    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Prediction failed. Check the ML model and server logs."
        ) from exc

    category_counts = Counter(
        item["category"] for item in results
    )

    return {
        "filename": file.filename,
        "totalLines": len(results),
        "categoryCounts": dict(category_counts),
        "results": results,
        "note": (
            "Experimental ML predictions. "
            "Verify each result against actual log evidence."
        )
    }

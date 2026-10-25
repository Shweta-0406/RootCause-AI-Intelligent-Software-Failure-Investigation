
import { useState } from "react";
import "./App.css";

const API = "http://localhost:8080/api";

function App() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadMessage, setUploadMessage] = useState("");
  const [mlPrediction, setMlPrediction] = useState(null);
  const [analysisMethod, setAnalysisMethod] = useState("Rule-based");

  async function analyzeLogs() {
    setLoading(true);
    setError("");
    setUploadMessage("");

    try {
      const response = await fetch(`${API}/analyze`);

      if (!response.ok) {
        throw new Error("Unable to fetch analysis results.");
      }

      const data = await response.json();
      setSummary(data);
      setAnalysisMethod("Rule-based");
    } catch (err) {
      setError(
        err.message ||
          "Backend connection failed. Check that Spring Boot is running."
      );
    } finally {
      setLoading(false);
    }
  }

  async function analyzeWithML() {
    setLoading(true);
    setError("");
    setUploadMessage("");
    setMlPrediction(null);

    try {
      const response = await fetch(`${API}/ml-test`);
      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || data.detail || "ML prediction failed."
        );
      }

      setMlPrediction(data);
      setAnalysisMethod("ML-based (Experimental)");
    } catch (err) {
      setError(
        err.message ||
          "ML connection failed. Check Spring Boot and Python API."
      );
    } finally {
      setLoading(false);
    }
  }

  function validateSelectedFile() {
    if (!selectedFile) {
      setError("Please select a .log or .txt file first.");
      return false;
    }

    const name = selectedFile.name.toLowerCase();

    if (!name.endsWith(".log") && !name.endsWith(".txt")) {
      setError("Please select only a .log or .txt file.");
      return false;
    }

    if (selectedFile.size === 0) {
      setError("The selected file is empty.");
      return false;
    }

    if (selectedFile.size > 2 * 1024 * 1024) {
      setError("Maximum file size is 2 MB.");
      return false;
    }

    return true;
  }

  async function uploadLogFile() {
    if (!validateSelectedFile()) return;

    setLoading(true);
    setError("");
    setUploadMessage("");

    try {
      const formData = new FormData();
      formData.append("file", selectedFile);

      const response = await fetch(`${API}/logs/upload`, {
        method: "POST",
        body: formData,
      });

      const message = await response.text();

      if (!response.ok) {
        throw new Error(message || "File upload failed.");
      }

      setUploadMessage(message);

      const analysisResponse = await fetch(`${API}/analyze`);

      if (!analysisResponse.ok) {
        throw new Error(
          "File uploaded, but dashboard results could not refresh."
        );
      }

      const data = await analysisResponse.json();
      setSummary(data);
      setMlPrediction(null);
      setAnalysisMethod("Rule-based");
    } catch (err) {
      setError(err.message || "File upload failed.");
    } finally {
      setLoading(false);
    }
  }

  async function analyzeUploadedFileWithML() {
    if (!validateSelectedFile()) return;

    setLoading(true);
    setError("");
    setUploadMessage("");
    setMlPrediction(null);

    try {
      const formData = new FormData();
      formData.append("file", selectedFile);

      const response = await fetch(`${API}/ml-test/upload`, {
        method: "POST",
        body: formData,
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message ||
            data.detail ||
            "ML file analysis failed."
        );
      }

      setMlPrediction(data);
      setAnalysisMethod("ML-based (Experimental)");
      setUploadMessage(
        `ML analysis completed for ${
          data.filename || selectedFile.name
        }.`
      );
    } catch (err) {
      setError(
        err.message ||
          "ML analysis failed. Check Spring Boot and Python API."
      );
    } finally {
      setLoading(false);
    }
  }

  function handleFileChange(event) {
    const file = event.target.files?.[0] || null;

    setSelectedFile(file);
    setError("");
    setUploadMessage("");
    setMlPrediction(null);
    setAnalysisMethod("Rule-based");
  }

  function clearSelectedFile() {
    setSelectedFile(null);
    setMlPrediction(null);
    setUploadMessage("");
    setError("");
    setAnalysisMethod("Rule-based");

    const input = document.getElementById("logFile");
    if (input) input.value = "";
  }

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <div className="brand-icon">RC</div>
          <div>
            <h2>RootCause AI</h2>
            <p>Explainable Software Failure Investigator</p>
          </div>
        </div>

        <span className="status">
          <span className="status-dot"></span>
          Analysis Dashboard
        </span>
      </header>

      <main className="container">
        <section className="hero">
          <span className="eyebrow">LOG INTELLIGENCE</span>

          <h1>
            Understand failures.
            <br />
            Find probable causes.
          </h1>

          <p>
            Analyze application logs, review evidence, and get
            actionable recommendations to investigate failures.
          </p>

          <button onClick={analyzeLogs} disabled={loading}>
            {loading ? "Please wait..." : "Analyze Logs"}
          </button>

          <div className="upload-area">
            <label htmlFor="logFile">
              Upload Log File (.log or .txt)
            </label>

            <input
              id="logFile"
              type="file"
              accept=".log,.txt,text/plain"
              onChange={handleFileChange}
              disabled={loading}
            />

            {selectedFile && (
              <p>
                Selected file: <strong>{selectedFile.name}</strong>{" "}
                ({(selectedFile.size / 1024).toFixed(1)} KB)
              </p>
            )}

            <div className="upload-actions">
              <button
                type="button"
                onClick={uploadLogFile}
                disabled={loading || !selectedFile}
              >
                {loading ? "Processing..." : "Upload to Dashboard"}
              </button>

              <button
                type="button"
                onClick={analyzeUploadedFileWithML}
                disabled={loading || !selectedFile}
              >
                {loading ? "Processing..." : "Run ML on Uploaded File"}
              </button>

              {selectedFile && (
                <button
                  type="button"
                  onClick={clearSelectedFile}
                  disabled={loading}
                >
                  Clear File
                </button>
              )}
            </div>
          </div>

          {uploadMessage && (
            <p className="success-message">{uploadMessage}</p>
          )}

          <p className="note">
            ML predictions are experimental. Verify results against
            the original log evidence.
          </p>

          <div className="ml-test-area">
            <h2>Machine Learning Analysis</h2>
            <p>
              Test the ML model using a default message or upload
              a log file for line-by-line predictions.
            </p>

            <button
              type="button"
              onClick={analyzeWithML}
              disabled={loading}
            >
              {loading ? "Processing..." : "Run Test Prediction"}
            </button>

            {mlPrediction && (
              <div className="ml-result">
                <h3>ML Prediction Results</h3>

                {Array.isArray(mlPrediction.results) ? (
                  <>
                    <p>
                      <strong>File:</strong>{" "}
                      {mlPrediction.filename || selectedFile?.name || "N/A"}
                    </p>

                    <p>
                      <strong>Total Lines:</strong>{" "}
                      {mlPrediction.totalLines ?? mlPrediction.results.length}
                    </p>

                    <h4>Predicted Category Counts</h4>

                    {mlPrediction.categoryCounts &&
                      Object.entries(mlPrediction.categoryCounts).map(
                        ([category, count]) => (
                          <p key={category}>
                            <strong>{category}:</strong> {count}
                          </p>
                        )
                      )}

                    <h4>Line-by-Line Results</h4>

                    {mlPrediction.results.map((item, index) => (
                      <article
                        className="log-card"
                        key={`${item.lineNumber ?? index}-${index}`}
                      >
                        <div className="log-header">
                          <div>
                            <span className="log-id">
                              LINE #{item.lineNumber ?? index + 1}
                            </span>
                            <h3>{item.category || "Unknown category"}</h3>
                          </div>

                          <span className="severity">
                            {typeof item.confidence === "number"
                              ? `${(item.confidence * 100).toFixed(1)}%`
                              : "N/A"}
                          </span>
                        </div>

                        <div className="evidence">
                          <span>LOG EVIDENCE</span>
                          <p>{item.message || "No message available"}</p>
                        </div>

                        <p className="note">
                          Prediction confidence is an estimate, not
                          a guarantee of correctness.
                        </p>
                      </article>
                    ))}
                  </>
                ) : (
                  <>
                    <p>
                      <strong>Predicted Category:</strong>{" "}
                      {mlPrediction.category || "Not available"}
                    </p>

                    <p>
                      <strong>Confidence:</strong>{" "}
                      {typeof mlPrediction.confidence === "number"
                        ? `${(mlPrediction.confidence * 100).toFixed(1)}%`
                        : "Not available"}
                    </p>

                    <p>
                      <strong>Note:</strong>{" "}
                      {mlPrediction.note ||
                        "Experimental prediction; verify against log evidence."}
                    </p>
                  </>
                )}
              </div>
            )}
          </div>
        </section>

        {error && <div className="error">{error}</div>}

        <section className="stats">
          <div className="stat-card">
            <span>Total Logs</span>
            <strong>{summary ? summary.totalLogs : "—"}</strong>
          </div>

          <div className="stat-card">
            <span>Error Logs</span>
            <strong className="error-number">
              {summary ? summary.errorCount : "—"}
            </strong>
          </div>

          <div className="stat-card">
            <span>Analysis Method</span>
            <strong className="method">{analysisMethod}</strong>
          </div>
        </section>

        <section className="results-section">
          <div className="section-heading">
            <div>
              <h2>Log Analysis Results</h2>
              <p>
                Evidence, probable root causes, and recommended actions
              </p>
            </div>
          </div>

          {!summary && !error && (
            <div className="empty-state">
              <div className="empty-icon">⌕</div>
              <h3>Ready to investigate</h3>
              <p>
                Click “Analyze Logs” to load results from your backend.
              </p>
            </div>
          )}

          {summary?.results?.map((log) => (
            <article className="log-card" key={log.logId}>
              <div className="log-header">
                <div>
                  <span className="log-id">LOG #{log.logId}</span>
                  <h3>{log.serviceName || "Unknown service"}</h3>
                </div>

                <span className="severity">
                  {log.severity || "UNKNOWN"}
                </span>
              </div>

              <div className="log-meta">
                <span>Time: {log.timestamp || "Not available"}</span>
                <span>Trace: {log.traceId || "Not available"}</span>
              </div>

              <div className="evidence">
                <span>LOG EVIDENCE</span>
                <p>{log.evidence || "No evidence provided"}</p>
              </div>

              <div className="analysis-grid">
                <div>
                  <span className="field-label">
                    PROBABLE ROOT CAUSE
                  </span>
                  <p>{log.rootCause || "Not available"}</p>
                </div>

                <div>
                  <span className="field-label">
                    RECOMMENDED ACTION
                  </span>
                  <p>{log.recommendation || "Not available"}</p>
                </div>
              </div>

              <div className="confidence">
                Confidence: {log.confidence ?? "Not available"}
              </div>
            </article>
          ))}
        </section>
      </main>

      <footer>
        RootCause AI · Explainable, evidence-based investigation
      </footer>
    </div>
  );
}

export default App;
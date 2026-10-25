# RootCause AI – Intelligent Software Failure Investigation

## About the Project

RootCause AI is an intelligent software failure investigation system designed to help developers identify potential causes of application failures. It combines rule-based log analysis with Machine Learning predictions to support troubleshooting and investigation.

## Key Features

- Upload application log files.
- Store and analyze application logs using a MySQL database.
- Identify error severity and potential root causes.
- Generate Machine Learning predictions for uploaded log files.
- Display predicted categories and confidence scores.
- Compare rule-based analysis with Machine Learning predictions.
- Provide suggested actions to help investigate application issues.

## Technology Stack

### Frontend
- React
- Vite
- JavaScript
- HTML and CSS

### Backend
- Java
- Spring Boot
- REST APIs

### Machine Learning Service
- Python
- FastAPI
- Scikit-learn
- TF-IDF and Logistic Regression

### Database
- MySQL

## Project Architecture

**Main Application Flow**

React Frontend → Spring Boot REST API → MySQL Database

**Machine Learning Flow**

Spring Boot Backend → Python FastAPI → Machine Learning Model

## Machine Learning Categories

- Database or Dependency Issues
- Service Connectivity Issues
- Memory Exhaustion
- General Application Issues

## How to Run the Project

### 1. Start the Spring Boot Backend

Open a terminal in the Spring Boot project root directory and run:

```bash
.\mvnw.cmd spring-boot:run
```

### 2. Start the React Frontend

Open a new terminal and run:

```bash
cd frontend
npm install
npm run dev
```

### 3. Start the Python Machine Learning Service

Open a separate terminal in the Python ML project directory. Activate the virtual environment if needed, then run:

```bash
py -m uvicorn app:app --host 127.0.0.1 --port 8000
```

Keep all three services running while testing the application.

## Important Notes

- Machine Learning predictions are experimental and should be verified against actual log evidence.
- Confidence scores are model estimates, not guarantees of correctness.
- The model was trained on a small illustrative dataset and requires more diverse data and validation before production use.

## Future Enhancements

- Improve model performance using a larger, diverse real-world log dataset.
- Add more software failure categories.
- Improve root-cause explanations.
- Add authentication and role-based access.
- Deploy the application to a cloud platform.

## Author

**Shweta Battas Gidde**

B.Tech – Computer Science (Data Science)

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from pydantic import BaseModel
import pandas as pd
import joblib
from fastapi.middleware.cors import CORSMiddleware
import json
import spacy

app = FastAPI()


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins for testing; restrict in productionpython
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

nlp = spacy.load("en_core_web_sm")

class ConnectionManager:
    def __init__(self):
        self.active_connections: dict[int, WebSocket] = {}  # Use a dictionary
        self.current_id = 0  # Auto-incrementable ID counter

    def get_next_id(self) -> int:
        """Generate the next conversation ID."""
        self.current_id += 1
        return self.current_id

    async def connect(self, websocket: WebSocket) -> int:
        """Accept a WebSocket connection and assign an ID."""
        await websocket.accept()
        conversation_id = self.get_next_id()
        self.active_connections[conversation_id] = websocket  # Use the dictionary
        return conversation_id

    def disconnect(self, conversation_id: int):
        """Remove a WebSocket connection."""
        if conversation_id in self.active_connections:
            del self.active_connections[conversation_id]

    async def send_message(self, message: str, conversation_id: int):
        """Send a message to a specific WebSocket."""
        connection = self.active_connections.get(conversation_id)
        if connection:
            await connection.send_text(message)

manager = ConnectionManager()

# Load data files
try:
    training_data = pd.read_csv("Data/training.csv")
    symptom_description = pd.read_csv("Data/symptom_description.csv", header=None)
    symptom_precaution = pd.read_csv("Data/symptom_precaution.csv", header=None)
    symptom_severity = pd.read_csv("Data/symptom_severity.csv", header=None)
    complications_data = pd.read_csv("Data/disease_complications.csv")

    # Rename columns for symptom_description.csv
    symptom_description.columns = ["Disease", "Description"]

    # Assign column names to symptom_precaution.csv
    num_precaution_columns = symptom_precaution.shape[1] - 1
    precaution_columns = ["Disease"] + [f"Precaution_{i}" for i in range(1, num_precaution_columns + 1)]
    symptom_precaution.columns = precaution_columns

    # Assign column names to symptom_severity.csv
    symptom_severity.columns = ["Symptom", "Severity"]

except Exception as e:
    raise ValueError(f"Error loading CSV files: {e}")

# def train_model():
#     """
#     Train a RandomForestClassifier on the training data.
#     """
#     try:
#         X = training_data.iloc[:, :-1]  # Symptoms
#         y = training_data.iloc[:, -1]  # Diagnoses

#         # Split the data into training and testing sets (optional for validation)
#         X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

#         # Train the RandomForest model
#         model = RandomForestClassifier(random_state=42)
#         model.fit(X_train, y_train)

#         # Print performance metrics (optional)
#         print("Model trained successfully!")
#         print(classification_report(y_test, model.predict(X_test)))

#         return model

#     except Exception as e:
#         raise ValueError(f"Error training model: {e}")

# model = train_model()
# Load the trained model
try:
    model = joblib.load("disease_prediction_model.pkl")
except FileNotFoundError:
    raise FileNotFoundError("Trained model not found. Train the model first.")

# User input schema
class UserInput(BaseModel):
    user_id: str
    message: str

# Store user sessions
user_sessions = {}

def interpret_message(message: str) -> str:
    """
    Interpret user input to classify it as 'yes', 'no', or 'unknown'.
    """
    # Normalize the input to lowercase for consistent comparison
    message = message.lower()

    # Multi-word affirmative and negative phrases
    affirmative_phrases = [
        "of course", "yeah sure", "yes please", "definitely yes", "sure thing"
    ]
    negative_phrases = [
        "not at all", "no way", "definitely not", "absolutely not", "never ever"
    ]

    # Exact match for multi-word phrases
    if any(phrase in message for phrase in affirmative_phrases):
        return "yes"
    if any(phrase in message for phrase in negative_phrases):
        return "no"

    # Tokenize the message for single-word phrases
    doc = nlp(message)
    affirmative = {
        "yes", "yeah", "yep", "sure", "absolutely", 
        "ok", "okay", "alright", "fine", "definitely"
    }
    negative = {
        "no", "nah", "nope", "never", "negative", "not really"
    }

    # Check for single-word matches
    for token in doc:
        if token.text in affirmative:
            return "yes"
        if token.text in negative:
            return "no"

    # Fallback: Use sentence structure to infer intent (optional)
    if "not" in message or "don't" in message:
        return "no"

    return "unknown"


def get_complications(disease: str) -> list:
    """
    Retrieve the complications associated with a specific disease.
    """
    complications_row = complications_data[complications_data["Disease"] == disease]
    if not complications_row.empty:
        complications = complications_row.iloc[0, 1:].dropna().tolist()  # Get all non-null complications
        return complications
    return ["No known complications."]

@app.get("/symptoms")
def get_symptoms():
    """
    Returns a list of all available symptoms for the dropdown.
    """
    try:
        symptoms = list(training_data.columns[:-1])  # All symptoms from training data
        return {"symptoms": symptoms}
    except Exception as e:
        return {"error": str(e)}, 500


@app.websocket("/ws/chat")
async def websocket_endpoint(websocket: WebSocket):
    conversation_id = await manager.connect(websocket)
    try:
        await manager.send_message(f"Connected! Your conversation ID is {conversation_id}.", conversation_id)

        while True:
            try:
                # Receive symptoms
                message = await websocket.receive_text()
                symptoms = [s.strip() for s in message.split(",")]

                # Predict the disease
                input_data = [1 if symptom in symptoms else 0 for symptom in training_data.columns[:-1]]
                predicted_prognosis = model.predict([input_data])[0]

                # Get additional details
                description_row = symptom_description[symptom_description["Disease"] == predicted_prognosis]
                description = description_row["Description"].iloc[0] if not description_row.empty else "No description available."

                precaution_row = symptom_precaution[symptom_precaution["Disease"] == predicted_prognosis]
                precautions = precaution_row.iloc[0, 1:].dropna().tolist() if not precaution_row.empty else ["No specific precautions available."]

                severity_values = symptom_severity[symptom_severity["Symptom"].isin(symptoms)]["Severity"]
                severity = severity_values.mean() if not severity_values.empty else "Unknown"

                # Fetch complications
                complications = get_complications(predicted_prognosis)

                # Construct the response
                response = {
                    "conversation_id": conversation_id,
                    "prognosis": predicted_prognosis,
                    "description": description,
                    "precautions": precautions,
                    "complications": complications,
                    "severity": f"{severity:.2f}" if isinstance(severity, float) else severity,
                }

                # Send the response
                await manager.send_message(json.dumps(response), conversation_id)

            except Exception as e:
                await manager.send_message(f"Error processing message: {e}", conversation_id)

    except WebSocketDisconnect:
        manager.disconnect(conversation_id)
        print(f"Connection {conversation_id} closed.")



@app.get("/start")
def start_chat():
    """
    Respond with the default message.
    """
    default_message = "Hi, I am your medical assistant. Can I help you with a diagnostic?"
    return {"message": default_message}


@app.post("/response")
def chatbot_response(user_input: UserInput):
    """
    Handle user input and guide the chatbot flow using NLP.
    """
    user_id = user_input.user_id
    message = user_input.message.strip()

    # Use NLP to interpret the message
    interpreted_message = interpret_message(message)

    if interpreted_message == "unknown":
        return {"message": "Sorry, I didn't understand. Please respond with 'yes' or 'no'."}

    if interpreted_message == "no":
        return {"message": "Ok, have a nice day!"}

    if interpreted_message == "yes":
        return {
            "message": "Please select symptoms from the list below and provide them comma-separated.",
            "symptoms": list(training_data.columns[:-1])  # List all symptoms
        }

@app.post("/predict")
def predict_disease(user_input: UserInput):
    """
    Predict disease based on symptoms provided by the user.
    """
    try:
        user_id = user_input.user_id
        symptoms = [s.strip() for s in user_input.message.split(",")]

        # Prepare the input vector for the model
        input_data = [1 if symptom in symptoms else 0 for symptom in training_data.columns[:-1]]

        # Predict prognosis
        predicted_prognosis = model.predict([input_data])[0]

        # Fetch additional information for the predicted prognosis
        description_row = symptom_description[symptom_description["Disease"] == predicted_prognosis]
        description = description_row["Description"].iloc[0] if not description_row.empty else "No description available."

        precaution_row = symptom_precaution[symptom_precaution["Disease"] == predicted_prognosis]
        precautions = precaution_row.iloc[0, 1:].dropna().tolist() if not precaution_row.empty else ["No specific precautions available."]

        severity_values = symptom_severity[symptom_severity["Symptom"].isin(symptoms)]["Severity"]
        severity = severity_values.mean() if not severity_values.empty else "Unknown"

        # Determine urgency and recommendation based on severity
        urgency = "Urgent" if severity and float(severity) > 3 else "General Consultation"
        specialist = "Emergency Specialist" if urgency == "Urgent" else "General Practitioner"

        # Response
        response = {
            "prognosis": predicted_prognosis,
            "description": description,
            "precautions": precautions,
            "severity": f"{severity:.2f}" if isinstance(severity, float) else severity,
            "recommendations": {
                "urgency": urgency,
                "specialist": specialist,
            }
        }

        return response

    except Exception as e:
        return {"error": str(e)}


# 🎨 Credit-Token-Based AI Comic Generator System

This is a full-stack application that allows users to generate AI images and comic stories using credits (tokens). Users authenticate via Google, generate content via Replicate (AI), store history in MongoDB, and recharge tokens via Razorpay.

---

## 🚀 Features

- 🔐 Google OAuth login
- 🎨 AI image generation via Replicate API
- 🧠 Token system (start: 1500 tokens)
- 💳 Razorpay recharge (₹50 = +100, ₹100 = +200)
- 🗂️ Persistent image history via MongoDB
- ☁️ Cloudinary storage for generated images
- 📘 Story generation module (coming soon)

---

## 🧠 Tech Stack

| Layer      | Tech                                |
|------------|-------------------------------------|
| Frontend   | React.js (Vite), Tailwind           |
| Backend    | Spring Boot, MongoDB, Cloudinary    |
| AI Server  | Flask + Replicate                   |
| Auth       | Google OAuth 2.0                    |
| Payments   | Razorpay                            |
| Storage    | Cloudinary                          |

---

## 🔐 Environment Variables (Required)

These are **not pushed to GitHub**. You must configure them locally:

### `backend/src/main/resources/application.properties`
```properties
spring.config.import=optional:file:.env.properties

# ======================
# Cloudinary Configuration
# ======================
cloudinary.cloud_name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api_key=${CLOUDINARY_API_KEY}
cloudinary.api_secret=${CLOUDINARY_API_SECRET}
# MongoDB Connection
# ======================
spring.data.mongodb.uri=${MONGODB_URI}
# ======================
# Google OAuth2 Settings
# ======================
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
spring.security.oauth2.client.registration.google.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.google.client-name=Google
# ======================
# Flask AI Service URL
# ======================
flask.server.url=${FLASK_SERVER_URL}
# ======================

# ======================
# Server & Logging Configuration
# ======================
server.port=8080
logging.level.org.springframework=INFO
logging.level.com.example=DEBUG
# ======================
# OpenAI API Key
# ======================
openai.api.key=${OPENAI_API_KEY}





###`backend/.env.properties`

CLOUDINARY_CLOUD_NAME=<your-cloudinary-name>
CLOUDINARY_API_KEY=<your-cloudinary-api-key>
CLOUDINARY_API_SECRET=<your-cloudinary-api-secret>
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.eni3pfk.mongodb.net/credit-token-system?retryWrites=true&w=majority&appName=Cluster0
FLASK_SERVER_URL=http://localhost:5000
OPENAI_API_KEY=your-openai-key
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
REPLICATE_API_TOKEN=your-replicate-token




###`ai-server/ai_service.py`

from flask import Flask, request, jsonify
import requests
import cloudinary
import cloudinary.uploader
import time
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# ─── Cloudinary configuration ─────────────────────────────────────────────────
cloudinary.config(
    cloud_name="dipmroldi",
    api_key="676187458442731",
    api_secret="fteHQCSM1PkSPLDQso4sauwWpEg",
    secure=True
)

# ─── Replicate configuration ──────────────────────────────────────────────────
REPLICATE_API_TOKEN = os.environ.get("REPLICATE_API_TOKEN")
REPLICATE_MODEL_VERSION = "7762fd07cf82c948538e41f63f77d685e02b063e37e496e96eefd46c929f9bdc"

SPRING_HISTORY_SAVE_URL = "http://localhost:8080/api/images/history/save"

@app.route('/generate-image', methods=['POST'])
def generate_image():
    data    = request.get_json() or {}
    prompt  = data.get("prompt")
    user_id = data.get("userId")

    if not prompt or not user_id:
        return jsonify({"error": "Missing prompt or userId"}), 400

    # 1) Ask Replicate to generate 4 images
    headers = {
        "Authorization": f"Token {REPLICATE_API_TOKEN}",
        "Content-Type": "application/json"
    }
    payload = {
        "version": REPLICATE_MODEL_VERSION,
        "input": {"prompt": prompt, "num_outputs": 4}
    }
    r = requests.post(
        "https://api.replicate.com/v1/predictions",
        json=payload, headers=headers, timeout=30
    )
    if r.status_code != 201:
        return jsonify({"error": "Replicate API error", "details": r.json()}), 500

    pred = r.json()
    pred_id = pred["id"]

    # 2) Poll until done
    status = pred.get("status")
    while status not in ("succeeded","failed"):
        time.sleep(2)
        poll = requests.get(f"https://api.replicate.com/v1/predictions/{pred_id}", headers=headers)
        pred = poll.json()
        status = pred.get("status")

    if status != "succeeded":
        return jsonify({"error":"Generation failed","details":pred}), 500

    outputs = pred.get("output") or []
    if not outputs:
        return jsonify({"error":"No outputs"}), 500

    # 3) Upload each to Cloudinary
    uploaded = []
    timestamp = int(time.time() * 1000)  # ms since epoch
    for url in outputs:
        result = cloudinary.uploader.upload(url, folder="generated/")
        uploaded.append({
            "imageUrl": result["secure_url"],
            "timestamp": timestamp,
            "userId": user_id
        })

    # 4) Notify Spring Boot to save history
    try:
        requests.post(
            SPRING_HISTORY_SAVE_URL,
            json={
                "googleId": user_id,
                "images": [u["imageUrl"] for u in uploaded]
            },
            timeout=5
        )
    except Exception as e:
        # Log but don’t fail the whole request
        print(f"⚠️ Could not save history to Spring Boot: {e}")

    # 5) Return results
    return jsonify({"images": uploaded}), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)







How to Run the Project Locally
1. AI Server (Flask)

cd ai-server
pip install -r requirements.txt
python ai_service.py

Serves image generation API using Replicate.

2. Backend (Spring Boot)
cd backend
mvn clean install
mvn spring-boot:run

Runs on http://localhost:8080

3. Frontend (React + Vite)
cd frontend
npm install
npm run dev
Runs on http://localhost:5173



Notes for Developers / Managers
All secret configs have been removed from Git and .gitignore is in place.

Secure files:
backend/.env.properties (Replicate, OpenAI)
application.properties (MongoDB, Cloudinary, Google)

Project is modular: you can extend it with "Character Creation", "Story Generator", "My Orders" easily.



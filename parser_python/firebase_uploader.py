import os
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import time
from dotenv import load_dotenv

load_dotenv()

# Инициализация Firebase Admin SDK
cred_path = os.getenv("FIREBASE_CREDENTIALS_PATH", "firebase-key.json")

try:
    cred = credentials.Certificate(cred_path)
    firebase_admin.initialize_app(cred)
    db = firestore.client()
    print("✅ Firebase initialized successfully.")
except Exception as e:
    print(f"❌ Failed to initialize Firebase: {e}")
    print("⚠️ Please ensure firebase-key.json (Service Account Key) is in the project root.")
    db = None

async def upload_to_moderation_queue(parsed_data: dict) -> bool:
    if db is None:
        print("❌ Cannot upload: Firebase not initialized.")
        return False
        
    try:
        # Валидация
        if parsed_data.get("carBrand") == "REJECT":
            print("⏭ Скипаем пост: ИИ посчитал его мусорным.")
            return False
            
        doc_ref = db.collection("community_solutions").document()
        
        document_data = {
            "carBrand": parsed_data.get("carBrand", ""),
            "carModel": parsed_data.get("carModel", ""),
            "carYear": parsed_data.get("carYear", ""),
            "system": parsed_data.get("system", ""),
            "symptoms": parsed_data.get("symptoms", ""),
            "errorCodes": parsed_data.get("errorCodes", ""),
            "solution": parsed_data.get("solution", ""),
            "source": "auto_parsed",
            "status": "pending",
            "authorUsername": "AutoParserBot",
            "authorEmail": "bot@avtoelektrika.local",
            "createdAt": int(time.time() * 1000),
            "likes": 0,
            "dislikes": 0,
            "successCount": 0,
            "isFromCommunity": True
        }
        
        doc_ref.set(document_data)
        print(f"✅ Успешно загружено в премодерацию: {document_data['carBrand']} {document_data['carModel']} ({document_data['system']})")
        return True
    except Exception as e:
        print(f"❌ Ошибка при загрузке в Firebase: {e}")
        return False

import os
import json
import re
from g4f.client import AsyncClient
from dotenv import load_dotenv

load_dotenv()

DELLMAR_API_URL = "https://api.dellmar.xyz/v1/chat/completions"
DELLMAR_API_KEY = os.getenv("DELLMAR_API_KEY", "sk-cvc-61b91d89d5d102529c0243449bad1c16887a9bfd10f9b05f5e34bc3ff5fc171c")

# Учитывая, что это ИИ-каталогизатор, даем ему список доступных систем
AUTO_SYSTEMS = [
    "Электропроводка", "Двигатель", "Генератор", "Стартер", "Аккумуляторная батарея",
    "Освещение и световая сигнализация", "Звуковая сигнализация", "Кондиционер и отопление",
    "Приборная панель", "Мультимедиа", "Очистители и омыватели", "Стеклоподъемники",
    "Центральный замок", "Подогрев сидений и зеркал", "Датчики и реле",
    "Система зажигания", "Система впрыска", "Система охлаждения", "ABS/ESP/Тормоза",
    "Подушки безопасности (SRS)", "Иммобилайзер/Сигнализация", "Круиз-контроль",
    "Парктроники/Камеры", "Электроусилитель руля (ЭУР)", "Блоки управления (ЭБУ/ECU/BCM)"
]

SYSTEM_PROMPT = f"""
Ты — профессиональный автоэлектрик и строгий каталогизатор данных. 
Я дам тебе сырой текст проблемы и ее решения с автомобильного форума.
Твоя задача — извлечь точные параметры для каталога и отформатировать решение.

СТРОГИЕ ПРАВИЛА:
1. "carBrand" — марка авто на английском (например: Toyota, Kia, Lada, Mercedes-Benz).
2. "carModel" — модель авто (например: Camry, Rio, Granta).
3. "carYear" — год выпуска или поколение, если указано (например: 2015, 2010-2014, "Не указано").
4. "system" — СТРОГО ОДНА ИЗ ЭТОГО СПИСКА: {", ".join(AUTO_SYSTEMS)}. Подбери наиболее подходящую.
5. "symptoms" — кратко, на что жаловался автор (1-2 предложения).
6. "errorCodes" — коды ошибок, если есть (например: P0335, P0171). Если нет, оставь пустым "".
7. "solution" — структурированный JSON-текст с решением. Он должен быть строкой, содержащей экранированный JSON в формате:
   {{\"solutions\": [{{\"title\": \"Название решения\", \"description\": \"Краткое описание\", \"steps\": [\"Шаг 1\", \"Шаг 2\"]}}]}}

ЕСЛИ в тексте нет решения (проблема не решена) или это откровенный мусор (не про автоэлектрику), верни в "carBrand" слово "REJECT".

ВЕРНИ ТОЛЬКО ВАЛИДНЫЙ JSON БЕЗ МАРКДАУНА (БЕЗ ```json и ```):
{{
  "carBrand": "...",
  "carModel": "...",
  "carYear": "...",
  "system": "...",
  "symptoms": "...",
  "errorCodes": "...",
  "solution": "..."
}}
"""

client = AsyncClient()

async def process_forum_text(raw_text: str) -> dict:
    try:
        response = await client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": f"Текст с форума:\n{raw_text}"}
            ]
        )
        
        content = response.choices[0].message.content.strip()
        print(f"🤖 AI Answer: {content}")
        
        # Очищаем от возможных markdown блоков (```json ... ```)
        content = re.sub(r'```json\s*', '', content)
        content = re.sub(r'```\s*', '', content)
        
        # Парсим JSON
        parsed_data = json.loads(content)
        return parsed_data
        
    except Exception as e:
        print(f"❌ AI Error (g4f): {e}")
        return None

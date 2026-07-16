import asyncio
import sys
from database import init_db
from drive2_spider import Drive2Spider

async def main():
    print("🚗 Автоэлектрика - Автономный Spider (Drive2 Edition)")
    print("======================================================")
    
    # 1. Инициализация локальной базы данных SQLite
    init_db()
    
    # Если запуск через .bat файл без аргументов, паук пойдет по этим крупным хабам
    start_urls = [
        "https://www.drive2.ru/communities/785/",   # Сообщество "Автоэлектрики"
        "https://www.drive2.ru/communities/531/",   # "Ремонт" (общая, но ИИ отсеет не электрику)
        "https://www.drive2.ru/experience/"         # Общий раздел "Опыт эксплуатации"
    ]
    
    if len(sys.argv) > 1:
        start_urls = sys.argv[1:]
        
    print(f"🕸️ Запускаем паука Drive2. Стартовые точки: {len(start_urls)}")
    spider = Drive2Spider(base_urls=start_urls, max_concurrent_tasks=3)
    await spider.start()

if __name__ == "__main__":
    asyncio.run(main())

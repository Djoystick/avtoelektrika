import asyncio
from typing import Set, List
from playwright.async_api import async_playwright, Page
from database import is_url_visited, mark_url_visited
from parser import fetch_html, extract_text_from_html
from ai_processor import process_forum_text
from firebase_uploader import upload_to_moderation_queue

class SpiderCore:
    def __init__(self, base_urls: List[str], max_concurrent_tasks: int = 5):
        self.queue = asyncio.Queue()
        self.max_concurrent_tasks = max_concurrent_tasks
        self.active_tasks = []
        self.playwright = None
        self.browser = None
        
        self.base_urls = base_urls
        # Загружаем стартовые ссылки
        for url in base_urls:
            self.queue.put_nowait(url)

    async def start(self):
        print("🕷️ Spider Core Started...")
        self.playwright = await async_playwright().start()
        self.browser = await self.playwright.chromium.launch(headless=False)
        
        # Создаем воркеры
        for i in range(self.max_concurrent_tasks):
            task = asyncio.create_task(self.worker(f"Worker-{i}"))
            self.active_tasks.append(task)
            
        await self.queue.join()
        
        for task in self.active_tasks:
            task.cancel()
            
        await self.browser.close()
        await self.playwright.stop()
        print("🕷️ Spider Core Finished.")

    async def worker(self, name: str):
        # Открываем вкладку браузера для этого воркера
        context = await self.browser.new_context()
        page = await context.new_page()
        
        while True:
            try:
                url = await self.queue.get()
                
                if url not in self.base_urls and is_url_visited(url):
                    self.queue.task_done()
                    continue
                    
                print(f"[{name}] Сканируем: {url}")
                await self.process_url(url, page)
                
                mark_url_visited(url)
                self.queue.task_done()
            except asyncio.CancelledError:
                break
            except Exception as e:
                print(f"[{name}] Ошибка: {e}")
                self.queue.task_done()
                
        await context.close()

    async def process_url(self, url: str, page: Page):
        """Эта функция может переопределяться для конкретного парсера. 
        В базовой реализации просто собираем текст и отправляем ИИ."""
        html = await fetch_html(url, page)
        if not html:
            return
            
        # Заглушка для поиска новых ссылок (Spider)
        new_links = self.extract_links(html, url)
        for link in new_links:
            if not is_url_visited(link):
                self.queue.put_nowait(link)
                
        # Извлекаем текст
        text = extract_text_from_html(html)
        if len(text) < 100:
            return
            
        # Отправляем ИИ
        structured_data = await process_forum_text(text)
        if structured_data and structured_data.get("carBrand") != "REJECT":
            # Успех, загружаем в Firebase
            await upload_to_moderation_queue(structured_data)

    def extract_links(self, html: str, base_url: str) -> List[str]:
        """Заглушка для извлечения новых ссылок. Должна быть реализована под конкретный сайт."""
        # Для безопасности базовый паук не переходит никуда, чтобы не спамить весь интернет
        return []

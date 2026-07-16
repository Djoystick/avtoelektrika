import asyncio
from bs4 import BeautifulSoup
from playwright.async_api import Page

async def fetch_html(url: str, page: Page) -> str:
    try:
        # Переходим на страницу
        await page.goto(url, wait_until="domcontentloaded", timeout=30000)
        # Ждем 3 секунды, чтобы JS (React) отрендерил посты на Drive2
        await page.wait_for_timeout(3000)
        
        # Если нас перекинуло на антибот (Cloudflare / reception), ждем пока не пустит дальше
        while "reception" in page.url:
            print(f"⏳ Ожидание антибота для {url} (Решите капчу в открытом окне браузера!)")
            await page.wait_for_timeout(5000)
        
        # Дадим еще пару секунд на подгрузку ленты после прохождения защиты
        await page.wait_for_timeout(3000)
        
        # Забираем итоговый HTML
        html = await page.content()
        return html
    except Exception as e:
        print(f"❌ Error fetching {url}: {e}")
        return ""

def extract_text_from_html(html: str) -> str:
    """Извлекает полезный текст из сырого HTML, удаляя скрипты, стили и меню."""
    soup = BeautifulSoup(html, 'lxml')
    
    # Удаляем невидимые элементы
    for script_or_style in soup(["script", "style", "header", "footer", "nav", "aside"]):
        script_or_style.decompose()
        
    # Ищем основной контент (на разных форумах это разные классии, но ИИ справится с "сырым" текстом тела)
    # Постараемся взять <title> и текстовые блоки
    title = soup.title.string if soup.title else ""
    
    # Берем текст, убирая лишние пробелы
    text_content = soup.get_text(separator=' ', strip=True)
    
    # Ограничиваем длину текста, чтобы не превысить лимит токенов (например, первые 6000 символов)
    # ИИ Dellmar (gpt-4o-mini) поддерживает до 128k, но нам хватит первых сообщений
    combined_text = f"Заголовок темы: {title}\n\nТекст обсуждения: {text_content}"
    return combined_text[:8000]

async def scrape_forum_thread(url: str) -> str:
    """Главная функция для парсинга одной ветки."""
    async with aiohttp.ClientSession() as session:
        html = await fetch_html(url, session)
        if not html:
            return ""
        return extract_text_from_html(html)

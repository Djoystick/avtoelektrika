from bs4 import BeautifulSoup
from urllib.parse import urljoin
from spider_core import SpiderCore
import re

class AutoBkSpider(SpiderCore):
    """
    Паук специально для форума auto-bk.ru (профессиональный форум автоэлектриков).
    Тут нет капчи Cloudflare, так что паук может работать на 100% автономно.
    """
    def extract_links(self, html: str, base_url: str):
        soup = BeautifulSoup(html, 'lxml')
        links = []
        
        # 1. Сбор ссылок на страницы пагинации (page/2, page/3)
        for a_tag in soup.find_all('a', href=True):
            href = a_tag['href']
            
            if 'page' in href.lower():
                full_url = urljoin(base_url, href)
                links.append(full_url)
                
            # 2. Ссылки на конкретные темы форума
            # Пример: https://www.auto-bk.ru/forum/topic/91783-ваз-2114-не-заводится/
            if re.search(r'/topic/\d+', href):
                # Исключаем ссылки на конкретные посты (с якорями #entry)
                if '#entry' not in href and '?do=' not in href:
                    full_url = urljoin(base_url, href)
                    # Очищаем URL от параметров, чтобы не дублировать
                    full_url = full_url.split('?')[0].split('#')[0]
                    links.append(full_url)
                
        # Возвращаем уникальные ссылки
        return list(set(links))

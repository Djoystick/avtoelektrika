from bs4 import BeautifulSoup
from urllib.parse import urljoin
from spider_core import SpiderCore
import re

class GenericForumSpider(SpiderCore):
    """
    Пример парсера под типичный форум на движке XenForo/vBulletin.
    """
    
    def extract_links(self, html: str, base_url: str):
        soup = BeautifulSoup(html, 'lxml')
        links = []
        
        # Ищем ссылки на следующие страницы пагинации (обычно классы типа pageNav, pagination)
        for a_tag in soup.find_all('a', href=True):
            href = a_tag['href']
            # Пример эвристики: если ссылка ведет на страницу /page-2, /index.php?page=2 и т.д.
            if 'page' in href or 'p=' in href:
                full_url = urljoin(base_url, href)
                links.append(full_url)
                
            # Ищем ссылки на сами темы форума (обычно содержат thread, topic)
            if 'thread' in href or 'topic' in href or '/t' in href:
                # Фильтруем "Решенные" темы. Часто на форумах есть метка [Решено] в заголовке
                # Но мы будем брать все темы, а ИИ уже сам отсеет мусор, 
                # либо можно парсить класс иконки статуса.
                # Для MVP берем все темы в разделе:
                full_url = urljoin(base_url, href)
                links.append(full_url)
                
        # Удаляем дубликаты
        return list(set(links))

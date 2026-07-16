from bs4 import BeautifulSoup
from urllib.parse import urljoin
from spider_core import SpiderCore
import re

class Drive2Spider(SpiderCore):
    """
    Паук специально для Drive2.ru.
    """
    def extract_links(self, html: str, base_url: str):
        soup = BeautifulSoup(html, 'lxml')
        links = []
        
        # 1. Сбор ссылок на следующие страницы пагинации (обычно ?page=2 или кнопка "Показать еще")
        for a_tag in soup.find_all('a', href=True):
            href = a_tag['href']
            
            # Пагинация Drive2 часто идет параметром ?page= или /page2/
            if '?page=' in href or 'page' in href:
                full_url = urljoin(base_url, href)
                links.append(full_url)
                
            # 2. Ссылки на конкретные записи в бортжурналах или сообществах
            # На drive2 ссылки на посты выглядят как /l/1234567/ (логи) или /c/1234567/ (сообщества) или /b/1234567/ (блоги)
            if re.search(r'/[lcb]/\d+/?', href):
                full_url = urljoin(base_url, href)
                
                # Мы можем предварительно фильтровать по заголовку ссылки, если он содержит нужные слова
                title = a_tag.get_text().lower()
                keywords = ["решено", "ремонт", "починил", "проблема", "причина", "электрик", "схема", "ошибка"]
                
                # Даже если в заголовке нет ключевиков, мы можем собрать URL,
                # а ИИ отсеет мусор. Но чтобы не спамить ИИ, оставим легкий фильтр.
                # Для MVP: берем все посты. ИИ вернет REJECT для постов типа "помыл машину".
                links.append(full_url)
                
        # Возвращаем уникальные ссылки
        return list(set(links))

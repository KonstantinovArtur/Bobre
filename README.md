Наследие кузни — интернет-магазин кузнечных изделий

Полноценное веб-приложение интернет-магазина на Spring Boot с ролями, админ-панелью, аналитикой,
корзиной, заказами и REST API.  
Автор: Константинов Артур Олегович

---

Возможности проекта:
- Каталог товаров, категории, поиск и фильтры
- Корзина, оформление заказов, статусы заказов
- Отзывы и рейтинги
- Кабинеты ролей (пользователь / менеджер / склад / администратор)
- Админ-панель: управление пользователями, логами, аналитикой
- Swagger/OpenAPI документация
- Поддержка запуска локально и в Docker

---

Локальный запуск:
1. Требования: Java 17, Maven, PostgreSQL 14+
2. Создать БД:
   CREATE DATABASE "DBForKurs2" WITH ENCODING 'UTF8';
3. Переменные окружения:
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/DBForKurs2
4. Запуск:
   mvn clean package
   java -jar target/*.jar
   http://localhost:8080

---

Docker запуск:
docker compose build
docker compose up -d
http://localhost:8080

---

Архитектура проекта:
config — настройки Spring  
security — роли, фильтры  
domain/model — сущности  
repository — репозитории  
service — бизнес-логика  
controller — MVC  
api — REST  
templates — Thymeleaf  
static — CSS/JS

---

Роли:
Гость — просмотр каталога  
Пользователь — корзина, заказы  
Менеджер — управление товарами  
Склад — подтверждение поставок  
Администратор — пользователи, логи, аналитика

---

Swagger:
http://localhost:8080/swagger-ui/index.html

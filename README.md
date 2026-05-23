# Poker Club Tournament Manager

MVP веб-приложения для автоматизации управления турнирами и тренировками спортивного poker-клуба.

## Стек

- Backend: Java 17, Spring Boot 3.5, Spring Security, JWT, Spring Data JPA, PostgreSQL
- Frontend: React, TypeScript, Vite
- База данных: PostgreSQL

## Быстрый запуск

1. Создать PostgreSQL базу данных:

```sql
CREATE DATABASE pokerclub;
```

2. В `backend/src/main/resources/application.yml` проверить параметры подключения:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pokerclub
    username: postgres
    password: postgres
```

3. Запустить backend из IDE на JDK 17.

4. Запустить frontend:

```bash
cd frontend
npm install
npm run dev
```

Тестовые пользователи создаются автоматически при первом запуске:

- Администратор: `admin@pokerclub.local` / `admin12345`
- Посетитель: `visitor@pokerclub.local` / `visitor12345`


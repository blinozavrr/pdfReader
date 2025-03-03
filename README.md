# UniversityFilterToDB

Этот проект предназначен для извлечения информации из PDF-файлов и сохранения данных о студентах в базу данных PostgreSQL. Программа фильтрует данные по коду специальности и сохраняет их в отдельные таблицы.

## 📌 Требования

Перед началом работы убедитесь, что у вас установлены:
- **Java 17+** (желательно OpenJDK 23)
- **Maven** (для управления зависимостями)
- **PostgreSQL** (и доступ к pgAdmin 4)
- **Git** (для клонирования репозитория)

---
## 🔥 Установка

### 📥 1. Скачивание проекта

Используйте Git для клонирования проекта:
```sh
# Клонируем репозиторий
git clone https://github.com/blinozavrr/pdfReader

# Переходим в папку проекта
cd pdfReader
```

### 📂 2. Открытие в IntelliJ IDEA

1. Открыть IntelliJ IDEA
2. Выбрать `File` → `Open` и указать папку проекта.
3. IntelliJ сам обнаружит `pom.xml` и предложит импортировать зависимости.

---
## 🏛️ Настройка базы данных

### 1️⃣ Создание базы данных в pgAdmin 4

1. **Открываем pgAdmin 4**.
2. **Подключаемся к серверу** (если не подключено, правый клик → `Connect`).
3. **Создаём новую базу данных:**
   - В `Object Explorer` правый клик на `Databases` → `Create` → `Database`.
   - В поле **Database name** вводим `postgres`.
   - В `Owner` указываем `postgres`.
   - Нажимаем `Save`.

### 2️⃣ Создание пользователя и прав доступа

Открываем SQL Query Editor и выполняем:
```sql
CREATE USER myuser WITH PASSWORD 'mypassword';
ALTER ROLE myuser SET client_encoding TO 'utf8';
ALTER ROLE myuser SET default_transaction_isolation TO 'read committed';
ALTER ROLE myuser SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE postgres TO myuser;
```
> Замените `myuser` и `mypassword` на ваши значения.

### 3️⃣ Подключение к базе данных

В файле `UniversityFilterToDB.java` замените `DB_URL`, `DB_USER`, `DB_PASSWORD` на ваши значения:
```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
private static final String DB_USER = "myuser";
private static final String DB_PASSWORD = "mypassword";
```
---
## 🚀 Запуск проекта

### 1️⃣ Сборка проекта с помощью Maven
```sh
mvn clean install
```

### 2️⃣ Запуск приложения
Запускаем через IntelliJ IDEA или в терминале:
```sh
java -jar target/UniversityFilterToDB.jar
```
> Введите код специальности (например, `001`) и программа загрузит данные в базу.

---
## 🗃️ Структура базы данных

После запуска данные загружаются в таблицу `students_XXX`, где `XXX` — код специальности.

Пример структуры таблицы `students_001`:
```sql
CREATE TABLE students_001 (
    student_id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    total_score INT NOT NULL,
    university_code TEXT NOT NULL
);
```
---
## 🛠️ Возможные ошибки и их решение

### ❌ Ошибка подключения
**Ошибка:**
```sh
Ошибка подключения к базе данных: FATAL: password authentication failed for user "postgres"
```
**Решение:** Проверьте правильность логина и пароля в `DB_USER` и `DB_PASSWORD`.

### ❌ Файл PDF не найден
**Ошибка:**
```sh
Ошибка при чтении PDF: file.pdf (No such file or directory)
```
**Решение:** Проверьте путь к файлу в `pdfFilePath`.

### ❌ `NumberFormatException`
**Ошибка:**
```sh
java.lang.NumberFormatException: For input string: "САҒИДУЛЛАҚЫЗЫ"
```
**Решение:** Фильтр строк добавлен в код. Убедитесь, что в PDF корректные данные.

---
## 📝 Заключение
Теперь у вас есть полностью настроенный проект, работающий с PostgreSQL и pgAdmin. 🚀


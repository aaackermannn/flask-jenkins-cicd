# flask-jenkins-cicd

Учебный репозиторий к практической работе № 4 «Практики DevOps в современных методологиях разработки».
Простое Flask-приложение, Dockerfile и Jenkinsfile, описывающий весь процесс CI/CD:
сборка Docker-образа → тесты → деплой в staging → проверка доступности → ручное подтверждение → деплой в production.

## Структура

```
├── app/app.py              Flask-приложение: /, /health, /api/sum
├── tests/test_app.py       модульные тесты (pytest)
├── Dockerfile              многоэтапная сборка: base → test → runtime
├── Jenkinsfile             усложнённый вариант: Docker-конвейер
├── scripts/
│   ├── deploy.sh           деплой контейнера в staging / production
│   └── smoke_test.sh       проверка доступности развёрнутого приложения
├── basic/                  базовый вариант по методическим указаниям
│   ├── Jenkinsfile
│   ├── deploy
│   └── smoke-tests
└── jenkins/
    ├── Dockerfile          образ Jenkins LTS + Docker CLI + плагины
    ├── docker-compose.yml  запуск Jenkins с доступом к Docker хоста
    └── init.groovy.d/      автонастройка: admin, секрет, задания
```

## Локальный запуск

```bash
python -m venv .venv
.venv/Scripts/pip install -r requirements-dev.txt   # Linux/macOS: .venv/bin/pip
.venv/Scripts/python -m pytest
.venv/Scripts/python -m app.app                     # http://localhost:5000
```

Через Docker:

```bash
docker build --target runtime -t flask-cicd-demo .
docker run --rm -p 5000:5000 flask-cicd-demo
```

## Запуск Jenkins

Создайте файл `jenkins/.env` с паролем администратора (файл не попадает в Git):

```
JENKINS_ADMIN_PASSWORD=придумайте-пароль
```

```bash
cd jenkins
docker compose up -d --build
```

Мастер первоначальной настройки отключён: при старте Jenkins выполняет скрипты из `jenkins/init.groovy.d` —
создаёт пользователя `admin`, секрет `demo-access-key` и оба задания. Откройте http://localhost:8080 и войдите как `admin`.

Код Jenkins берёт из этого же репозитория, примонтированного в контейнер (`file:///repos/flask-jenkins-cicd`,
ветка `main`), поэтому изменения нужно закоммитить. Чтобы собирать из GitHub, задайте в `jenkins/.env`
переменную `REPO_URL=https://github.com/<логин>/flask-jenkins-cicd.git` до первого запуска.

Jenkins подключён к Docker-сети `cicd-net`, поэтому конвейер обращается к развёрнутым контейнерам по имени
(`http://flask-staging:5000`), а с хоста они доступны на портах 8081 (staging) и 8082 (production).

## Задания в Jenkins

| Задание | Тип | Script Path |
|---|---|---|
| `basic-pipeline` | Pipeline script from SCM | `basic/Jenkinsfile` |
| `flask-cicd` | Pipeline script from SCM | `Jenkinsfile` |

В обоих: SCM — Git, Branch Specifier — `*/main`. Задания и секрет `demo-access-key` (тип **Secret text**)
создаются автоматически; вручную это делается через *Создать Item → Pipeline* и
*Настроить Jenkins → Credentials → System → Global credentials → Add Credentials*.

Уведомления о провале: настройте SMTP в *Настроить Jenkins → System → Уведомление по почте* и задайте переменную
`NOTIFY_EMAIL` (*Настроить Jenkins → System → Global properties → Environment variables*).

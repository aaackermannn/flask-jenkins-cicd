#!/usr/bin/env bash
# Проверка доступности развёрнутого приложения.
# Использование: smoke_test.sh <base_url>, например http://flask-staging:5000
set -euo pipefail

BASE_URL="${1:?usage: smoke_test.sh <base_url>}"
ATTEMPTS=20

echo "Ожидание запуска ${BASE_URL} ..."
for i in $(seq 1 "$ATTEMPTS"); do
    if curl -fsS --max-time 3 "${BASE_URL}/health" >/dev/null 2>&1; then
        break
    fi
    if [ "$i" -eq "$ATTEMPTS" ]; then
        echo "Сервис ${BASE_URL} не ответил за $((ATTEMPTS * 2)) с" >&2
        exit 1
    fi
    sleep 2
done

check() {
    local path="$1" pattern="$2" body
    body="$(curl -fsS --max-time 5 "${BASE_URL}${path}")"
    echo "GET ${path} -> ${body}"
    echo "$body" | grep -Eq "$pattern" || { echo "Ответ ${path} не содержит ${pattern}" >&2; exit 1; }
}

check "/health" '"status": ?"ok"'
check "/" 'Hello from Flask CI/CD demo'
check "/api/sum?a=2&b=3" '"result": ?5'

echo "Smoke-тесты пройдены: ${BASE_URL}"

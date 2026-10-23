#!/usr/bin/env bash
# Деплой Docker-образа в окружение staging или production.
# Использование: deploy.sh <staging|production> <image:tag>
set -euo pipefail

ENV_NAME="${1:?usage: deploy.sh <staging|production> <image:tag>}"
IMAGE="${2:?usage: deploy.sh <staging|production> <image:tag>}"
NETWORK="${DOCKER_NET:-cicd-net}"

case "$ENV_NAME" in
    staging)    NAME="flask-staging";    PORT="${STAGING_PORT:-8081}" ;;
    production) NAME="flask-production"; PORT="${PROD_PORT:-8082}" ;;
    *) echo "Неизвестное окружение: $ENV_NAME" >&2; exit 1 ;;
esac

echo "Деплой ${IMAGE} в окружение ${ENV_NAME} (контейнер ${NAME}, порт ${PORT})"
docker network inspect "$NETWORK" >/dev/null 2>&1 || docker network create "$NETWORK"
docker rm -f "$NAME" >/dev/null 2>&1 || true
docker run -d \
    --name "$NAME" \
    --restart unless-stopped \
    --network "$NETWORK" \
    -p "${PORT}:5000" \
    -e APP_ENV="$ENV_NAME" \
    "$IMAGE"
docker ps --filter "name=^${NAME}$" --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'

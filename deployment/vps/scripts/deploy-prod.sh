#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
ENV_FILE="$PROJECT_ROOT/deployment/vps/.env.prod"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing $ENV_FILE. Copy deployment/vps/.env.prod.example to .env.prod and fill real values."
  exit 1
fi

cd "$PROJECT_ROOT/deployment/vps"
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build

echo "Production stack deployed."

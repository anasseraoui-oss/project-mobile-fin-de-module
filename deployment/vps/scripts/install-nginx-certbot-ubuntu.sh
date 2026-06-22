#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 5 ]]; then
  echo "Usage: $0 email auth.example.com api.example.com s3.example.com minio.example.com"
  exit 1
fi

EMAIL="$1"
AUTH_DOMAIN="$2"
API_DOMAIN="$3"
S3_DOMAIN="$4"
MINIO_DOMAIN="$5"

sudo apt-get update
sudo apt-get install -y nginx snapd
sudo snap install core || true
sudo snap refresh core
sudo snap install --classic certbot
sudo ln -sf /snap/bin/certbot /usr/bin/certbot

sudo mkdir -p /var/www/html
for DOMAIN in "$AUTH_DOMAIN" "$API_DOMAIN" "$S3_DOMAIN" "$MINIO_DOMAIN"; do
  sudo certbot certonly --webroot \
    -w /var/www/html \
    --agree-tos \
    --no-eff-email \
    -m "$EMAIL" \
    -d "$DOMAIN"
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONF_SOURCE="$SCRIPT_DIR/../nginx/elearning.conf"
TMP_CONF="$(mktemp)"

sed \
  -e "s/auth.example.com/$AUTH_DOMAIN/g" \
  -e "s/api.example.com/$API_DOMAIN/g" \
  -e "s/s3.example.com/$S3_DOMAIN/g" \
  -e "s/minio.example.com/$MINIO_DOMAIN/g" \
  "$CONF_SOURCE" > "$TMP_CONF"

sudo cp "$TMP_CONF" /etc/nginx/sites-available/elearning.conf
sudo ln -sf /etc/nginx/sites-available/elearning.conf /etc/nginx/sites-enabled/elearning.conf
sudo nginx -t
sudo systemctl reload nginx
rm -f "$TMP_CONF"

echo "Nginx and Let's Encrypt are configured."

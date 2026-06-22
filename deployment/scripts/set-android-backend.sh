#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $0 --auth-url URL --resource-url URL [--release] [--android-project DIR]"
}

AUTH_URL=""
RESOURCE_URL=""
ANDROID_PROJECT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../elearning-android" && pwd)"
RELEASE="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --auth-url)
      AUTH_URL="$2"
      shift 2
      ;;
    --resource-url)
      RESOURCE_URL="$2"
      shift 2
      ;;
    --android-project)
      ANDROID_PROJECT="$2"
      shift 2
      ;;
    --release)
      RELEASE="true"
      shift
      ;;
    *)
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$AUTH_URL" || -z "$RESOURCE_URL" ]]; then
  usage
  exit 1
fi

[[ "$AUTH_URL" == */ ]] || AUTH_URL="$AUTH_URL/"
[[ "$RESOURCE_URL" == */ ]] || RESOURCE_URL="$RESOURCE_URL/"

FILE="$ANDROID_PROJECT/local.properties"
touch "$FILE"

TMP="$(mktemp)"
grep -v -E '^(AUTH_SERVER_URL|RESOURCE_SERVER_URL|RELEASE_AUTH_SERVER_URL|RELEASE_RESOURCE_SERVER_URL)=' "$FILE" > "$TMP" || true

if [[ "$RELEASE" == "true" ]]; then
  {
    cat "$TMP"
    echo "RELEASE_AUTH_SERVER_URL=$AUTH_URL"
    echo "RELEASE_RESOURCE_SERVER_URL=$RESOURCE_URL"
  } > "$FILE"
else
  {
    cat "$TMP"
    echo "AUTH_SERVER_URL=$AUTH_URL"
    echo "RESOURCE_SERVER_URL=$RESOURCE_URL"
  } > "$FILE"
fi

rm -f "$TMP"
echo "Updated $FILE"

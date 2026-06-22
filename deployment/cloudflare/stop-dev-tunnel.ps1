param(
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
)

$ErrorActionPreference = "Stop"

Push-Location $ProjectRoot
try {
    docker compose `
        -f docker-compose.yml `
        -f deployment/cloudflare/docker-compose.cloudflare.yml `
        --env-file .env `
        --env-file deployment/cloudflare/.env.cloudflare `
        down
}
finally {
    Pop-Location
}

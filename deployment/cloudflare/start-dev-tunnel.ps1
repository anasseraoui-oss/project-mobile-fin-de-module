param(
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
)

$ErrorActionPreference = "Stop"
$cloudflareEnv = Join-Path $ProjectRoot "deployment\cloudflare\.env.cloudflare"

if (-not (Test-Path $cloudflareEnv)) {
    throw "Missing $cloudflareEnv. Copy deployment\cloudflare\.env.cloudflare.example to .env.cloudflare and fill it."
}

Push-Location $ProjectRoot
try {
    docker compose `
        -f docker-compose.yml `
        -f deployment/cloudflare/docker-compose.cloudflare.yml `
        --env-file .env `
        --env-file deployment/cloudflare/.env.cloudflare `
        up -d --build
}
finally {
    Pop-Location
}

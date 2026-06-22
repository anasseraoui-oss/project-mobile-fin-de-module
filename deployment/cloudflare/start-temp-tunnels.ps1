param(
    [int]$AuthPort = 8081,
    [int]$ResourcePort = 8082,
    [int]$MinioPort = 9003,
    [string]$CloudflaredPath = "C:\Program Files (x86)\cloudflared\cloudflared.exe",
    [string]$OutputDir = (Join-Path $PSScriptRoot "temp")
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $CloudflaredPath)) {
    throw "cloudflared not found at $CloudflaredPath"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

Get-CimInstance Win32_Process |
    Where-Object { $_.Name -eq "cloudflared.exe" -and $_.CommandLine -match "tunnel --url http://localhost:(8081|8082|9003)" } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }

function Start-Tunnel {
    param(
        [string]$Name,
        [int]$Port
    )

    $stdout = Join-Path $OutputDir "$Name.out.log"
    $stderr = Join-Path $OutputDir "$Name.err.log"
    Remove-Item -LiteralPath $stdout, $stderr -Force -ErrorAction SilentlyContinue

    Start-Process `
        -FilePath $CloudflaredPath `
        -ArgumentList @("tunnel", "--url", "http://localhost:$Port") `
        -WindowStyle Hidden `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr

    $deadline = (Get-Date).AddSeconds(45)
    do {
        Start-Sleep -Milliseconds 500
        $content = ""
        if (Test-Path $stdout) { $content += Get-Content -Raw -LiteralPath $stdout -ErrorAction SilentlyContinue }
        if (Test-Path $stderr) { $content += Get-Content -Raw -LiteralPath $stderr -ErrorAction SilentlyContinue }
        $match = [regex]::Match($content, "https://[a-z0-9-]+\.trycloudflare\.com")
        if ($match.Success) {
            return $match.Value
        }
    } while ((Get-Date) -lt $deadline)

    throw "Timed out waiting for $Name tunnel URL. Check $stdout and $stderr."
}

$authUrl = Start-Tunnel -Name "auth" -Port $AuthPort
$resourceUrl = Start-Tunnel -Name "resource" -Port $ResourcePort
$minioUrl = Start-Tunnel -Name "minio" -Port $MinioPort

$envFile = Join-Path $OutputDir "current-tunnels.env"
@(
    "AUTH_PUBLIC_ORIGIN=$authUrl",
    "RESOURCE_PUBLIC_ORIGIN=$resourceUrl",
    "MINIO_PUBLIC_ORIGIN=$minioUrl"
) | Set-Content -LiteralPath $envFile -Encoding UTF8

[pscustomobject]@{
    AuthUrl = $authUrl
    ResourceUrl = $resourceUrl
    MinioUrl = $minioUrl
    EnvFile = $envFile
}

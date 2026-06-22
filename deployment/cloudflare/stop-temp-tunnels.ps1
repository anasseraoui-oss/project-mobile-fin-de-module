$ErrorActionPreference = "Stop"

Get-CimInstance Win32_Process |
    Where-Object { $_.Name -eq "cloudflared.exe" -and $_.CommandLine -match "tunnel --url http://localhost:(8081|8082|9003)" } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }

# Backend Access Architecture for Android Development and Production

## Recommendation

Use two stable hostnames for every environment:

- Auth server: `https://auth-dev.example.com` in development, `https://auth.example.com` in production.
- Resource server: `https://api-dev.example.com` in development, `https://api.example.com` in production.
- Optional MinIO public S3 endpoint: `https://s3-dev.example.com` in development, `https://s3.example.com` in production.

The Android app should never know the PC LAN IP address. It should only read URLs from `BuildConfig`, which is already how this project is structured in `NetworkConfig.kt`.

Best setup:

- Development from a real phone: Cloudflare Tunnel with fixed public hostnames.
- Private-only development: Tailscale if you do not want public access and can install Tailscale on the phone.
- Production: VPS with Docker Compose, PostgreSQL, Redis, MinIO, and host Nginx reverse proxy with Let's Encrypt certificates.

## Comparison

| Option | Stable URL | HTTPS | Real phone testing | Security | Production fit | Verdict |
|---|---:|---:|---:|---:|---:|---|
| ngrok | Yes with static/dev domain | Yes | Excellent | Good, third-party edge | Medium | Great quick demo, less ideal as main workflow |
| Cloudflare Tunnel | Yes with your domain | Yes at Cloudflare edge | Excellent | Strong, outbound-only, no router port forwarding | Medium/High for staging | Best local development choice |
| Tailscale | Yes inside tailnet | Yes via Serve/Funnel | Good if phone has Tailscale | Very strong private access | Medium | Best private dev, not best for public testers |
| DuckDNS | DNS only | Needs extra TLS setup | Works only with port forwarding/public IP | Weak alone | Low/Medium | Not recommended for your current need |
| VPS | Yes | Yes with Nginx/Certbot | Excellent | Strong if hardened | High | Best production choice |

## Why local IPs break

`192.168.x.x` or `10.x.x.x` belongs to the current Wi-Fi network. When the PC changes network, the address changes. A phone on mobile data cannot reach it, and even a phone on the same Wi-Fi may fail if the firewall blocks the ports.

Cloudflare Tunnel and Tailscale avoid this by connecting outbound from your PC to a stable network identity. VPS avoids it by moving the backend to a stable server.

## Android URL strategy

Keep this invariant:

```text
Android code -> NetworkConfig -> BuildConfig -> Gradle properties/env vars
```

Debug builds should read:

```properties
AUTH_SERVER_URL=https://auth-dev.example.com/
RESOURCE_SERVER_URL=https://api-dev.example.com/
```

Release builds should read:

```properties
RELEASE_AUTH_SERVER_URL=https://auth.example.com/
RELEASE_RESOURCE_SERVER_URL=https://api.example.com/
```

Use `deployment/scripts/set-android-backend.ps1` or `deployment/scripts/set-android-backend.sh` to update `elearning-android/local.properties` without editing Kotlin source code.

## Dynamic configuration

Recommended layers:

1. Compile-time environment selection with `BuildConfig`.
2. A stable optional remote config file for feature flags and non-critical URLs:

   ```text
   https://config.example.com/mobile-config.json
   ```

3. Never fetch the primary API base URL from the API itself. That creates a bootstrapping problem.

The project includes `deployment/mobile-config/mobile-config.example.json` as a contract if you later add runtime remote config.

## Cloudflare Tunnel development flow

1. Own a domain managed by Cloudflare.
2. Create a remotely-managed tunnel in Cloudflare Zero Trust.
3. Add public hostnames:

   | Public hostname | Service URL |
   |---|---|
   | `auth-dev.example.com` | `http://auth-server:9000` |
   | `api-dev.example.com` | `http://resource-server:8080` |
   | `s3-dev.example.com` | `http://minio:9000` |
   | `minio-dev.example.com` | `http://minio:9001` |

4. Copy the tunnel token into `deployment/cloudflare/.env.cloudflare`.
5. Start Docker:

   ```powershell
   .\deployment\cloudflare\start-dev-tunnel.ps1
   ```

6. Update Android debug URLs:

   ```powershell
   .\deployment\scripts\set-android-backend.ps1 `
     -AuthUrl "https://auth-dev.example.com/" `
     -ResourceUrl "https://api-dev.example.com/"
   ```

7. Rebuild the Android debug app.

## Docker/Spring requirements for tunnels

The resource server must validate JWTs against the same public issuer that Android uses:

```text
AUTH_SERVER_ISSUER_URI=https://auth-dev.example.com
```

The resource server can still fetch JWKs internally:

```text
AUTH_SERVER_URL=http://auth-server:9000
```

MinIO presigned URLs must use the public S3 hostname:

```text
MINIO_PUBLIC_URL=https://s3-dev.example.com
```

The auth server now supports:

```text
AUTH_SERVER_ISSUER=https://auth-dev.example.com
```

This keeps OAuth `iss` stable when the PC IP changes.

## VPS production architecture

```text
Internet
  |
  | HTTPS
  v
Nginx on VPS
  |-- auth.example.com  -> 127.0.0.1:9000  -> auth-server container
  |-- api.example.com   -> 127.0.0.1:8081  -> resource-server container
  |-- s3.example.com    -> 127.0.0.1:9003  -> MinIO S3 API
  |-- minio.example.com -> 127.0.0.1:9001  -> MinIO console

Docker internal network:
auth-server, resource-server, PostgreSQL, Redis, MinIO
```

On the VPS, only ports `80` and `443` should be public. Bind backend container ports to `127.0.0.1`.

## Migration rule

To migrate from local development to production without changing Android code:

1. Keep the Kotlin code using `NetworkConfig`.
2. Change only Gradle properties:

   ```properties
   RELEASE_AUTH_SERVER_URL=https://auth.example.com/
   RELEASE_RESOURCE_SERVER_URL=https://api.example.com/
   ```

3. Keep the same API paths and OAuth redirect URI.
4. Keep issuer and resource validation aligned:

   ```text
   AUTH_SERVER_ISSUER=https://auth.example.com
   AUTH_SERVER_ISSUER_URI=https://auth.example.com
   ```

## Production hardening checklist

- Replace development passwords and never commit `.env`.
- Persist or externalize authorization server signing keys. Current in-memory RSA keys invalidate tokens after restart.
- Add database backups for PostgreSQL and MinIO.
- Protect MinIO console with firewall, VPN, or Cloudflare Access.
- Enable rate limiting at Nginx or Cloudflare.
- Keep Android release builds HTTPS-only.
- Use monitoring for container health and disk usage.

## References

- Cloudflare Tunnel: https://developers.cloudflare.com/cloudflare-one/networks/connectors/cloudflare-tunnel/
- Cloudflare Tunnel routing: https://developers.cloudflare.com/tunnel/routing/
- ngrok domains: https://ngrok.com/docs/universal-gateway/domains/
- Tailscale Funnel: https://tailscale.com/docs/features/tailscale-funnel
- DuckDNS install: https://www.duckdns.org/install.jsp
- Nginx reverse proxy: https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/
- Certbot with Nginx: https://certbot.eff.org/instructions?ws=nginx
- Docker Compose variable interpolation: https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/

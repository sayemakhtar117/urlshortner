# URL Shortener

A Spring Boot URL shortener using MongoDB to persist URL mappings and Redis to cache redirects. The project includes a REST API, Swagger UI, Docker Compose services, Kubernetes manifests, and a Maven test suite.

## Contents

- [Requirements](#requirements)
- [Configuration](#configuration)
- [Run Locally](#run-locally)
- [Test the API](#test-the-api)
- [Run Tests](#run-tests)
- [Docker Compose](#docker-compose)
- [Kubernetes Deployment](#kubernetes-deployment)
- [GitHub Actions](#github-actions)
- [Troubleshooting](#troubleshooting)

## Requirements

- Java 17 or newer
- Docker with the Docker Compose plugin
- Git
- `kubectl` and a Kubernetes cluster for Kubernetes deployment

The Maven wrapper is included, so a separate Maven installation is not required.

## Configuration

| Setting | Default | Purpose |
| --- | --- | --- |
| `SPRING_DATA_MONGODB_URI` | `mongodb://localhost:27017/urlshortener` | MongoDB connection string |
| `SPRING_REDIS_HOST` | `localhost` | Redis hostname |
| `SPRING_REDIS_PORT` | `6379` | Redis port |
| `JWT_SECRET` | Development-only fallback in `application.yml` | HS256 signing key; set a unique secret of at least 32 bytes outside local development |
| `SPRING_SECURITY_USER_NAME` | `user` | HTTP Basic username; Spring Boot's default user is used if unset |
| `SPRING_SECURITY_USER_PASSWORD` | Generated at startup if unset | HTTP Basic password |

`POST /api/shorten` requires HTTP Basic authentication. If credentials are not configured, Spring Boot prints a generated password in the application startup log. The redirect endpoint and API documentation are public.

The JWT utility uses `JWT_SECRET`. Current API route protection uses HTTP Basic and does not issue or validate JWTs.

## Run Locally

Start MongoDB and Redis in Docker:

```powershell
docker compose up -d mongo redis
```

For predictable local API credentials, set environment variables before starting the application. These values are for local use only:

```powershell
$env:SPRING_SECURITY_USER_NAME = "local-user"
$env:SPRING_SECURITY_USER_PASSWORD = "local-only-change-me"
$env:JWT_SECRET = "local-development-secret-change-before-deploy-123456"
& (Join-Path $PWD 'mvnw.cmd') spring-boot:run
```

The application listens on `http://localhost:8080`.

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

The OpenAPI page lists the routes, but HTTP Basic is not currently declared as an OpenAPI security scheme. Use `curl` or Postman with Basic Auth to call the protected shorten route.

## Test the API

Create a short URL. The request body is plain text, not a JSON object:

```powershell
curl.exe -i -u "local-user:local-only-change-me" `
  -H "Content-Type: text/plain" `
  --data "https://example.com" `
  http://localhost:8080/api/shorten
```

Expected result: `200 OK` and a short code in the response body, for example `a1b2c3`.

Use the returned code to test the redirect:

```powershell
curl.exe -i http://localhost:8080/a1b2c3
```

Expected result: `302 Found` and a `Location: https://example.com` response header. Do not add `-L` when you want to inspect the redirect instead of following it.

| Method | Path | Authentication | Behavior |
| --- | --- | --- | --- |
| `POST` | `/api/shorten` | HTTP Basic | Stores a URL and returns its short code |
| `GET` | `/{shortCode}` | Public | Redirects to the original URL |

## Run Tests

Run the complete Maven verification lifecycle:

```powershell
.\mvnw.cmd clean verify
```

On Linux or macOS:

```bash
./mvnw -B clean verify
```

The suite covers JWT generation/validation, service behavior with mocked Redis/repositories, and the controller flow. The controller test mocks the business services rather than writing real mappings.

## Docker Compose

Compose starts the application, MongoDB, and Redis. The app container connects to the Compose service names `mongo` and `redis`, not to its own `localhost`.

Start the complete local stack:

```powershell
docker compose up --build -d
docker compose logs -f app
```

The default Compose credentials are `urlshortener` / `local-only-change-me`. They are for local development only. Override them and the JWT key before starting Compose:

```powershell
$env:APP_USER = "local-user"
$env:APP_PASSWORD = "replace-with-a-local-password"
$env:JWT_SECRET = "replace-with-a-random-secret-of-at-least-32-bytes"
docker compose up --build -d
```

Check service status and stop the stack:

```powershell
docker compose ps
docker compose down
```

The current Compose file does not persist MongoDB data in a named volume; removing the MongoDB container also removes its writable data.

## Kubernetes Deployment

The manifests deploy the application and expose it through a `LoadBalancer` Service. MongoDB and Redis must already be reachable from the cluster; this repository does not provision those data services. Update the Redis hostname and image in `k8s/app.yaml` for your namespace, cluster, and image registry.

Build and push an image that the cluster can pull:

```bash
docker build -t YOUR_REGISTRY/url-shortener:YOUR_TAG .
docker push YOUR_REGISTRY/url-shortener:YOUR_TAG
```

Set the `image` field in `k8s/app.yaml` to that image. Create the Kubernetes Secret expected by the Deployment. In PowerShell, set these variables to your own production credentials and MongoDB endpoint first:

```powershell
$env:JWT_SECRET = "replace-with-a-random-secret-of-at-least-32-bytes"
$env:MONGODB_URI = "mongodb://YOUR_MONGODB_HOST:27017/urlshortener"
$env:APP_USER = "urlshortener-api"
$env:APP_PASSWORD = "replace-with-a-strong-password"

kubectl create secret generic url-shortener-secrets `
  --from-literal=jwt-secret="$env:JWT_SECRET" `
  --from-literal=mongodb-uri="$env:MONGODB_URI" `
  --from-literal=api-user="$env:APP_USER" `
  --from-literal=api-password="$env:APP_PASSWORD"
```

Apply the manifests and wait for the Deployment:

```powershell
kubectl apply -f k8s/app.yaml -f k8s/service.yaml
kubectl rollout status deployment/url-shortener
kubectl get service url-shortener-service
```

Use the Service's external IP or hostname on port 80. Configure TLS, network policies, secret rotation, resource limits, health probes, image-pull access, and managed/persistent database services before production use.

## GitHub Actions

The workflow at `.github/workflows/ci.yml` runs on pushes and pull requests. It installs Temurin Java 17, caches Maven dependencies, and executes `./mvnw -B clean verify`. If the job fails, Surefire test reports are uploaded as an artifact.

To view a run:

1. Push the repository to GitHub or open a pull request.
2. Open the repository's **Actions** tab.
3. Select **Java CI** and open the run for your branch or pull request.
4. Review the `Build and test` job. Download the `surefire-reports` artifact if the job fails.

This workflow tests the application; it does not publish a Docker image or deploy to Kubernetes. The current tests mock external data services, so no MongoDB or Redis service containers are required in CI.

## Troubleshooting

- **`401` or `403` from `/api/shorten`:** use HTTP Basic credentials. Check the startup log for Spring Boot's generated password or configure `SPRING_SECURITY_USER_NAME` and `SPRING_SECURITY_USER_PASSWORD` yourself.
- **MongoDB/Redis connection errors locally:** verify the services are listening on ports `27017` and `6379`. The app container uses `mongo` and `redis` as hostnames in Compose.
- **Kubernetes pods cannot start:** verify the image is pullable, `url-shortener-secrets` exists in the same namespace, and MongoDB/Redis hosts are reachable from the cluster.
- **JWT key validation errors:** configure `JWT_SECRET` to at least 32 bytes. Do not use the development fallback in production.
- **Maven test failures:** inspect `target/surefire-reports/` locally or download the artifact from the failed GitHub Actions run.
# Campus Lost and Found - Backend (REST API)

Spring Boot 3 / Java 17 REST API with JWT login, JPA, and a database (H2 locally, PostgreSQL in production).
The frontend lives in a separate repository: `campus-lost-found-frontend`.

## Run locally

```
mvn spring-boot:run
```

API at http://localhost:8080 (open it: you should see `{"status":"running"}`). Data goes to `./data`.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | /api/auth/register | no | create account, returns JWT |
| POST | /api/auth/login | no | log in, returns JWT |
| GET | /api/items?q=&type=&status= | no | list/search (type LOST/FOUND, status OPEN/IN_PROGRESS/CLOSED) |
| GET | /api/items/{id} | optional | item + the conversations you are allowed to see |
| GET | /api/items/{id}/image | no | photo |
| POST | /api/items | yes | report item (multipart: type,title,category,location,eventDate,description,image) |
| POST | /api/items/{id}/respond | yes | body `{"message": "..."}`: starts a private chat ("I found it" / "This is mine") |
| GET | /api/threads/{id} | yes, participants only | one conversation |
| POST | /api/threads/{id}/messages | yes, participants only | body `{"message": "..."}` |
| POST | /api/threads/{id}/confirm | yes, participants only | owner: "I got it back"; finder: "I handed it over". Case closes when both confirm |
| GET | /api/me/items | yes | my reports and the reports I replied to |

Send the token as `Authorization: Bearer <token>`.

## Environment variables (production)

| Variable | Example |
|---|---|
| DB_URL | `jdbc:postgresql://HOST:5432/DBNAME` |
| DB_USER / DB_PASS | database credentials |
| JWT_SECRET | a long random string, 32+ characters. **Required: the built-in default is public.** |
| CORS_ORIGIN | your frontend URL, no trailing slash, e.g. `https://campus-lost-found.netlify.app` |
| PORT | set automatically by most hosts |

## Deploy on Railway

1. Push this folder to its own GitHub repo.
2. On railway.com: New Project, Deploy from GitHub repo, pick this repo (the `Dockerfile` is detected automatically).
3. In the same project click New, Database, Add PostgreSQL.
4. Open the backend service, then Variables, and add (the `${{Postgres...}}` parts are Railway references to the database service; if your database service has another name, use that name):

| Variable | Value |
|---|---|
| DB_URL | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| DB_USER | `${{Postgres.PGUSER}}` |
| DB_PASS | `${{Postgres.PGPASSWORD}}` |
| JWT_SECRET | a long random string, 32+ characters |
| CORS_ORIGIN | your Netlify URL, no trailing slash |

5. Settings, Networking, Generate Domain. Open that URL: you should see `{"status":"running"}`. That URL is the frontend's `API_URL`.

Other hosts (Render, Fly, etc.) work the same way with the same variables.

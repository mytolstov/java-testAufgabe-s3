# Java Test Task S3

## Description

Spring Boot application that reads new customer order data from PostgreSQL, exports it as CSV files every 3 hours, and uploads the generated CSV files to AWS S3.

The application stores the last exported order ID to avoid exporting the same records multiple times.

## Technologies

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Lombok
- Scheduler
- AWS S3
- AWS SDK for Java

## Database

PostgreSQL database:

- KundenDB

### Tables

- kunde
- auftrage

### Relations

```text
auftrage.kunde_id -> kunde.kunde_id
```

```text
Kunde 1  --has--  many Auftrage
```

## CSV Export

The application exports only new records.

Export logic:

```text
1. Read last exported Auftrag ID
2. Find all Auftrage with ID greater than last exported ID
3. Create a new CSV file
4. Upload the CSV file to AWS S3
5. Save the newest exported Auftrag ID
```

CSV files are created locally in:

```text
exports/
```

Example file name:

```text
auftrage_2026-07-09_12-00-00.csv
```

The last exported ID is stored in:

```text
exports/last_exported_id.id
```

## AWS S3

Generated CSV files are uploaded to AWS S3.

Example S3 path:

```text
s3://your-bucket-name/exports/auftrage_2026-07-09_12-00-00.csv
```

The S3 bucket should stay private.

The application uses an IAM user with upload permissions.

Required upload permission:

```text
s3:PutObject
```

## Partner Access

The partner receives a separate AWS IAM user with read-only access.

Required read permissions:

```text
s3:ListBucket
s3:GetObject
```

The partner can access the exported CSV files through AWS Console:

```text
1. Log in to AWS Console
2. Open S3
3. Open the bucket
4. Open the exports folder
5. Download CSV files
```

The partner does not need AWS access keys if they download files through the AWS Console.

## Configuration

Database and AWS credentials are stored in `.env`.

Create a `.env` file as in `.env.example`.

Example:

```env
DB_URL=jdbc:postgresql://localhost:5432/KundenDB
DB_USERNAME=postgres
DB_PASSWORD=your_password

AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=eu-north-1
AWS_S3_BUCKET=your-bucket-name
AWS_S3_PREFIX=exports
```

Important:

```text
.env must not be committed to GitHub.
```

Add this to `.gitignore`:

```gitignore
.env
exports/
```

## Run from terminal

```bash
set -a
source .env
set +a

mvn -U clean install
mvn spring-boot:run
```

## Docker

The application can be run together with PostgreSQL using Docker Compose.

```bash
docker compose up --build
```

This starts:

- `postgres` — PostgreSQL 16 database (`KundenDB`)
- `app` — the Spring Boot application, built from the local `Dockerfile`

The `app` container reads its configuration from `.env` (see [Configuration](#configuration)) and mounts the local `exports/` directory, so generated CSV files stay available on the host.

To stop:

```bash
docker compose down
```

## Tests

Run the test suite:

```bash
mvn test
```

Tests do not require a running PostgreSQL instance, Docker, or real AWS credentials:

- `src/test/resources/application.yaml` overrides the datasource with an in-memory H2 database and provides dummy AWS S3 config values, so the Spring context loads in isolation.
- `CsvExportService`, `S3UploadService`, and `DataGenerator` are covered by unit tests (JUnit 5 + Mockito) with mocked repositories and a mocked `S3Client`.

## Notes

- CSV export runs every 3 hours.
- Only new records are exported.
- Existing records are not exported again.
- AWS credentials are used only from environment variables.
- The S3 bucket should not be public.
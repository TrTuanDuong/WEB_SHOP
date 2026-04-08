#!/usr/bin/env bash
set -euo pipefail

# Portable PostgreSQL bootstrap script for this project.
# Usage:
#   DB_USER=your_pg_user PGPASSWORD=your_pg_password ./setup_db_portable.sh
# Optional variables:
#   DB_NAME (default: btl_web)
#   DB_HOST (default: localhost)
#   DB_PORT (default: 5432)
#   SCHEMA_FILE (default: src/main/resources/schema.sql)

DB_NAME="${DB_NAME:-btl_web}"
DB_USER="${DB_USER:-$USER}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
SCHEMA_FILE="${SCHEMA_FILE:-src/main/resources/schema.sql}"

if ! command -v psql >/dev/null 2>&1; then
  echo "Error: psql is not installed or not in PATH." >&2
  exit 1
fi

if ! command -v createdb >/dev/null 2>&1; then
  echo "Error: createdb is not installed or not in PATH." >&2
  exit 1
fi

if [[ ! -f "$SCHEMA_FILE" ]]; then
  echo "Error: schema file not found: $SCHEMA_FILE" >&2
  exit 1
fi

echo "Checking database '$DB_NAME' on $DB_HOST:$DB_PORT with user '$DB_USER'..."
DB_EXISTS="$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'")"

if [[ "$DB_EXISTS" != "1" ]]; then
  echo "Database '$DB_NAME' does not exist. Creating..."
  createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
else
  echo "Database '$DB_NAME' already exists."
fi

echo "Applying schema/data from $SCHEMA_FILE..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SCHEMA_FILE"

echo "Database bootstrap completed successfully."

echo
echo "Use these environment variables when running Tomcat:"
echo "export DB_URL=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
echo "export DB_USER=$DB_USER"
echo "export DB_PASSWORD=<your_password_or_empty>"

echo
echo "Quick check:"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT current_database() AS db_name, current_user AS db_user;"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) AS shop_products FROM shop_product;"

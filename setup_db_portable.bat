@echo off
setlocal enabledelayedexpansion

REM Portable PostgreSQL bootstrap script for Windows/NetBeans.
REM Usage:
REM   set DB_USER=postgres
REM   set PGPASSWORD=your_password
REM   setup_db_portable.bat
REM Optional variables:
REM   DB_NAME (default: btl_web)
REM   DB_HOST (default: localhost)
REM   DB_PORT (default: 5432)
REM   SCHEMA_FILE (default: src\main\resources\schema.sql)

if "%DB_NAME%"=="" set "DB_NAME=btl_web"
if "%DB_USER%"=="" set "DB_USER=%USERNAME%"
if "%DB_HOST%"=="" set "DB_HOST=localhost"
if "%DB_PORT%"=="" set "DB_PORT=5432"
if "%SCHEMA_FILE%"=="" set "SCHEMA_FILE=src\main\resources\schema.sql"

where psql >nul 2>nul
if errorlevel 1 (
  echo Error: psql is not installed or not in PATH.
  exit /b 1
)

where createdb >nul 2>nul
if errorlevel 1 (
  echo Error: createdb is not installed or not in PATH.
  exit /b 1
)

if not exist "%SCHEMA_FILE%" (
  echo Error: schema file not found: %SCHEMA_FILE%
  exit /b 1
)

echo Checking database '%DB_NAME%' on %DB_HOST%:%DB_PORT% with user '%DB_USER%'...
set "DB_EXISTS="
for /f "usebackq delims=" %%i in (`psql -h "%DB_HOST%" -p "%DB_PORT%" -U "%DB_USER%" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='%DB_NAME%'"`) do set "DB_EXISTS=%%i"
set "DB_EXISTS=%DB_EXISTS: =%"

if not "%DB_EXISTS%"=="1" (
  echo Database '%DB_NAME%' does not exist. Creating...
  createdb -h "%DB_HOST%" -p "%DB_PORT%" -U "%DB_USER%" "%DB_NAME%"
  if errorlevel 1 exit /b 1
) else (
  echo Database '%DB_NAME%' already exists.
)

echo Applying schema/data from %SCHEMA_FILE%...
psql -h "%DB_HOST%" -p "%DB_PORT%" -U "%DB_USER%" -d "%DB_NAME%" -f "%SCHEMA_FILE%"
if errorlevel 1 exit /b 1

echo.
echo Database bootstrap completed successfully.
echo.
echo Use these environment variables when running Tomcat:
echo set DB_URL=jdbc:postgresql://%DB_HOST%:%DB_PORT%/%DB_NAME%
echo set DB_USER=%DB_USER%
echo set DB_PASSWORD=^<your_password_or_empty^>
echo.
echo Quick check:
psql -h "%DB_HOST%" -p "%DB_PORT%" -U "%DB_USER%" -d "%DB_NAME%" -c "SELECT current_database() AS db_name, current_user AS db_user;"
psql -h "%DB_HOST%" -p "%DB_PORT%" -U "%DB_USER%" -d "%DB_NAME%" -c "SELECT COUNT(*) AS shop_products FROM shop_product;"

exit /b 0

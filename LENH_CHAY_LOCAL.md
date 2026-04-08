# Lenh chay local

## 0a) Chuan bi DB tren may moi (khuyen dung)

```bash
cd "/duong/dan/toi/BTL_WEB"
DB_USER=postgres PGPASSWORD=your_password ./setup_db_portable.sh
```

Script se tu tao database `btl_web`, import schema + du lieu san pham, sau do in ra cac bien moi truong JDBC can dung.

## 0) Chuan bi PostgreSQL (chay 1 lan)

```bash
createdb btl_web
psql -d btl_web -f src/main/resources/schema.sql
```

Neu chua tao user hoac mat khau khac, sua lai DB_USER/DB_PASSWORD o buoc 1.

## 1) Set Tomcat (chi can lam 1 lan moi terminal)

```bash
export TOMCAT_HOME="/Users/trantuanduong/Downloads/apache-tomcat-9.0.105"
export DB_URL="jdbc:postgresql://localhost:5432/btl_web"
export DB_USER="postgres"
export DB_PASSWORD="postgres"
```

Neu Tomcat cua ban nam o duong dan khac thi sua lai gia tri TOMCAT_HOME.

## 2) Chay project (vao trang san pham)

```bash
cd "/Users/trantuanduong/Documents/CNTT_PTIT/Hoc ky 6/WEB/BTL_WEB"
mvn clean package
cp target/BTL_WEB.war "$TOMCAT_HOME/webapps/BTL_WEB.war"
"$TOMCAT_HOME/bin/startup.sh"
open "http://localhost:8080/BTL_WEB/"

```

## 3) Sau khi sua code, cap nhat lai ban moi

```bash
export TOMCAT_HOME="/Users/trantuanduong/Downloads/Các file cài đặt/apache-tomcat-9.0.115"
cd "/Users/trantuanduong/Documents/CNTT_PTIT/Học kỳ 6/WEB/BTL_WEB"
"$TOMCAT_HOME/bin/shutdown.sh" || true
rm -rf "$TOMCAT_HOME/webapps/BTL_WEB" "$TOMCAT_HOME/webapps/BTL_WEB.war"
mvn clean package
cp target/BTL_WEB.war "$TOMCAT_HOME/webapps/BTL_WEB.war"
"$TOMCAT_HOME/bin/startup.sh"
open "http://localhost:8080/BTL_WEB/"
```

## 4) Tat server

```bash
"$TOMCAT_HOME/bin/shutdown.sh"
```

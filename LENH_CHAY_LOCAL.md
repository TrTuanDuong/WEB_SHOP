# Lệnh chạy local

## 0a) Chuẩn bị DB trên máy mới (khuyến dùng)

```bash
cd "/duong/dan/toi/BTL_WEB"
DB_USER=postgres PGPASSWORD=your_password ./setup_db_portable.sh
```

Script sẽ tự tạo database `btl_web`, import schema + dữ liệu sản phẩm, sau đó in ra các biến môi trường JDBC cần dùng.

## 0) Chuẩn bị PostgreSQL (chạy 1 lần)

```bash
createdb btl_web
psql -d btl_web -f src/main/resources/schema.sql
```

Nếu chưa tạo user hoặc mật khẩu khác, sửa lại `DB_USER`/`DB_PASSWORD` ở bước 1.

## 1) Set Tomcat (chỉ cần làm 1 lần mỗi terminal)

```bash
export TOMCAT_HOME="/Users/trantuanduong/Downloads/apache-tomcat-9.0.105"
export DB_URL="jdbc:postgresql://localhost:5432/btl_web"
export DB_USER="postgres"
export DB_PASSWORD="postgres"
```

Nếu Tomcat của bạn nằm ở đường dẫn khác thì sửa lại giá trị `TOMCAT_HOME`.

## 2) Chạy project (vào trang sản phẩm)

```bash
cd "/Users/trantuanduong/Documents/CNTT_PTIT/Hoc ky 6/WEB/BTL_WEB"
mvn clean package
cp target/BTL_WEB.war "$TOMCAT_HOME/webapps/BTL_WEB.war"
"$TOMCAT_HOME/bin/startup.sh"
open "http://localhost:8080/BTL_WEB/"
```

## 3) Sau khi sửa code, cập nhật lại bản mới

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

## 4) Tắt server

```bash
"$TOMCAT_HOME/bin/shutdown.sh"
```

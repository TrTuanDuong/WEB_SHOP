# BTL WEB - Web Ban Quan Ao

Ung dung duoc xay dung bang Java Servlet + JSP voi luong 2 buoc:

1. Nhap thong tin san pham quan ao.
2. Xac nhan thong tin truoc khi luu.

## Chuc nang chinh

- Them san pham quan ao voi cac truong: ma san pham, ten, loai, size, mau, gia, ton kho.
- Kiem tra du lieu dau vao va thong bao loi ro rang.
- Kiem tra trung ma san pham.
- Xac nhan truoc khi luu hoac quay lai chinh sua.
- Hien thi danh sach san pham da them ngay tai trang chinh.

## Chay du an

```bash
mvn clean package
```

### Yeu cau

- JDK 21
- Maven 3.9+
- Tomcat 9 (vi du: `apache-tomcat-9.x`)

### Build WAR

```bash
mvn clean package
```

Sau khi build thanh cong se tao file:

```bash
target/BTL_WEB.war
```

### Chay local bang Tomcat

```bash
cp target/BTL_WEB.war /duong-dan-toi-tomcat/webapps/
/duong-dan-toi-tomcat/bin/startup.sh
```

Mo trinh duyet:

```text
http://localhost:8080/BTL_WEB
```

Dung server:

```bash
/duong-dan-toi-tomcat/bin/shutdown.sh
```

Deploy file WAR trong thu muc `target/` len server Servlet (Tomcat, Jetty, ...).

# WEB_SHOP

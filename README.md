# 🛒 E-commerce C2C Microservices Platform

Một hệ thống thương mại điện tử mô hình **C2C (Consumer-to-Consumer)** được xây dựng theo kiến trúc **Microservices**, sử dụng **Spring Boot, WebFlux, MongoDB, PostgreSQL, Kafka, Redis, Elasticsearch, Docker**, và các nguyên tắc **event-driven architecture** nhằm đảm bảo hiệu năng, khả năng mở rộng và tính chịu lỗi cao.

---

## 🚀 1. Tổng quan dự án
Dự án mô phỏng một nền tảng thương mại điện tử C2C nơi người bán (seller) có thể đăng sản phẩm, quản lý kho và nhận thanh toán; người mua có thể tìm kiếm, đặt hàng và thanh toán.

Hệ thống sử dụng kiến trúc microservices kết nối qua Kafka để đảm bảo **eventual consistency**, **khả năng mở rộng độc lập**, và **tách biệt domain** rõ ràng.

---

## 🧩 2. Danh sách Microservices
Dự án bao gồm các service sau:

### **0. config-server**
- Cung cấp cấu hình tập trung (Spring Cloud Config)
- Quản lý cấu hình theo profile (dev/staging/prod)
- Hỗ trợ reload cấu hình cho các service

### **1. discovery-server**
- Đăng ký và quản lý service registry (Eureka/Consul)
- Hỗ trợ load balancing và tự khám phá dịch vụ

### **2. gateway-api**
- API Gateway (route, auth, rate-limit, routing to services)
- Thực hiện authentication (JWT validation) trước khi chuyển request
- Có thể thực hiện caching, circuit-breaker, throttling

### **3. identity-service**
- Đăng ký, đăng nhập, quản lý tài khoản
- Phát hành JWT
- Quản lý seller & buyer

### **4. product-service**
- CRUD sản phẩm
- Quản lý thông tin sản phẩm: giá, mô tả, danh mục
- Phát hành event `PRODUCT_UPDATED`

### **5. inventory-service**
- Quản lý tồn kho
- Hold/Release sản phẩm trong quá trình đặt hàng
- Giảm tồn kho khi thanh toán thành công

### **6. order-service**
- Tạo đơn hàng
- Validate sản phẩm và tồn kho
- Lưu order lifecycle
- Nhận event thanh toán để cập nhật trạng thái

### **7. payment-service**
- Tạo phiên thanh toán
- Tích hợp cổng thanh toán (Momo/ZaloPay/Stripe)
- Nhận callback và đẩy event `PAYMENT_SUCCESS/FAILED`

### **8. noti-service**
- Gửi email/push notification
- Lắng nghe tất cả sự kiện order/payment

### **9. search-service**
- Index sản phẩm lên Elasticsearch
- Full-text search
- Tự động cập nhật khi sản phẩm thay đổi

### **10. seller-management-service (Seller Onboarding & Management)**
- **Đăng ký nhà cung cấp (seller onboarding)**: thu thập thông tin doanh nghiệp/cá nhân, hợp đồng, xác thực giấy tờ
- Quản lý tài khoản seller, cấu hình cửa hàng, lịch sử bán hàng, dashboard cơ bản
- Quản lý trạng thái xử lý đơn hàng từ phía seller (chấp nhận, chuẩn bị, gửi)
- Phát event liên quan seller (seller.created, seller.updated, seller.payout)

### **11. sync-service**
- Đồng bộ dữ liệu giữa các service
- Fanout các event Kafka sang nhiều service
- Retry, đảm bảo idempotency
- Giúp hệ thống đạt **eventual consistency**

---

## 🔗 3. Kiến trúc tổng thể
Hệ thống sử dụng:
- **API Gateway** để routing request
- **Config Server** để quản lý cấu hình tập trung
- **Discovery Server** để tìm kiếm service
- **Kafka** cho truyền thông bất đồng bộ
- **Redis** cho cache
- **MongoDB/PostgreSQL** cho lưu trữ dữ liệu
- **Docker** để chạy độc lập

Luồng chính của một giao dịch:
```
User → Gateway → Search → View product → Order → Hold stock
Kafka: ORDER_CREATED → Payment pending
Callback → PAYMENT_SUCCESS
Kafka: PAYMENT_SUCCESS
Order update → Inventory update → Noti → Seller update
Shipment update → Delivered → Completed
```

---

## 🔄 4. Luồng hoạt động chính

### **1. Người dùng tìm kiếm sản phẩm**
- User → Gateway → search-service truy vấn Elasticsearch
- Trả về danh sách sản phẩm

### **2. Người dùng đặt hàng**
- User → Gateway → order-service tạo đơn → emit event `ORDER_CREATED`
- inventory-service giữ hàng
- payment-service tạo phiên thanh toán

### **3. Thanh toán thành công**
- payment-service emit `PAYMENT_SUCCESS`
- order-service cập nhật trạng thái
- inventory giảm tồn kho
- seller-management cập nhật doanh thu
- noti-service gửi thông báo
- sync-service ghi log & đồng bộ dữ liệu

### **4. Giao hàng → Hoàn tất đơn**
- seller-management-service cập nhật shipment → event `ORDER_SHIPMENT_UPDATED`
- buyer nhận hàng → event `ORDER_DELIVERED`
- order completed

---

## 🧱 5. Công nghệ sử dụng
- **Java 8**
- **Spring Boot WebFlux** (Reactive)
- **Spring Security + JWT**
- **Spring Cloud Config (config-server)**
- **API Gateway (Spring Cloud Gateway / Kong / Nginx)**
- **Discovery (Eureka/Consul)**
- **Kafka** (event-driven)
- **MongoDB, PostgreSQL**
- **Redis** (cache)
- **Elasticsearch** (search engine)
- **Docker & Docker Compose / Kubernetes**

---

## 🧪 6. Testing
Bao gồm:
- Unit Test (JUnit + Mockito)
- Integration Test (TestContainers)
- Stress test với Gatling/JMeter

---

## 🎯 7. Mục tiêu của dự án
- Mô phỏng hệ thống thương mại điện tử hiện đại
- Hiểu và triển khai kiến trúc microservices thực tế
- Nắm được cách hoạt động event-driven với Kafka
- Áp dụng chiến lược eventual consistency và idempotency

---

## 📝 8. Hướng phát triển
- Realtime notification (WebSocket)
- Chat giữa buyer và seller
- Recommendation system (ML)
- Admin dashboard
- Anti-fraud service

---

## 👨‍💻 9. Tác giả
Dự án được xây dựng nhằm hỗ trợ học tập, phỏng vấn và mô phỏng kiến trúc thực tế của hệ thống thương mại điện tử.


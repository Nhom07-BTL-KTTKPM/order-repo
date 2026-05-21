 # Tài liệu REST API Module Voucher

 ## Mục lục

 - [1. Tổng quan](#1-tổng-quan)
 - [2. Quy ước chung](#2-quy-ước-chung)
 - [3. Mô hình dữ liệu và DTO](#3-mô-hình-dữ-liệu-và-dto)
 - [4. Voucher APIs](#4-voucher-apis)
	 - [4.1 Lấy danh sách voucher](#41-lấy-danh-sách-voucher)
	 - [4.2 Lấy voucher theo id](#42-lấy-voucher-theo-id)
	 - [4.3 Lấy voucher theo code](#43-lấy-voucher-theo-code)
	 - [4.4 Tạo voucher](#44-tạo-voucher)
	 - [4.5 Cập nhật voucher](#45-cập-nhật-voucher)
	 - [4.6 Xóa voucher](#46-xóa-voucher)
	 - [4.7 Thay đổi trạng thái voucher](#47-thay-đổi-trạng-thái-voucher)
	 - [4.8 Validate voucher](#48-validate-voucher)
	 - [4.9 Redeem voucher](#49-redeem-voucher)
 - [5. Voucher Redemption APIs](#5-voucher-redemption-apis)
	 - [5.1 Lấy danh sách redemption](#51-lấy-danh-sách-redemption)
	 - [5.2 Lấy redemption theo customerId](#52-lấy-redemption-theo-customerid)
	 - [5.3 Lấy redemption theo voucherId](#53-lấy-redemption-theo-voucherid)
 - [6. Ví dụ nghiệp vụ theo từng loại voucher](#6-ví-dụ-nghiệp-vụ-theo-từng-loại-voucher)
 - [7. Lỗi validation và lỗi nghiệp vụ](#7-lỗi-validation-và-lỗi-nghiệp-vụ)
 - [8. Mã lỗi phổ biến](#8-mã-lỗi-phổ-biến)
 - [9. Flow áp dụng voucher khi checkout](#9-flow-áp-dụng-voucher-khi-checkout)
 - [10. Quy tắc transaction khi redeem voucher](#10-quy-tắc-transaction-khi-redeem-voucher)

 ## 1. Tổng quan

 Module Voucher là một thành phần của kiến trúc Spring Boot Microservice, chịu trách nhiệm quản lý mã giảm giá và lịch sử sử dụng voucher trong quá trình checkout.

 Tài liệu này chuẩn hóa contract theo base path sau:

 - Base path Voucher: `/api/v1/orders/vouchers`
 - Base path Voucher Redemption: `/api/v1/orders/voucher-redemptions`

 Lưu ý triển khai: trong source hiện tại, controller đang map theo đường dẫn nội bộ khác. Tài liệu này mô tả contract chuẩn cho API công khai/gateway và nghiệp vụ mong muốn của module.

 ### Đặc điểm phản hồi

 - Response thành công trả thẳng DTO hoặc danh sách DTO.
 - Response lỗi dùng cấu trúc `ErrorMessage`.
 - Các giá trị tiền tệ được chuẩn hóa theo quy tắc làm tròn `BigDecimal scale = 0` với `RoundingMode.HALF_UP` trong logic nghiệp vụ.

 ## 2. Quy ước chung

 ### 2.1 Request Headers dùng chung

 | Header | Bắt buộc | Mô tả |
 |---|---:|---|
 | `Content-Type: application/json` | Có | Định dạng request body JSON |
 | `Accept: application/json` | Có | Yêu cầu phản hồi JSON |
 | `Authorization: Bearer <token>` | Tùy hệ thống | Dùng khi API được bảo vệ qua gateway hoặc security layer |

 ### 2.2 Cấu trúc lỗi chuẩn

 ```json
 {
	 "statusCode": 400,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Giá trị đơn hàng không được để trống",
	 "description": "Validation failed"
 }
 ```

 ### 2.3 HTTP status thường dùng

 | Mã | Ý nghĩa |
 |---|---|
 | `200 OK` | Thành công |
 | `201 Created` | Tạo mới thành công |
 | `204 No Content` | Xóa thành công |
 | `400 Bad Request` | Dữ liệu đầu vào không hợp lệ hoặc lỗi nghiệp vụ |
 | `404 Not Found` | Không tìm thấy tài nguyên |
 | `500 Internal Server Error` | Lỗi hệ thống không dự đoán trước |

 ## 3. Mô hình dữ liệu và DTO

 ### 3.1 Enum nghiệp vụ

 #### `VoucherType`

 | Giá trị | Ý nghĩa |
 |---|---|
 | `PERCENT` | Giảm theo phần trăm trên giá trị đơn hàng |
 | `AMOUNT` | Giảm trừ một số tiền cố định |
 | `FREE_SHIPPING` | Hỗ trợ hoặc miễn phí vận chuyển |

 #### `VoucherStatus`

 | Giá trị | Ý nghĩa |
 |---|---|
 | `UPCOMING` | Voucher chưa đến thời điểm hiệu lực |
 | `ACTIVE` | Voucher đang có hiệu lực |
 | `EXPIRED` | Voucher đã hết hiệu lực hoặc hết số lượng |
 | `DISABLED` | Voucher bị vô hiệu hóa |

 ### 3.2 DTO: `VoucherRequestDTO`

 Dùng cho API tạo mới và cập nhật voucher.

 | Field | Kiểu dữ liệu | Bắt buộc | Ràng buộc | Mô tả |
 |---|---|---:|---|---|
 | `code` | string | Có | Không rỗng, tối đa 100 ký tự, regex `^[A-Z0-9_-]+$` | Mã voucher, tự chuẩn hóa chữ in hoa |
 | `name` | string | Có | Không rỗng, tối đa 255 ký tự | Tên chương trình |
 | `description` | string | Không | Tối đa 2000 ký tự | Mô tả chi tiết |
 | `type` | string | Có | Một trong `PERCENT`, `AMOUNT`, `FREE_SHIPPING` | Loại voucher |
 | `discountValue` | number | Có | Lớn hơn 0 | Giá trị giảm |
 | `maxDiscountAmount` | number | Không | Lớn hơn hoặc bằng 0 | Mức giảm tối đa, đặc biệt cho voucher phần trăm |
 | `minOrderAmount` | number | Có | Lớn hơn hoặc bằng 0 | Đơn hàng tối thiểu để áp dụng |
 | `quantity` | integer | Có | Lớn hơn 0 | Số lượng còn lại |
 | `maxUsagePerUser` | integer | Không | Lớn hơn 0 | Số lần tối đa trên mỗi khách hàng |
 | `status` | string | Có | Một trong `UPCOMING`, `ACTIVE`, `EXPIRED`, `DISABLED` | Trạng thái voucher |
 | `startDate` | datetime | Có | Không rỗng | Ngày bắt đầu hiệu lực |
 | `endDate` | datetime | Có | Không rỗng, lớn hơn `startDate` | Ngày kết thúc hiệu lực |

 ### 3.3 DTO: `VoucherResponseDTO`

 Dùng cho response của các API CRUD voucher.

 | Field | Kiểu dữ liệu | Mô tả |
 |---|---|---|
 | `id` | UUID | Định danh voucher |
 | `code` | string | Mã voucher |
 | `name` | string | Tên voucher |
 | `description` | string | Mô tả |
 | `type` | string | Loại voucher |
 | `discountValue` | number | Giá trị giảm |
 | `maxDiscountAmount` | number | Mức giảm tối đa |
 | `minOrderAmount` | number | Đơn hàng tối thiểu |
 | `quantity` | integer | Số lượng còn lại |
 | `maxUsagePerUser` | integer | Giới hạn sử dụng theo người dùng |
 | `status` | string | Trạng thái hiện tại |
 | `startDate` | datetime | Ngày bắt đầu |
 | `endDate` | datetime | Ngày kết thúc |
 | `createdAt` | datetime | Thời điểm tạo |

 ### 3.4 DTO: `VoucherStatusChangeRequestDTO`

 | Field | Kiểu dữ liệu | Bắt buộc | Mô tả |
 |---|---|---:|---|
 | `status` | string | Có | Trạng thái mới của voucher |

 ### 3.5 DTO: `VoucherValidationRequestDTO`

 | Field | Kiểu dữ liệu | Bắt buộc | Ràng buộc | Mô tả |
 |---|---|---:|---|---|
 | `customerId` | UUID | Không | - | Khách hàng dùng để kiểm tra giới hạn theo người dùng |
 | `orderAmount` | number | Có | Lớn hơn hoặc bằng 0 | Tổng tiền hàng trước khi giảm |
 | `shippingFee` | number | Không | Lớn hơn hoặc bằng 0 | Phí vận chuyển gốc |

 ### 3.6 DTO: `VoucherValidationResponseDTO`

 | Field | Kiểu dữ liệu | Mô tả |
 |---|---|---|
 | `voucherId` | UUID | Định danh voucher được kiểm tra |
 | `code` | string | Mã voucher |
 | `valid` | boolean | Kết quả hợp lệ hay không |
 | `message` | string | Lý do hợp lệ hoặc không hợp lệ |
 | `discountAmount` | number | Số tiền dự kiến được giảm |
 | `remainingQuantity` | integer | Số lượng voucher còn lại |

 ### 3.7 DTO: `VoucherRedemptionResponseDTO`

 | Field | Kiểu dữ liệu | Mô tả |
 |---|---|---|
 | `id` | UUID | Định danh lịch sử sử dụng |
 | `voucherId` | UUID | Voucher đã được áp dụng |
 | `customerId` | UUID | Khách hàng đã sử dụng |
 | `orderId` | UUID | Đơn hàng đã áp dụng |
 | `amountDiscounted` | number | Số tiền thực tế đã giảm |
 | `redeemedAt` | datetime | Thời điểm redeem |

 ### 3.8 Cấu trúc lỗi `ErrorMessage`

 | Field | Kiểu dữ liệu | Mô tả |
 |---|---|---|
 | `statusCode` | integer | Mã trạng thái HTTP |
 | `timestamp` | datetime | Thời điểm phát sinh lỗi |
 | `message` | string | Thông điệp lỗi chính |
 | `description` | string | Mô tả ngữ cảnh lỗi |

 ## 4. Voucher APIs

 ### 4.1 Lấy danh sách voucher

 **HTTP Method:** `GET`  
 **URL:** `/api/v1/orders/voucher`

 **Mô tả nghiệp vụ**  
 Trả về toàn bộ danh sách voucher trong hệ thống, thường dùng cho trang quản trị, đồng bộ dữ liệu hoặc tra cứu nhanh.

 **Request Headers**  
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu hệ thống có bảo vệ

 **Path Variables:** Không có  
 **Query Parameters:** Không có  
 **Request Body:** Không có

 **Validation rules**
 - Không có input từ client.
 - Dữ liệu trả về được sắp xếp theo thời gian tạo giảm dần.

 **HTTP Status Codes**
 - `200 OK`

 **Response thành công**

 ```json
 [
	 {
        "id": "c5452312-20e5-4aa1-a04c-e20da0d9d855",
        "code": "TEST-PERCENT-ACTIVE",
        "name": "Voucher giảm theo phần trăm đang hoạt động",
        "description": "Voucher kiểm thử cho loại PERCENT với trạng thái ACTIVE",
        "type": "PERCENT",
        "discountValue": 10.00,
        "maxDiscountAmount": 50000.00,
        "minOrderAmount": 100000.00,
        "quantity": 100,
        "maxUsagePerUser": 1,
        "status": "ACTIVE",
        "startDate": "2026-05-20T09:57:58.3014",
        "endDate": "2026-11-21T09:57:58.3014",
        "createdAt": "2026-05-21T09:57:58.3014"
    },
 ]
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 500,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Unexpected error message",
	 "description": "Unexpected server error"
 }
 ```

 ### 4.2 Lấy voucher theo id

 **HTTP Method:** `GET`  
 **URL:** `/api/v1/orders/vouchers/{id}`

 **Mô tả nghiệp vụ**  
 Lấy chi tiết một voucher theo định danh UUID.

 **Request Headers**
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables**

 | Field | Kiểu dữ liệu | Bắt buộc | Mô tả |
 |---|---|---:|---|
 | `id` | UUID | Có | Định danh voucher |

 **Query Parameters:** Không có  
 **Request Body:** Không có

 **Validation rules**
 - `id` phải là UUID hợp lệ.
 - Nếu không tồn tại voucher, trả về lỗi 404.

 **HTTP Status Codes**
 - `200 OK`
 - `404 Not Found`

 **Response thành công**

 ```json
 {
	 "id": "a3d9d3f7-2b65-4d63-a8a0-61fbf8b8aa01",
	 "code": "TEST-PERCENT-ACTIVE",
	 "name": "Voucher giảm theo phần trăm đang hoạt động",
	 "description": "Voucher kiểm thử cho loại PERCENT với trạng thái ACTIVE",
	 "type": "PERCENT",
	 "discountValue": 10,
	 "maxDiscountAmount": 50000,
	 "minOrderAmount": 100000,
	 "quantity": 100,
	 "maxUsagePerUser": 1,
	 "status": "ACTIVE",
	 "startDate": "2026-05-20T08:00:00",
	 "endDate": "2026-11-21T23:59:59",
	 "createdAt": "2026-05-21T08:00:00"
 }
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 404,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Không tìm thấy voucher",
	 "description": "Resource not found"
 }
 ```

 ### 4.3 Lấy voucher theo code

 **HTTP Method:** `GET`  
 **URL:** `/api/v1/vouchers/code/{code}`

 **Mô tả nghiệp vụ**  
 Tra cứu voucher theo mã code đã chuẩn hóa chữ in hoa.

 **Request Headers**
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables**

 | Field | Kiểu dữ liệu | Bắt buộc | Mô tả |
 |---|---|---:|---|
 | `code` | string | Có | Mã voucher |

 **Query Parameters:** Không có  
 **Request Body:** Không có

 **Validation rules**
 - `code` không được để trống.
 - Hệ thống tự trim và chuyển sang chữ in hoa.
 - Nếu không tìm thấy voucher, trả về 404.

 **HTTP Status Codes**
 - `200 OK`
 - `404 Not Found`

 **Response thành công**

 ```json
 {
	 "id": "d1a51d7c-8e1b-437f-9d3a-8a8f0b5f4b11",
	 "code": "TEST-AMOUNT-ACTIVE",
	 "name": "Voucher giảm số tiền cố định đang hoạt động",
	 "description": "Voucher kiểm thử cho loại AMOUNT với trạng thái ACTIVE",
	 "type": "AMOUNT",
	 "discountValue": 20000,
	 "maxDiscountAmount": 20000,
	 "minOrderAmount": 50000,
	 "quantity": 50,
	 "maxUsagePerUser": null,
	 "status": "ACTIVE",
	 "startDate": "2026-05-20T08:00:00",
	 "endDate": "2026-11-21T23:59:59",
	 "createdAt": "2026-05-21T08:00:00"
 }
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 404,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Không tìm thấy voucher",
	 "description": "Resource not found"
 }
 ```

 ### 4.4 Tạo voucher

 **HTTP Method:** `POST`  
 **URL:** `/api/v1/orders/vouchers`

 **Mô tả nghiệp vụ**  
 Tạo mới một voucher trong hệ thống với đầy đủ cấu hình về loại giảm giá, thời gian hiệu lực, số lượng và trạng thái ban đầu.

 **Request Headers**
 - `Content-Type: application/json`
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables:** Không có  
 **Query Parameters:** Không có

 **Request Body**

 ```json
 {
	 "code": "SUMMER10",
	 "name": "Giảm giá mùa hè 10%",
	 "description": "Áp dụng cho đơn hàng từ 100.000đ",
	 "type": "PERCENT",
	 "discountValue": 10,
	 "maxDiscountAmount": 50000,
	 "minOrderAmount": 100000,
	 "quantity": 1000,
	 "maxUsagePerUser": 1,
	 "status": "ACTIVE",
	 "startDate": "2026-06-01T00:00:00",
	 "endDate": "2026-08-31T23:59:59"
 }
 ```

 **Validation rules**
 - `code` bắt buộc, viết hoa, không chứa khoảng trắng, chỉ gồm chữ in hoa, số, `_`, `-`.
 - `name` không được trống.
 - `type` bắt buộc.
 - `discountValue` phải lớn hơn 0.
 - `minOrderAmount` phải lớn hơn hoặc bằng 0.
 - `quantity` phải lớn hơn 0.
 - `maxUsagePerUser`, nếu có, phải lớn hơn 0.
 - `status` bắt buộc.
 - `startDate` và `endDate` bắt buộc, `endDate` phải lớn hơn `startDate`.
 - `code` không được trùng với voucher khác.
 - Dữ liệu tiền tệ được làm tròn theo quy tắc `scale = 0`, `RoundingMode.HALF_UP`.

 **HTTP Status Codes**
 - `201 Created`
 - `400 Bad Request`

 **Response thành công**

 ```json
{
    "id": "8aa14053-b0fa-4de9-a5de-c48422e1b088",
    "code": "SUMMER10",
    "name": "Giảm giá mùa hè 10%",
    "description": "Áp dụng cho đơn hàng từ 100.000đ",
    "type": "PERCENT",
    "discountValue": 10,
    "maxDiscountAmount": 50000,
    "minOrderAmount": 100000,
    "quantity": 1000,
    "maxUsagePerUser": 1,
    "status": "ACTIVE",
    "startDate": "2026-06-01T00:00:00",
    "endDate": "2026-08-31T23:59:59",
    "createdAt": "2026-05-21T10:37:48.3162537"
}
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 400,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "code: Mã voucher phải ở dạng chữ in hoa, số, dấu gạch dưới hoặc gạch ngang",
	 "description": "Validation failed"
 }
 ```

 ### 4.5 Cập nhật voucher

 **HTTP Method:** `PUT`  
 **URL:** `/api/v1/orders/vouchers/{id}`

 **Mô tả nghiệp vụ**  
 Cập nhật toàn bộ thông tin voucher theo định danh.

 **Request Headers**
 - `Content-Type: application/json`
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables**

 | Field | Kiểu dữ liệu | Bắt buộc | Mô tả |
 |---|---|---:|---|
 | `id` | UUID | Có | Định danh voucher |

 **Query Parameters:** Không có

 **Request Body**

 ```json
 {
	 "code": "SUMMER15",
	 "name": "Giảm giá mùa hè 15%",
	 "description": "Áp dụng cho đơn hàng từ 200.000đ",
	 "type": "PERCENT",
	 "discountValue": 15,
	 "maxDiscountAmount": 75000,
	 "minOrderAmount": 200000,
	 "quantity": 800,
	 "maxUsagePerUser": 2,
	 "status": "ACTIVE",
	 "startDate": "2026-06-01T00:00:00",
	 "endDate": "2026-09-30T23:59:59"
 }
 ```

 **Validation rules**
 - Tương tự API tạo mới.
 - `id` phải tồn tại.
 - `code` không được trùng với voucher khác ngoài bản ghi hiện tại.

 **HTTP Status Codes**
 - `200 OK`
 - `400 Bad Request`
 - `404 Not Found`

 **Response thành công**

 ```json
 {
	 "id": "f4d7c1fd-1b0f-4ec1-9d2b-5e8bd2e8d6f7",
	 "code": "SUMMER15",
	 "name": "Giảm giá mùa hè 15%",
	 "description": "Áp dụng cho đơn hàng từ 200.000đ",
	 "type": "PERCENT",
	 "discountValue": 15,
	 "maxDiscountAmount": 75000,
	 "minOrderAmount": 200000,
	 "quantity": 800,
	 "maxUsagePerUser": 2,
	 "status": "ACTIVE",
	 "startDate": "2026-06-01T00:00:00",
	 "endDate": "2026-09-30T23:59:59",
	 "createdAt": "2026-05-21T10:15:30"
 }
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 404,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Không tìm thấy voucher",
	 "description": "Resource not found"
 }
 ```

 ### 4.6 Xóa voucher

 **HTTP Method:** `DELETE`  
 **URL:** `/api/v1/orders/vouchers/{id}`

 **Mô tả nghiệp vụ**  
 Xóa logic voucher khỏi khả năng sử dụng. Theo triển khai hiện tại, hệ thống không xóa cứng mà chuyển trạng thái voucher sang `DISABLED` để bảo toàn lịch sử và phục vụ truy vết.

 **Request Headers**
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables**

 | Field | Kiểu dữ liệu | Bắt buộc | Mô tả |
 |---|---|---:|---|
 | `id` | UUID | Có | Định danh voucher |

 **Query Parameters:** Không có  
 **Request Body:** Không có

 **Validation rules**
 - `id` phải tồn tại.
 - Sau khi xóa, voucher được vô hiệu hóa, không nên tiếp tục dùng trong checkout.

 **HTTP Status Codes**
 - `204 No Content`
 - `404 Not Found`

 **Response thành công**

 Không có body.

 **Response lỗi**

 ```json
 {
	 "statusCode": 404,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Không tìm thấy voucher",
	 "description": "Resource not found"
 }
 ```

 ### 4.7 Thay đổi trạng thái voucher

 **HTTP Method:** `PATCH`  
 **URL:** `/api/v1/orders/vouchers/{id}/status`

 **Mô tả nghiệp vụ**  
 Cập nhật trạng thái voucher mà không cần thay đổi toàn bộ thông tin.

 **Request Headers**
 - `Content-Type: application/json`
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables**

 | Field | Kiểu dữ liệu | Bắt buộc | Mô tả |
 |---|---|---:|---|
 | `id` | UUID | Có | Định danh voucher |

 **Query Parameters:** Không có

 **Request Body**

 ```json
 {
	 "status": "ACTIVE"
 }
 ```

 **Validation rules**
 - `status` không được trống.
 - Giá trị hợp lệ: `UPCOMING`, `ACTIVE`, `EXPIRED`, `DISABLED`.

 **HTTP Status Codes**
 - `200 OK`
 - `400 Bad Request`
 - `404 Not Found`

 **Response thành công**

 ```json
 {
	 "id": "a3d9d3f7-2b65-4d63-a8a0-61fbf8b8aa01",
	 "code": "TEST-PERCENT-ACTIVE",
	 "name": "Voucher giảm theo phần trăm đang hoạt động",
	 "description": "Voucher kiểm thử cho loại PERCENT với trạng thái ACTIVE",
	 "type": "PERCENT",
	 "discountValue": 10,
	 "maxDiscountAmount": 50000,
	 "minOrderAmount": 100000,
	 "quantity": 100,
	 "maxUsagePerUser": 1,
	 "status": "ACTIVE",
	 "startDate": "2026-05-20T08:00:00",
	 "endDate": "2026-11-21T23:59:59",
	 "createdAt": "2026-05-21T08:00:00"
 }
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 400,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Trạng thái mới không được để trống",
	 "description": "Illegal argument"
 }
 ```

 ### 4.8 Validate voucher

 **HTTP Method:** `POST`  
 **URL:** `/api/v1/orders/vouchers/{voucherId}/validate`

 **Mô tả nghiệp vụ**  
 Kiểm tra voucher có hợp lệ để áp dụng cho đơn hàng hay không, trả về số tiền dự kiến được giảm nếu voucher hợp lệ.

 **Request Headers**
 - `Content-Type: application/json`
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables:** Không có  
 **Query Parameters:** Không có

 **Request Body**

 ```json
 {
	 "voucherId": "a3d9d3f7-2b65-4d63-a8a0-61fbf8b8aa01",
	 "customerId": "d5d6f7a8-3c2e-4f1d-9b8a-123456789abc",
	 "orderAmount": 350000,
	 "shippingFee": 25000
 }
 ```

 **Validation rules**
 - `voucherId` phải tồn tại.
 - `orderAmount` không được trống và phải lớn hơn hoặc bằng 0.
 - `shippingFee`, nếu có, phải lớn hơn hoặc bằng 0.
 - `customerId` là tùy chọn, nhưng sẽ cần thiết nếu voucher có giới hạn theo khách hàng.
 - Voucher chỉ hợp lệ khi:
	 - trạng thái là `ACTIVE`
	 - nằm trong khoảng thời gian hiệu lực
	 - còn `quantity`
	 - đơn hàng đạt `minOrderAmount`
	 - chưa vượt số lần dùng theo khách hàng, nếu có giới hạn
	 - với `FREE_SHIPPING`, `shippingFee` phải được cung cấp

 **HTTP Status Codes**
 - `200 OK`
 - `400 Bad Request`
 - `404 Not Found`

 **Response thành công**

 ```json
 {
	 "voucherId": "a3d9d3f7-2b65-4d63-a8a0-61fbf8b8aa01",
	 "code": "TEST-PERCENT-ACTIVE",
	 "valid": true,
	 "message": "Voucher hợp lệ",
	 "discountAmount": 35000,
	 "remainingQuantity": 100
 }
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 400,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Giá trị đơn hàng không được để trống",
	 "description": "Validation failed"
 }
 ```


 ## 5. Voucher Redemption APIs

 ### 5.1 Lấy danh sách redemption

 **HTTP Method:** `GET`  
 **URL:** `/api/v1/orders/voucher-redemptions`

 **Mô tả nghiệp vụ**  
 Trả về toàn bộ lịch sử sử dụng voucher trong hệ thống. API này phù hợp cho màn hình audit, thống kê hoặc đối soát.

 **Request Headers**
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables:** Không có  
 **Query Parameters:** Không có  
 **Request Body:** Không có

 **Validation rules**
 - Không có input từ client.
 - Dữ liệu phản hồi nên được sắp xếp theo thời gian redeem giảm dần nếu có triển khai danh sách đầy đủ.

 **HTTP Status Codes**
 - `200 OK`

 **Response thành công**

 ```json
 [
	 {
		 "id": "c88efb8a-71f3-4f6c-9f70-4d8cc7f2a321",
		 "voucherId": "a3d9d3f7-2b65-4d63-a8a0-61fbf8b8aa01",
		 "customerId": "d5d6f7a8-3c2e-4f1d-9b8a-123456789abc",
		 "orderId": "8f5d8d9d-37d9-4d49-9f70-b8c9c4c0f111",
		 "amountDiscounted": 35000,
		 "redeemedAt": "2026-05-21T10:15:30"
	 }
 ]
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 500,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Unexpected error message",
	 "description": "Unexpected server error"
 }
 ```

 ### 5.2 Lấy redemption theo customerId

 **HTTP Method:** `GET`  
 **URL:** `/api/v1/orders/voucher-redemptions/customer/{customerId}`

 **Mô tả nghiệp vụ**  
 Lấy toàn bộ lịch sử sử dụng voucher của một khách hàng.

 **Request Headers**
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables**

 | Field | Kiểu dữ liệu | Bắt buộc | Mô tả |
 |---|---|---:|---|
 | `customerId` | UUID | Có | Định danh khách hàng |

 **Query Parameters:** Không có  
 **Request Body:** Không có

 **Validation rules**
 - `customerId` phải là UUID hợp lệ.
 - Nếu không có bản ghi nào, có thể trả về mảng rỗng.

 **HTTP Status Codes**
 - `200 OK`
 - `400 Bad Request`

 **Response thành công**

 ```json
 [
	 {
		 "id": "c88efb8a-71f3-4f6c-9f70-4d8cc7f2a321",
		 "voucherId": "a3d9d3f7-2b65-4d63-a8a0-61fbf8b8aa01",
		 "customerId": "d5d6f7a8-3c2e-4f1d-9b8a-123456789abc",
		 "orderId": "8f5d8d9d-37d9-4d49-9f70-b8c9c4c0f111",
		 "amountDiscounted": 35000,
		 "redeemedAt": "2026-05-21T10:15:30"
	 }
 ]
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 400,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "customerId không được để trống",
	 "description": "Illegal argument"
 }
 ```

 ### 5.3 Lấy redemption theo voucherId

 **HTTP Method:** `GET`  
 **URL:** `/api/v1/orders/voucher-redemptions/voucher/{voucherId}`

 **Mô tả nghiệp vụ**  
 Lấy toàn bộ lịch sử sử dụng của một voucher cụ thể để theo dõi số lần sử dụng, hiệu quả chiến dịch và đối soát.

 **Request Headers**
 - `Accept: application/json`
 - `Authorization: Bearer <token>` nếu có bảo vệ

 **Path Variables**

 | Field | Kiểu dữ liệu | Bắt buộc | Mô tả |
 |---|---|---:|---|
 | `voucherId` | UUID | Có | Định danh voucher |

 **Query Parameters:** Không có  
 **Request Body:** Không có

 **Validation rules**
 - `voucherId` phải tồn tại.
 - Nếu voucher không có bản ghi sử dụng, có thể trả về mảng rỗng.

 **HTTP Status Codes**
 - `200 OK`
 - `400 Bad Request`
 - `404 Not Found`

 **Response thành công**

 ```json
 [
	 {
		 "id": "c88efb8a-71f3-4f6c-9f70-4d8cc7f2a321",
		 "voucherId": "a3d9d3f7-2b65-4d63-a8a0-61fbf8b8aa01",
		 "customerId": "d5d6f7a8-3c2e-4f1d-9b8a-123456789abc",
		 "orderId": "8f5d8d9d-37d9-4d49-9f70-b8c9c4c0f111",
		 "amountDiscounted": 35000,
		 "redeemedAt": "2026-05-21T10:15:30"
	 }
 ]
 ```

 **Response lỗi**

 ```json
 {
	 "statusCode": 404,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "Không tìm thấy voucher",
	 "description": "Resource not found"
 }
 ```

 ## 6. Ví dụ nghiệp vụ theo từng loại voucher

 ### 6.1 Voucher PERCENT

 **Công thức**

 ```text
 amountDiscounted = min((orderAmount * discountValue) / 100, maxDiscountAmount)
 ```

 **Ví dụ**

 - `orderAmount = 350000`
 - `discountValue = 10`
 - `maxDiscountAmount = 50000`

 Tính toán:

 - Giảm theo phần trăm = `350000 * 10 / 100 = 35000`
 - Vì `35000 < 50000`, số tiền giảm thực tế = `35000`

 **Response validate thực tế**

 ```json
 {
	 "voucherId": "a3d9d3f7-2b65-4d63-a8a0-61fbf8b8aa01",
	 "code": "TEST-PERCENT-ACTIVE",
	 "valid": true,
	 "message": "Voucher hợp lệ",
	 "discountAmount": 35000,
	 "remainingQuantity": 100
 }
 ```

 ### 6.2 Voucher AMOUNT

 **Công thức**

 ```text
 amountDiscounted = min(discountValue, orderAmount)
 ```

 **Ví dụ**

 - `orderAmount = 120000`
 - `discountValue = 20000`

 Tính toán:

 - Số tiền giảm thực tế = `min(20000, 120000) = 20000`

 **Response validate thực tế**

 ```json
 {
	 "voucherId": "d1a51d7c-8e1b-437f-9d3a-8a8f0b5f4b11",
	 "code": "TEST-AMOUNT-ACTIVE",
	 "valid": true,
	 "message": "Voucher hợp lệ",
	 "discountAmount": 20000,
	 "remainingQuantity": 50
 }
 ```

 ### 6.3 Voucher FREE_SHIPPING

 **Công thức**

 ```text
 amountDiscounted = min(discountValue, shippingFee)
 ```

 **Ví dụ**

 - `shippingFee = 15000`
 - `discountValue = 20000`

 Tính toán:

 - Số tiền giảm thực tế = `min(20000, 15000) = 15000`

 **Response validate thực tế**

 ```json
 {
	 "voucherId": "b1a29b33-0d95-4ab4-98d2-03c6c8a6a123",
	 "code": "TEST-FREE-SHIPPING-ACTIVE",
	 "valid": true,
	 "message": "Voucher hợp lệ",
	 "discountAmount": 15000,
	 "remainingQuantity": 30
 }
 ```

 ## 7. Lỗi validation và lỗi nghiệp vụ

 ### 7.1 Lỗi validation đầu vào

 Trường hợp dữ liệu request body sai schema hoặc vi phạm constraint, hệ thống trả về `400 Bad Request` với `ErrorMessage`.

 Ví dụ:

 ```json
 {
	 "statusCode": 400,
	 "timestamp": "2026-05-21T10:15:30",
	 "message": "orderAmount: Giá trị đơn hàng phải lớn hơn hoặc bằng 0",
	 "description": "Validation failed"
 }
 ```

 ### 7.2 Lỗi nghiệp vụ phổ biến

 - Voucher chưa đến thời gian bắt đầu.
 - Voucher đã hết thời gian kết thúc.
 - Voucher đã bị vô hiệu hóa.
 - Voucher đã hết số lượng.
 - Đơn hàng chưa đạt mức tối thiểu.
 - Khách hàng đã vượt quá số lần sử dụng cho phép.
 - `FREE_SHIPPING` nhưng thiếu `shippingFee`.
 - Mã voucher bị trùng.

 ### 7.3 Quy tắc làm tròn và chuẩn hóa số tiền

 - Tất cả giá trị tiền được normalize trước khi tính.
 - Scale của phép tính là `0`.
 - Quy tắc làm tròn là `HALF_UP`.
 - Nếu kết quả tính ra nhỏ hơn 0, hệ thống trả về `0`.

 ## 8. Mã lỗi phổ biến

 | HTTP Status | Tình huống điển hình | Ví dụ message |
 |---|---|---|
 | `400` | Thiếu dữ liệu hoặc sai định dạng | `Giá trị đơn hàng không được để trống` |
 | `400` | Vi phạm điều kiện nghiệp vụ | `Voucher đã hết số lượng` |
 | `400` | Trạng thái thao tác không hợp lệ | `Voucher chưa có hiệu lực` |
 | `404` | Không tìm thấy voucher hoặc redemption | `Không tìm thấy voucher` |
 | `500` | Lỗi hệ thống | `Unexpected server error` |

 ## 9. Flow áp dụng voucher khi checkout

 1. Frontend/checkout service nhận `voucherId` hoặc `code` từ khách hàng.
 2. Hệ thống tra cứu voucher theo id hoặc code.
 3. Gọi API validate với `customerId`, `orderAmount`, `shippingFee`.
 4. Nếu `valid = false`, trả thông báo lỗi cho người dùng và không tiếp tục checkout.
 5. Nếu `valid = true`, hiển thị `discountAmount` để cập nhật tổng tiền thanh toán.
 6. Sau khi đơn hàng được tạo thành công, gọi API redeem để ghi nhận sử dụng voucher.
 7. Transaction redeem sẽ giảm số lượng voucher và tạo bản ghi redemption.
 8. Nếu voucher hết số lượng sau redeem, hệ thống chuyển trạng thái sang `EXPIRED`.
 9. Lưu kết quả redemption để phục vụ tra cứu lịch sử và đối soát sau này.

 ## 10. Quy tắc transaction khi redeem voucher

 - Redeem phải được thực hiện trong một transaction để đảm bảo tính nhất quán giữa bảng voucher và bảng redemption.
 - Nếu lưu redemption thất bại, thay đổi số lượng voucher không được commit.
 - Nếu giảm số lượng thành công nhưng tạo redemption thất bại, transaction phải rollback toàn bộ.
 - Số lượng voucher không được nhỏ hơn 0.
 - Khi số lượng sau redeem bằng 0, voucher được chuyển sang `EXPIRED`.
 - Redeem chỉ được thực hiện sau khi checkout của đơn hàng đã thành công hoặc ở trạng thái xác nhận cuối cùng.
 - Không nên gọi redeem trước khi đơn hàng được tạo thành công để tránh trạng thái voucher và đơn hàng bị lệch.

 ## Lưu ý nghiệp vụ

 - `code` luôn được chuẩn hóa sang chữ in hoa trước khi lưu và tra cứu.
 - Xóa voucher là xóa logic, không xóa cứng.
 - `maxUsagePerUser` chỉ có ý nghĩa khi voucher cần giới hạn theo khách hàng.
 - Với voucher `FREE_SHIPPING`, `shippingFee` là dữ liệu cần thiết để tính giảm.
 - Mỗi lần redeem thành công đều tạo lịch sử để phục vụ audit.
 - Danh sách redemption nên được bảo vệ ở lớp quyền truy cập nếu dùng trong môi trường production.
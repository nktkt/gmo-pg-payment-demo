# GMO-PG Payment Demo

A full-flow credit card payment demo application built with Spring Boot and the GMO Payment Gateway (GMO-PG) Protocol Type API.

Demonstrates the complete payment lifecycle: **Authorization (AUTH) → Capture (SALES) → Void/Refund (VOID/RETURN)** — with PCI DSS-compliant card data non-retention via token-based payment.

## Architecture

```
Browser (Thymeleaf)                Spring Boot                    GMO-PG
  │                                     │                            │
  │  1. Browse products                 │                            │
  │<────────────────────────────────────│                            │
  │                                     │                            │
  │  2. Enter card info                 │                            │
  │     token.js sends directly ───────────────────────────────────>│
  │     to GMO-PG                       │                            │
  │<── token returned ─────────────────────────────────────────────│
  │                                     │                            │
  │  3. Submit token + order info       │                            │
  │────────────────────────────────────>│  4. EntryTran (AUTH)       │
  │                                     │───────────────────────────>│
  │                                     │<── AccessID, AccessPass ──│
  │                                     │  5. ExecTran (token)       │
  │                                     │───────────────────────────>│
  │                                     │<── Approve, TranID ───────│
  │  6. Show result                     │                            │
  │<────────────────────────────────────│                            │
  │                                     │                            │
  │  7. Admin: Capture / Cancel         │  8. AlterTran              │
  │────────────────────────────────────>│───────────────────────────>│
  │                                     │<── Result ────────────────│
```

## Tech Stack

- **Java 21** / **Spring Boot 3.4**
- **Gradle** (Kotlin DSL)
- **Thymeleaf** (server-side rendering)
- **GMO-PG Protocol Type API** (direct HTTP POST, no SDK)
- **GMO-PG token.js** (frontend tokenization for card data non-retention)
- **H2 Database** (in-memory, for demo purposes)
- **Bootstrap 5** (UI)

## Features

- **Token-based payment (PCI DSS compliant)** — Card data is sent directly from the browser to GMO-PG via `token.js`. The server never handles raw card numbers.
- **Full payment lifecycle** — Auth → Capture → Void/Refund
- **Admin dashboard** — View all transactions, capture authorized payments, cancel/refund
- **Log masking** — Automatic masking of card numbers, tokens, AccessPass, ShopPass, and security codes in application logs
- **Sample products** — 3 pre-loaded demo products with an H2 in-memory database

## Prerequisites

- **JDK 21**
- **GMO-PG test environment credentials** (ShopID / ShopPass)

## Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/<your-username>/payment-demo.git
   cd payment-demo
   ```

2. **Configure GMO-PG credentials**

   Set environment variables:

   ```bash
   export GMOPG_SHOP_ID=your_shop_id
   export GMOPG_SHOP_PASS=your_shop_pass
   ```

   Or edit `src/main/resources/application.yml` directly.

3. **Run the application**

   ```bash
   ./gradlew bootRun
   ```

4. **Open in browser**

   ```
   http://localhost:8080/
   ```

## Usage

1. **Browse products** at `http://localhost:8080/`
2. **Select a product** → enter test card info on the checkout page
3. **Test card**: `4111111111111111`, any future expiry (e.g., `12/28`), any 3-digit CVV
4. **View result** — approval number and transaction ID are displayed
5. **Admin panel** at `http://localhost:8080/admin` — capture or cancel transactions

## Project Structure

```
src/main/java/com/example/payment/
├── PaymentDemoApplication.java          # Main class
├── config/
│   ├── GmoPgProperties.java            # GMO-PG config (ConfigurationProperties)
│   ├── RestTemplateConfig.java          # RestTemplate with logging interceptor
│   └── LogMaskingPatternLayout.java     # Logback log masking
├── controller/
│   ├── ProductController.java           # Product listing & checkout page
│   └── PaymentController.java           # Payment processing & admin
├── service/
│   ├── GmoPgApiClient.java             # GMO-PG API client
│   └── PaymentService.java             # Payment business logic
├── model/
│   ├── Product.java                     # Product entity
│   ├── PaymentTransaction.java          # Transaction entity
│   └── TransactionStatus.java           # Status enum
├── dto/                                 # Request/response DTOs
├── repository/                          # Spring Data JPA repositories
└── exception/
    └── GmoPgApiException.java           # GMO-PG error exception
```

## Log Masking

The following sensitive data is automatically masked in logs:

| Data | Pattern | Masked Output |
|------|---------|---------------|
| Card numbers | `1234 5678 9012 3456` | `****-****-****-3456` |
| Token | `Token=abc123...` | `Token=****` |
| AccessPass | `AccessPass=xyz...` | `AccessPass=****` |
| ShopPass | `ShopPass=xyz...` | `ShopPass=****` |
| Security code | `SecurityCode=123` | `SecurityCode=****` |

## Running Tests

```bash
./gradlew test
```

Tests use `MockRestServiceServer` to verify the GMO-PG API client behavior (EntryTran, ExecTran, AlterTran — both success and error cases).

## License

MIT

# ⚡ ElectraSync ERP
**Electronics Retail & POS Management System**
*Java Spring Boot · Thymeleaf · MySQL · Spring Security · Hibernate JPA*

---

## What This System Does
ElectraSync ERP is a complete backend + frontend web application for running an electronics retail store (similar to Croma, Reliance Digital, Vijay Sales). It covers every part of the business lifecycle:

| Business Flow | What the System Does |
|---|---|
| **Owner buys from vendor** | Create Purchase Order → Receive goods → Stock auto-increases → Record vendor payment |
| **Customer walks in** | POS Billing screen → Add products → Apply discount → Generate invoice → Stock auto-decreases |
| **Owner checks profitability** | P&L Report shows Revenue, Gross Profit, Salary Expenses, Net Profit |
| **Manager manages inventory** | Add products, categories, brands, watch low-stock alerts |
| **Owner manages staff** | Add employees, pay monthly salary with bonus/deductions, full history |
| **Owner controls access** | Create logins with OWNER / MANAGER / CASHIER roles |

---

## Tech Stack
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6 (BCrypt + Role-based) |
| ORM | Hibernate JPA via Spring Data JPA |
| View Layer | Thymeleaf (server-rendered HTML, like JSP but for Spring Boot 3) |
| Database | MySQL 8.x |
| Build Tool | Maven |
| Frontend | HTML5, CSS3, Bootstrap (via CDN), minimal vanilla JS |

---

## Project Structure
```
electrasync-erp/
├── src/main/java/com/electrasync/
│   ├── ElectraSyncApplication.java       ← Spring Boot entry point
│   ├── config/
│   │   ├── SecurityConfig.java           ← URL access rules, login/logout
│   │   └── DataInitializer.java          ← Seeds default owner account on startup
│   ├── security/
│   │   └── CustomUserDetailsService.java ← Loads user from DB for Spring Security
│   ├── model/                            ← JPA entities (database tables)
│   │   ├── User.java                     ← System logins (OWNER/MANAGER/CASHIER)
│   │   ├── Employee.java                 ← Staff members
│   │   ├── SalaryPayment.java            ← Monthly salary records
│   │   ├── Category.java                 ← Product categories (TV, Mobile, etc.)
│   │   ├── Brand.java                    ← Brands (Samsung, Sony, etc.)
│   │   ├── Product.java                  ← Products with SKU, cost price, sell price, stock
│   │   ├── Vendor.java                   ← Supplier companies
│   │   ├── PurchaseOrder.java            ← Goods ordered from vendor
│   │   ├── PurchaseOrderItem.java        ← Line items on a PO
│   │   ├── Customer.java                 ← Registered customers
│   │   ├── Sale.java                     ← A completed invoice/bill
│   │   └── SaleItem.java                 ← Line items on an invoice
│   ├── repository/                       ← Spring Data JPA interfaces (auto-generates SQL)
│   ├── service/                          ← Business logic
│   │   ├── ProductService.java
│   │   ├── VendorService.java
│   │   ├── PurchaseOrderService.java     ← Stock increase on receive, vendor payment
│   │   ├── CustomerService.java
│   │   ├── EmployeeService.java          ← Salary payment logic
│   │   ├── SaleService.java              ← Core POS billing, stock deduction, profit calc
│   │   └── DashboardService.java         ← Aggregates all KPIs for dashboard
│   └── controller/                       ← MVC controllers (handle HTTP requests)
│       ├── AuthController.java           ← /login
│       ├── DashboardController.java      ← /dashboard
│       ├── ProductController.java        ← /manager/products/**
│       ├── VendorController.java         ← /manager/vendors/**
│       ├── PurchaseOrderController.java  ← /manager/purchase-orders/**
│       ├── CustomerController.java       ← /manager/customers/**
│       ├── EmployeeController.java       ← /owner/employees/**
│       ├── UserController.java           ← /owner/users/**
│       ├── POSController.java            ← /pos/** (billing screen)
│       ├── SalesHistoryController.java   ← /sales/**
│       └── ReportController.java         ← /owner/reports/**
├── src/main/resources/
│   ├── application.properties            ← DB config, server port
│   ├── static/css/style.css              ← Custom design system
│   ├── static/js/main.js                 ← Minimal utilities
│   └── templates/                        ← Thymeleaf HTML pages
│       ├── auth/login.html
│       ├── dashboard.html
│       ├── partials/sidebar.html          ← Reusable navigation sidebar
│       ├── manager/                       ← Products, vendors, POs, customers
│       ├── owner/                         ← Employees, salary, users, P&L
│       ├── pos/                           ← Billing screen + invoice
│       └── sales/                         ← Sales history
├── database-setup.sql                    ← Run this once in MySQL
└── pom.xml                               ← Maven dependencies
```

---

## Setup & Run Instructions

### Step 1 — Prerequisites
- Java 17+ installed (`java -version`)
- MySQL 8.x running locally
- Maven installed (`mvn -version`) or use IntelliJ IDEA

### Step 2 — Create the Database
Open MySQL and run:
```sql
CREATE DATABASE electrasync_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Or run the included `database-setup.sql` file.

### Step 3 — Configure Database Password
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.password=your_mysql_root_password
```
Change `your_password_here` to your actual MySQL password.

### Step 4 — Build & Run
```bash
cd electrasync-erp
mvn spring-boot:run
```
Or open in IntelliJ IDEA → Run `ElectraSyncApplication.java`

### Step 5 — Open in Browser
```
http://localhost:8080/login
```

### Default Login
| Username | Password | Role |
|---|---|---|
| `owner` | `Admin@123` | OWNER (full access) |

The owner account is auto-created on first startup.

---

## Role-Based Access Control

| Feature | OWNER | MANAGER | CASHIER |
|---|---|---|---|
| Dashboard with KPIs | ✅ | ✅ | ❌ (goes to POS) |
| POS Billing | ✅ | ✅ | ✅ |
| Sales History | ✅ | ✅ | ✅ |
| Products / Inventory | ✅ | ✅ | ❌ |
| Vendors | ✅ | ✅ | ❌ |
| Purchase Orders | ✅ | ✅ | ❌ |
| Customers | ✅ | ✅ | ❌ |
| Employees & Salary | ✅ | ❌ | ❌ |
| Staff Logins | ✅ | ❌ | ❌ |
| Profit & Loss Report | ✅ | ❌ | ❌ |

---

## Key Business Logic (for interviews)

**How stock is managed:**
- Stock **increases** only when a Purchase Order is marked as "Received" (real goods delivered by vendor)
- Stock **decreases** the moment a sale is completed at POS
- Returns restore stock automatically

**How profit is calculated:**
- Each SaleItem stores `unitPrice` (selling price) and `unitCost` (cost price) **at the time of sale**
- This means even if you change the product's cost later, old invoices still reflect correct profit
- Gross Profit = sum of `(unitPrice - unitCost) × quantity` across all completed sales
- Net Profit = Gross Profit − Salary Expenses paid that month

**How vendor payments work:**
- A PO has `totalAmount` and `amountPaid`
- Owner can record partial payments over time
- `amountDue = totalAmount - amountPaid` shown on the PO screen

---

## How to Add to Resume

**Project:** ElectraSync ERP — Electronics Retail & POS Management System

**Tech Stack:** Java, Spring Boot, Spring Security, Hibernate (JPA), Thymeleaf, MySQL, Maven, Git

**Bullet points:**
- Architected a full-stack ERP for electronics retail covering POS billing, inventory, vendor procurement, employee management and financial reporting using Spring Boot MVC and Hibernate ORM
- Implemented role-based authentication (OWNER/MANAGER/CASHIER) with Spring Security and BCrypt password hashing, controlling access to 10+ modules
- Built a POS billing engine with real-time stock validation, automatic stock deduction, invoice generation, discount management and multi-mode payment support (Cash/UPI/Card/EMI)
- Designed a vendor supply chain module with Purchase Order creation, goods receipt workflow and incremental vendor payment tracking
- Developed a monthly P&L report calculating Gross Profit (from snapshotted sale-time cost prices) minus salary expenses to give accurate Net Profit

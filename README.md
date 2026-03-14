# SB-IBMS – Integrated Business Management System

**SB-IBMS** is a full-stack **enterprise-grade business management platform** designed to streamline core operations for small-to-medium businesses. 
SB Group Integrated Business Management System centralized core operations, including inventory tracking, sales and purchase management, customer CRM, 
Reporting and role-based access control, Cashback system, with call system, payments API integration, Cashback Excel, disbursement Excel, master data Excel, 
Auto-generation and update features.

Built with modern **Java**, **Spring Boot**, and best practices for scalability, security, and maintainability.

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat&logo=spring)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![GitHub stars](https://img.shields.io/github/stars/Shishirkumardas/SB_Group_Backend_Java_Springboot?style=social)](https://github.com/Shishirkumardas/SB_Group_Backend_Java_Springboot)

## Features

- **Cashback & Rewards** — Automated monthly cashback calculation and payment tracking
- **Disbursement Document** — Automated generation and update of Bkash Cashback payment Disbursement Sheet.
- **Real-time Recalculation** — On-demand or triggered updates of financial summaries
- **Inventory & Stock Management** — Track items, quantities, purchases, and stock levels
- **Sales & Purchase Ledger** — Record transactions, calculate dues, and manage payments
- **Area/Territory Summary** — Daily & overall summaries per area (purchase, paid, due, cashback, package quantity)
- **RESTful API Backend** — Secure, scalable APIs for frontend or mobile integration
- **Role-based Authentication** — Using Spring Security + Custom UserDetails
- **Database Optimization** — Efficient JPA/Hibernate queries and aggregations
- **Container-ready** — Docker support for easy deployment

## 🛠 Tech Stack

| Layer             | Technology                          | Purpose                              |
|-------------------|-------------------------------------|--------------------------------------|
| Backend           | Java 17/21, Spring Boot 3.x         | Core framework                       |
| API               | Spring Web, RESTful APIs            | HTTP endpoints                       |
| Payment           | Bkash Payment API                   | Payment Gateway                      |
| Data Access       | Spring Data JPA, Hibernate          | ORM & repositories                   |
| Database          | MSSQL / PostgreSQL (configurable)   | Persistent storage                   |
| Security          | JWT                                 | Authentication & authorization       |
| Build Tool        | Maven                               | Dependency & build management        |
| Frontend (partial)| Next.js (optional integration)      | Modern React-based UI                |
| Call Service      | Twilio                              | Customer Call System                 |
| Testing           | JUnit 5, Mockito                    | Unit & integration tests             |
| Other             | Lombok, MapStruct (if used), Git    | Productivity & code quality          |

## 📸 Screenshots / Demo
![Master Data](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshots/master-data.jpg)
![Auto Generated Cashback Details](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshots/cashback-details.jpeg)
![Daily Cashback sheet Extraction](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshots/cashback-excel-extract.jpg.jpeg)
![Daily Cashback sheet](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshots/cashback-excel-file-exp.jpg)
![Bkash Disbursement Sheet Upload](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshots/cashback-payout-update.jpg)
![Dashboard](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshotsdashboard.jpg)
![Area Summary](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshots/area-summary.jpg)
![Product Manage](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshots/manage-products.jpg)
![Daily Summary](https://raw.githubusercontent.com/Shishirkumardas/SB_Group_Backend_Java_Springboot/master/screenshots/daily-summary.jpg)


## 🚀 Quick Start (Local Development)

### Prerequisites

- Java 17+ (JDK)
- Maven 3.8+
- MySQL 8+ or PostgreSQL (or use H2 for quick testing)
- Git

### Steps

1. **Clone the repository**

   ```bash
   git clone https://github.com/Shishirkumardas/SB_Group_Backend_Java_Springboot.git
   cd SB_Group_Backend_Java_Springboot

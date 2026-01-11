# Java-SmartPos-System

SmartPOS is a modern, Point of Sale (POS) system built with **JavaFX** and **Clean Architecture**. It is designed to be a robust starting point for small businesses or a portfolio project demonstrating industrial-level software design patterns in Java.

<img width="1279" height="827" alt="preview" src="https://github.com/user-attachments/assets/622388f7-b83d-41fa-98ac-c8c74e3cb03b" />

## 🚀 Features

- **Point of Sale (POS)**: Fast checkout with automatic product lookup by ID/SKU.
- **Product Management**: Create, edit, and deactivate products.
- **Inventory Control**: Track stock movements (In/Out) automatically.
- **Purchasing**: Register supplier invoices to replenish stock.
- **Reporting**: Real-time insights on Sales, Stock Levels, and Top Selling Products.
- **Clean UI**: Modern dark/light interface built with CSS-styled JavaFX.

## 🛠️ Technology Stack

- **Language**: Java 17
- **UI Framework**: JavaFX 21 (Modular)
- **Database**: MySQL 8.0
- **Persistence**: JDBC (Raw SQL for performance and control) with HikariCP Connection Pooling.
- **Architecture**: Hexagonal / Clean Architecture (Domain, Infrastructure, UI separation).
- **Build Tool**: Maven

## 📂 Project Structure

```
src/main/java/io/smartpos
├── core            # Domain Logic (Entities, Exceptions, Interfaces)
├── infrastructure  # Data Access (DAO Impl, JDBC, Database)
├── services        # Application Business Logic (Use Cases)
└── ui              # JavaFX Controllers and Views (FXML)
```

## ⚙️ Setup & Installation

### Prerequisites

- JDK 17 or higher
- Maven 3.8+
- MySQL Server 8.0+
- Thermal printer connectivity (optional for physical tickets).

### Database Setup

1. Create a MySQL database (e.g., `smartpos_db`).
2. Run the SQL script found in `database/schema.sql` to create tables.
3. Update `src/main/resources/application.properties` with your credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/smartpos_db
   db.username=your_username
   db.password=your_password
   ```

### Running the App

```bash
mvn clean compile exec:java
```

---

📄 **License**
This project is distributed under the MIT license. Its purpose is strictly educational and research-based, developed as an Applied Java solution.

Note for recruiters: This project demonstrates my ability to design and implement complex systems using professional standards. It highlights my mastery of transactional integrity, clean architecture, and the development of resilient software capable of handling real-world failure scenarios.

Author: JUAN S.
Contact: https://github.com/johnyse99



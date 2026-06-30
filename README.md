

# 📊 Java Database Management System (Inventory Dashboard)

An efficient, GUI-driven Java application built to manage database operations seamlessly. This project integrates a robust MySQL backend with an intuitive Java Swing frontend, designed specifically for tracking inventory, validating input dynamically, and managing data workflows.

---

## 🚀 Features

* **💻 Interactive Dashboard:** Real-time inventory tracking and UI-driven data manipulation via `InventoryDashboard.java`.
* **🔌 Seamless MySQL Integration:** Automated or manual configuration with MySQL using the pre-configured `mysql-connector-j-9.7.0` driver.
* **🛠️ Database Automations:** Instant database generation via setup scripts (`schema.sql` and `sample_data.sql`).
* **🛡️ Robust Input Validation:** Built-in validation algorithms (`InputValidator.java`) preventing corrupt data entries or invalid types before hitting the database.
* **💬 Responsive Dialogs:** Clean user messaging and error handlings utilizing structural layout dialogs (`DialogHelper.java`).

---

## 📁 Project Structure

```text
Javadbmsproject-main/
│
├── 📂 sql/                         # Database initialization files
│   ├── schema.sql                  # Database tables and constraints definition
│   ├── sample_data.sql             # Dummy data for immediate testing
│   └── demo_queries.sql            # Built-in SQL queries for testing execution
│
├── 📂 src/                         # Java Source Code
│   ├── Main.java                   # Main application entry point
│   ├── InventoryDashboard.java     # Primary Swing GUI dashboard interface
│   ├── DatabaseManager.java        # Core JDBC driver connections and CRUD executions
│   ├── DatabaseSetup.java          # Automation for initial database/table setup
│   ├── InputValidator.java         # Data format validation logic
│   ├── DialogHelper.java           # Simplification wrapper for UI popups/alerts
│   └── SelectItem.java             # Logic handling data row selection and extraction
│
└── 📂 .idea/                       # JetBrains IntelliJ configuration environment

```

---

## 🛠️ Prerequisites

Before you begin, ensure you have the following installed:

* **Java Development Kit (JDK 11 or higher)**
* **MySQL Server** (Running locally or hosted)
* An IDE of choice (e.g., **IntelliJ IDEA** or Eclipse)

---

## ⚙️ Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/Javadbmsproject.git
cd Javadbmsproject-main

```

### 2. Configure the Database

You can set up the database using your preferred MySQL CLI or GUI client (like Workbench):

```bash
# Log in to your MySQL terminal
mysql -u your_username -p

# Source the schema and sample data
mysql> source sql/schema.sql;
mysql> source sql/sample_data.sql;

```

*(Alternatively, the application's `DatabaseSetup.java` can automate table builds once connection variables are provided).*

### 3. Update Database Credentials

Open `src/DatabaseManager.java` and adjust your connection string configurations:

```java
private static final String URL = "jdbc:mysql://localhost:3606/your_database_name";
private static final String USER = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";

```

### 4. Build and Run

If you are using **IntelliJ IDEA**:

1. Open the root folder in IntelliJ.
2. The `.idea` files will automatically configure the `mysql-connector-j-9.7.0.xml` library dependency.
3. Open `src/Main.java` and click **Run**.

---

## 📊 Sample Queries Included

To check data handling methods manually, consult `sql/demo_queries.sql`. These contain pre-built triggers/queries useful for modifying inventory behaviors outside the GUI.

---

## 🛡️ License

Distributed under the MIT License. See `LICENSE` for more information (if applicable).


# Inventory Management System

Java Swing and MySQL inventory management project for database application demonstration.

## Features

- Product CRUD with supplier mapping.
- Manual stock update from the Swing UI.
- Multi-item order placement.
- Order cancellation that restores stock.
- Report tabs for low stock, inventory value, product sales, and stock movements.
- SQL schema, sample data, DML examples, views, triggers, basic queries, and complex queries.

## Database Setup

Run the setup script in MySQL as an admin user:

```powershell
mysql -u root -p < sql\01_schema_and_seed.sql
```

The Java application defaults to:

- Database: `project`
- User: `javauser`
- Password: `password123`
- JDBC URL: `jdbc:mysql://localhost:3306/project?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`

The setup script creates the database user and grants access.

## Demonstration SQL

After setup, run:

```powershell
mysql -u javauser -p project < sql\02_basic_queries.sql
mysql -u javauser -p project < sql\03_complex_queries.sql
```

`02_basic_queries.sql` demonstrates basic SELECT, WHERE, INSERT, UPDATE, and DELETE operations.

`03_complex_queries.sql` demonstrates joins, grouping, aggregate functions, views, subqueries, transactions, and trigger behavior.

## Run the Swing App

Open the project in IntelliJ IDEA and run `ui.MainFrame`.

If `javac` is available on PATH, you can also compile from PowerShell:

```powershell
$jar = "$env:USERPROFILE\Downloads\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar"
$sources = Get-ChildItem src -Recurse -Filter *.java
javac -cp $jar -d out\production\javadbproject $sources.FullName
java -cp "out\production\javadbproject;$jar" ui.MainFrame
```

To use different database credentials:

```powershell
java -Dinventory.db.url="jdbc:mysql://localhost:3306/project?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" `
     -Dinventory.db.user="javauser" `
     -Dinventory.db.password="password123" `
     -cp "out\production\javadbproject;$jar" ui.MainFrame
```

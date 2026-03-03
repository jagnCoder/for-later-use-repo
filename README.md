# 🏦 Java Full-Stack KYC Management System

A robust, two-phase Java application designed to streamline the "Know Your Customer" (KYC) process. This project was developed as part of a team competition where I served as the lead contributor.

## 🌟 Project Evolution
* **Phase 1 (CMD Interface):** A console-based application to test core logic, database connectivity, and data validation.
* **Phase 2 (Web Integration):** An upgraded version featuring a Java-based backend server and an HTML frontend for a modern user experience.

## 🛠️ Tech Stack
* **Language:** Java (JDK 8+)
* **Database:** MySQL (Relational Data Storage)
* **Frontend:** HTML5
* **Backend:** Java Socket Programming / HTTP Server
* **Drivers:** MySQL Connector/J

## 📊 Key Features
* **User Data Entry:** Captures name, date, and document details.
* **Database Persistence:** Stores user records securely in a MySQL database (`kyc_db.sql`).
* **Server-Client Architecture:** A custom Java server (`KYCServer.java`) that handles frontend requests and processes data.

## 📁 File Breakdown
* `KYCApp.java`: Main logic for data processing.
* `KYCServer.java`: Handles HTTP requests and serves the HTML frontend.
* `kyc_db.sql`: The database schema and table structures.

---
*Note: This was a collaborative team project where I handled the majority of the implementation, including the transition from a CLI to a Web-based interface.*
# Taxi & Driver Reservation System — MySQL JDBC

A plain Java Swing taxi/driver reservation desktop application. Customer registration,
role-specific login, bookings, drivers, history, and dashboards use **MySQL** through
JDBC. No web server, Maven, Spring, or GUI redesign is required.

## Easiest way to run on Windows (like VRMS)

1. Install **MySQL Community Server 8.0+** and **JDK 17+**. Configure the MySQL
   password during installation. Make sure javac and java are in PATH.
2. Clone/download this repository.
3. **Double-click run.bat** from the project folder.
4. The launcher checks for MySQL on localhost:3306. If a local MySQL Windows
   service is stopped, it can request administrator permission to start it.
5. On the first run, enter the **MySQL username and password** when prompted.
   Typically the username is root and the password is the password set during
   MySQL installation.
6. The launcher validates your MySQL login and automatically creates the
   taxi_booking database and its three tables when missing.
7. On the first successful start, a small dialog asks you to **create your admin
   username and password** (minimum eight characters). Then sign in as admin,
   add a driver, register a customer, and create a booking.

**You do not manually download or attach a Connector/J JAR.** build.ps1
automatically downloads the MySQL JDBC driver into lib/ on first use, then
compiles the Java code with it. The JAR is ignored by Git and reused on later
runs. Internet is needed for that first download only.

The MySQL Server itself **must be installed locally or reachable over the
network**. The JDBC driver is not a substitute for the database server.

### What run.bat does

- Runs run.ps1, which calls build.ps1.
- Checks Java and optionally starts a stopped local MySQL service.
- Downloads MySQL Connector/J **9.7.0** from Maven Central if missing.
- Compiles every src/com/taxireservation/*.java into out/.
- Prompts for the local MySQL login on the first run.
- Verifies the MySQL login before saving it or starting the app.
- Saves the database login to .taxi-db.json using Windows user encryption.
  It is not committed to Git. Invalid saved credentials are discarded.
- Automatically creates missing MySQL tables, without deleting existing data.
- Starts com.taxireservation.Main.
- Stops a MySQL service on exit **only if this launcher started that service**.
  An already-running MySQL service is left running.

To forget saved MySQL credentials and enter them again:

    .\run.bat -ResetDbConfig

To compile only (with automatic driver download):

    powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1

The launcher requires PowerShell (included with Windows) and internet access
for the first JDBC-driver download.

## First-run authentication

The first administrator is created through an application prompt after MySQL
initialization. A preconfigured administrator can also be supplied with
TAXI_ADMIN_USERNAME and TAXI_ADMIN_PASSWORD environment variables before
launch. Do not use the old public demo credentials
admin@taxires.com / admin123. Customer passwords are salted PBKDF2 hashes.

User data is stored in MySQL; the old data/users.txt has been removed. There is
no automatic import of previous text-file users or in-memory demo records.
Add drivers through the admin UI after the first launch.

## Advanced: environment configuration

The launcher defaults to the local database:

    TAXI_DB_URL=jdbc:mysql://localhost:3306/taxi_booking
    TAXI_DB_SERVER_URL=jdbc:mysql://localhost:3306/
    TAXI_DB_USER=root
    TAXI_DB_PASSWORD=<the password you supplied>

For a custom MySQL host, set both TAXI_DB_URL and TAXI_DB_SERVER_URL, and ensure
the MySQL user has access to that server. Environment variables override the
saved login when TAXI_DB_PASSWORD is explicitly set. When using an account
without CREATE DATABASE/CREATE TABLE permissions, initialize the schema with
an administrator beforehand (see db/schema.sql).

PowerShell example for temporary environment settings in the current terminal:

    $env:TAXI_DB_USER = "taxi_app"
    $env:TAXI_DB_PASSWORD = "your-password"

The optional db/schema.sql file provides the same database structure for
manual setup in MySQL Workbench or for instructors inspecting the schema.
First-run auto-creation means Workbench is **optional**, not required.

## NetBeans (optional)

The repository contains hand-written Swing source code, rather than a generated
NetBeans project or .form files.

To use it inside NetBeans, create a Java with Ant → Java Application project
without generating a Main Class, copy src/com/taxireservation into its source
directory, and set the main class to com.taxireservation.Main.

If you use **NetBeans Run** rather than run.bat, add the locally downloaded
lib/mysql-connector-j-9.7.0.jar to the project's Libraries and set the
TAXI_DB_USER / TAXI_DB_PASSWORD environment variables for the Java process.
Running run.bat instead avoids this manual IDE classpath setup.

## How persistence works

- MySQL users store password hashes and unique account identities; customer
  history is filtered by user ID rather than matching display names.
- MySQL drivers and reservations use database-generated IDs.
- Booking creation atomically claims the driver and inserts a Pending booking.
  On failure, the entire transaction is rolled back.
- A driver with an active booking cannot be double-booked or manually removed.
- Taxi + Driver selects only available drivers of the requested cab type;
  Driver Only (Own Vehicle) selects any available driver.
- Start Trip, Complete, and Cancel persist their changes. Completion and
  cancellation free the associated driver.
- Driver removal is a soft deletion to preserve existing booking history.
- All dashboard totals and completed-trip revenue are loaded from MySQL.

The original Swing screens and fare formula are otherwise unchanged.

## Troubleshooting

- **javac is not recognized:** install JDK 17+ and add it to PATH.
- **Connector download fails:** check internet, then rerun run.bat.
- **MySQL not reachable:** start/install MySQL Server and confirm port 3306.
- **Access denied:** enter the MySQL password chosen during installation,
  or run `run.bat -ResetDbConfig` if a saved login was wrong.
- **Insufficient database privileges:** use a database administrator for the
  initial schema creation, then use a restricted MySQL account.
- **Another MySQL host/port:** configure TAXI_DB_URL and TAXI_DB_SERVER_URL.
- **Customer sees no history:** reservations are linked to the signed-in user
  ID. Administrator-created bookings without that user ID are admin-only.

## Manual verification checklist

1. Start with run.bat; sign in with the newly created administrator account.
2. Add an available driver; restart the app and verify the driver remains.
3. Register a customer; restart and sign in again.
4. Book a trip; check the driver becomes unavailable and cannot be booked twice.
5. Complete/cancel a booking; check the driver is available again.
6. Register users with the same display name and verify private history.
7. Restart and check booking history and revenue are unchanged.

System requirement: JDK 17+, MySQL Server 8.0+, Windows PowerShell. There are
no additional manually installed Java libraries when using run.bat.

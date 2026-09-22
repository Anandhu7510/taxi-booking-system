# Taxi & Driver Reservation System — NetBeans Guide

A complete Java Swing desktop app: login, dashboard, new bookings, driver
management, and reservation history — ready to run and easy to keep
building on inside the NetBeans GUI Builder.

## 1. Import into NetBeans

1. **File → New Project → Java with Ant → Java Application** (or "Java Application"
   under the plain "Java" category). Name it `TaxiReservationSystem`, uncheck
   "Create Main Class" (we already have one).
2. In the Projects panel, delete the auto-generated empty `src` package if any.
3. Right-click **Source Packages → Import → select this `src/com/taxireservation`
   folder** (or just copy the `com` folder into your project's `src/` on disk,
   then in NetBeans: right-click project → **Clean and Build**).
4. Right-click `Main.java` → **Run File** (or set Project Properties → Run →
   Main Class to `com.taxireservation.Main`, then hit the green Run button).
5. Login screen opens. Use **admin / admin123**.

No external libraries or database setup needed — it runs immediately with
seeded sample data (drivers + bookings) held in memory.

## 2. What's included

| Screen | File | Features |
|---|---|---|
| Login | `LoginForm.java` | Username/password gate, styled card layout |
| Dashboard | `MainDashboard.java` + `DashboardHomePanel.java` | Sidebar nav (CardLayout), stat cards: total bookings, ongoing trips, completed, revenue |
| New Booking | `NewReservationPanel.java` | Customer name/phone, pickup/drop, date-time, cab type, **live-updating fare estimate**, auto-filtered list of *available* drivers only |
| Driver Management | `DriverManagementPanel.java` | Add driver (name, phone, license, vehicle no., type), table view, set Available/Offline, remove driver |
| Reservations | `ReservationHistoryPanel.java` | Search by customer/location, filter by status, Mark Completed / Cancel (auto-frees the driver back to Available) |
| Data | `DataStore.java` | Single in-memory source of truth shared by all screens — swap for JDBC later without touching any UI code |
| Models | `Driver.java`, `Reservation.java` | Plain POJOs |

This covers the core feature set any taxi/driver reservation UI needs:
auth, at-a-glance metrics, booking creation with fare + driver assignment,
fleet management, and a searchable/filterable trip history with status
workflow (Pending → Ongoing → Completed/Cancelled).

## 3. Rebuilding each screen with the NetBeans GUI Builder (drag & drop)

Every panel file has a comment block at the top titled **"NetBeans GUI
Builder equivalent"** describing exactly which components to drag from the
**Palette** (Swing Controls) and how to lay them out with **GroupLayout**
("Free Design," NetBeans's default). Quick summary:

- **New JFrame Form / New JPanel Form**: right-click the package →
  **New → Other → Swing GUI Forms → JFrame Form** (for `LoginForm`,
  `MainDashboard`) or **JPanel Form** (for the three feature panels).
- Drag `JLabel`, `JTextField`, `JComboBox`, `JButton`, `JTable` from the
  **Palette** onto the **Design** canvas; snap-align using the blue guide
  lines NetBeans shows automatically (that's GroupLayout doing its thing).
- Rename each component in the **Properties → Code → Variable Name** field
  to match names like `txtName`, `cboCabType`, `tblReservations` — makes the
  generated code readable.
- Double-click a button in Design view to jump straight to its
  `actionPerformed` handler in the **Source** view — paste in the matching
  logic from my files (e.g. `confirmBooking()`, `addDriver()`).
- For the `JTable`s, right-click → **Table Contents...** to define columns,
  or just do it in code with `DefaultTableModel` as I did — easier to keep
  in sync with `DataStore`.

**Important:** the files I generated are plain hand-written Swing code, so
they'll compile, run, and can be freely edited in NetBeans's **Source**
view — but the **Design (drag-and-drop) view** only renders for files
NetBeans itself generated via "New JFrame/JPanel Form" (it needs its own
`// GEN-BEGIN`/`// GEN-END` markers). If you want live drag-and-drop editing
of a screen, create a fresh Form via the wizard above and rebuild it using
the component list in that file's header comment — then copy over the event
logic.

## 4. Natural next steps

- **Persistence**: replace the internals of `DataStore.java` with JDBC calls
  (MySQL/SQLite) — the rest of the app won't need to change since every
  screen only talks to `DataStore.get()`.
- **Real fare calculation**: hook `updateFareEstimate()` in
  `NewReservationPanel` up to a distance API or a per-km rate instead of the
  flat per-type estimate.
- **Roles**: add a customer-facing login mode vs. admin/dispatcher mode.
- **Notifications**: use a `Timer` to simulate driver location/status
  updates, or wire in real GPS data.

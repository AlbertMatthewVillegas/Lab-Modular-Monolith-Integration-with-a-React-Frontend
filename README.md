# Supabase Setup

- Java 17+
- Node.js 18+
- A free Supabase project
- Maven Wrapper included in `backend/villegas`

## 1. Create the Supabase database

1. Create or open a project at [supabase.com](https://supabase.com).
2. Open **SQL Editor** and run the following script.

The complete script is available at [schema.sql](schema.sql).

The `orders.product_id` foreign key references `inventory.product_id`. Keep the database tables in the `public` schema.

## 2. Configure backend credentials

Create a `.env` file in the repository root. It is ignored by Git; never commit it.

In Supabase, open **Project Settings > Database > Connect**, choose the transaction pooler connection, and copy its host, port, username, database name, and password into the JDBC URL:

```properties
DB_URL=jdbc:postgresql://<pooler-host>:6543/postgres?sslmode=require
DB_USERNAME=postgres.<project-ref>
DB_PASSWORD=<your-database-password>
FRONTEND_ORIGIN=http://localhost:5173
```

Use the exact host and username shown by Supabase. If the project uses the direct connection instead, use its port and username from the Supabase connection dialog.

The backend reads these values through `backend/villegas/src/main/resources/application.properties`. Hibernate schema changes are disabled with `ddl-auto=none`, so the SQL above must be run in Supabase first.

## Network Tab Evidence

Rejected order request: the Network tab shows `POST /api/orders` returning `200`, and the UI displays `REJECTED`.

![Rejected order Network tab evidence](<evidence/Screenshot 2026-09-10 at 8.00.07 PM.png>)

Confirmed order request: the Network tab shows `POST /api/orders` returning `200`, and the UI displays `CONFIRMED`.

![Confirmed order Network tab evidence](<evidence/Screenshot 2026-09-10 at 8.00.22 PM.png>)

## Architecture Reflection

### 1. What differs between integrating Order/Inventory in-process vs. as separate microservices over a network --> what do you get for free, and what would you need to add back if split?

### 2. Why does package-private visibility on InventoryServiceImpl matter for the module boundary --> what breaks if it's public?

### 3. When would you extract Inventory into its own microservice, and what would need to change in your code to do it?

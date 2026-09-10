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

Keeping modules inside the same application gives you reliable database transactions and instant communication for free. If an order fails, Spring can easily cancel the inventory update using a simple database rollback without any extra networking code. If you split them into separate microservices communicating over a network, everything becomes much more complicated. You lose that shared database safety net, meaning you have to build extra tools to handle network dropouts, server crashes, and tracking requests across different machines. You would have to add API gateways, circuit breakers, and tools to handle delayed data updates across systems.

### 2. Why does package-private visibility on InventoryServiceImpl matter for the module boundary --> what breaks if it's public?

Package-private visibility acts like a locked door between your project folders. By hiding the InventoryServiceImpl class and only sharing its interface, you force the Order module to interact with inventory through a strictly defined rulebook. If the implementation class were public, other parts of the app could accidentally bypass those rules and call the inventory logic directly. This creates messy, tightly-coupled code where changing how inventory works could suddenly break the order system, completely defeating the purpose of organizing your code into clean, independent modules.

### 3. When would you extract Inventory into its own microservice, and what would need to change in your code to do it?

You should only split inventory into its own microservice when the business grows such as handling massive traffic spikes for flash sales that need separate server scaling or when different developer teams need to release updates independently. To make this happen, you would have to rewrite code to stop using direct Java method calls and instead use network requests, like HTTP clients. You would also need to separate the single shared database into two independent databases and set up a system for them to sync data safely without breaking each other.

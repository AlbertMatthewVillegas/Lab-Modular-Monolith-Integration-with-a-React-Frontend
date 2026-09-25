-- Run this file in the Supabase SQL Editor.
-- It recreates the complete schema and seed data from scratch.

create extension if not exists pgcrypto;

drop table if exists public.notifications cascade;
drop table if exists public.order_items cascade;
drop table if exists public.orders cascade;
drop table if exists public.inventory cascade;

create table public.inventory (
    product_id uuid primary key,
    name text not null,
    price numeric(12, 2) not null check (price >= 0),
    stock integer not null check (stock >= 0)
);

create table public.orders (
    order_id uuid primary key default gen_random_uuid(),
    -- Kept for compatibility with the current Spring Order entity.
    product_id uuid references public.inventory(product_id),
    quantity integer check (quantity > 0),
    status text not null check (status in ('CONFIRMED', 'REJECTED', 'CANCELLED')),
    reason text,
    created_at timestamptz not null default now()
);

create table public.order_items (
    product_id uuid not null references public.inventory(product_id),
    order_id uuid not null references public.orders(order_id) on delete cascade,
    price numeric(12, 2) not null check (price >= 0),
    quantity integer not null check (quantity > 0),
    primary key (order_id, product_id)
);

create table public.notifications (
    notification_id uuid primary key default gen_random_uuid(),
    order_id uuid references public.orders(order_id) on delete set null,
    product_id uuid,
    message text not null,
    created_at timestamptz not null default now()
);

create table public.supplier_orders (
    id uuid primary key default gen_random_uuid(),
    product_id uuid not null references public.inventory(product_id),
    buyer_ref text not null unique,
    request_id text not null unique,
    po_number text,
    cases integer not null check (cases > 0),
    units integer not null check (units > 0),
    status text not null check (status in ('PENDING', 'ACCEPTED', 'PICKING', 'SHIPPED', 'DELIVERED', 'FAILED', 'UNKNOWN')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

insert into public.inventory (product_id, name, price, stock)
values
    ('550e8400-e29b-41d4-a716-446655440100', 'Wireless Mouse', 24.99, 25),
    ('550e8400-e29b-41d4-a716-446655440200', 'Mechanical Keyboard', 79.99, 10),
    ('550e8400-e29b-41d4-a716-446655440300', 'USB-C Hub', 19.99, 0);

-- Verify the recreated schema:
select * from public.inventory order by product_id;
select * from public.orders order by created_at desc;
select * from public.order_items order by order_id, product_id;
select * from public.notifications order by created_at desc;

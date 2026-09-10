-- Run this file in the Supabase SQL Editor.
-- It creates the inventory and orders tables and seeds the catalog.

create extension if not exists pgcrypto;

create table if not exists public.inventory (
    product_id text primary key,
    name text not null,
    stock integer not null check (stock >= 0)
);

create table if not exists public.orders (
    order_id uuid primary key default gen_random_uuid(),
    product_id text not null references public.inventory(product_id),
    quantity integer not null check (quantity > 0),
    status text not null check (status in ('CONFIRMED', 'REJECTED')),
    reason text,
    created_at timestamptz not null default now()
);

insert into public.inventory (product_id, name, stock)
values
    ('P100', 'Wireless Mouse', 25),
    ('P200', 'Mechanical Keyboard', 10),
    ('P300', 'USB-C Hub', 0)
on conflict (product_id) do update
set name = excluded.name;

-- Verify the setup:
select * from public.inventory order by product_id;
select * from public.orders order by created_at desc;

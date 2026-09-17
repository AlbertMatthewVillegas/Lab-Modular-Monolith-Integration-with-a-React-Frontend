-- Run this file in the Supabase SQL Editor.
-- It recreates the complete schema and seed data from scratch.

create extension if not exists pgcrypto;

drop table if exists public.notifications cascade;
drop table if exists public.order_items cascade;
drop table if exists public.orders cascade;
drop table if exists public.inventory cascade;

create table public.inventory (
    product_id text primary key,
    name text not null,
    stock integer not null check (stock >= 0)
);

create table public.orders (
    order_id uuid primary key default gen_random_uuid(),
    -- Kept for compatibility with the current Spring Order entity.
    product_id text references public.inventory(product_id),
    quantity integer check (quantity > 0),
    status text not null check (status in ('CONFIRMED', 'REJECTED', 'CANCELLED')),
    reason text,
    created_at timestamptz not null default now()
);

create table public.order_items (
    order_id uuid not null references public.orders(order_id) on delete cascade,
    product_id text not null references public.inventory(product_id),
    quantity integer not null check (quantity > 0),
    primary key (order_id, product_id)
);

create table public.notifications (
    notification_id uuid primary key default gen_random_uuid(),
    order_id uuid references public.orders(order_id) on delete set null,
    product_id text references public.inventory(product_id) on delete set null,
    message text not null,
    created_at timestamptz not null default now()
);

insert into public.inventory (product_id, name, stock)
values
    ('P100', 'Wireless Mouse', 25),
    ('P200', 'Mechanical Keyboard', 10),
    ('P300', 'USB-C Hub', 0);

-- Verify the recreated schema:
select * from public.inventory order by product_id;
select * from public.orders order by created_at desc;
select * from public.order_items order by order_id, product_id;
select * from public.notifications order by created_at desc;

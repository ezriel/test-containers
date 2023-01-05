create schema customers;
create table customers.customer (
    customer_id serial not null,
    first_name varchar(64),
    last_name varchar(128),
    pesel varchar(12),
    created_at timestamp,
    CONSTRAINT customer_pk PRIMARY KEY (customer_id)
);
create table customers.address
(
    id serial,
    customer_id serial not null,
    type int,
    address     varchar(256),
    created_at  timestamp,
    CONSTRAINT address_pk PRIMARY KEY (id),
    CONSTRAINT address_customer_fk FOREIGN KEY (customer_id) REFERENCES customers.customer(customer_id)
);
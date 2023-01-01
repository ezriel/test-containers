create table customer
(
    customer_id serial not null,
    first_name  varchar(64),
    last_name   varchar(128),
    pesel       varchar(12),
    created_at  timestamp,
    CONSTRAINT customer_pk PRIMARY KEY (customer_id)
);
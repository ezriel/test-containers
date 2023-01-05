create table address
(
    id serial,
    customer_id serial not null,
    type int,
    address     varchar(256),
    created_at  timestamp,
    CONSTRAINT address_pk PRIMARY KEY (id),
    CONSTRAINT address_customer_fk FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE INDEX address_customer_idx ON address (customer_id);
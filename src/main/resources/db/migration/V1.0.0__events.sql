CREATE SCHEMA IF NOT EXISTS ctf_example;

CREATE TABLE ctf_example.outbox_event (
                              id uuid NOT NULL,
                              destination varchar(255) NULL,
                              payload varchar(4096) NULL,
                              timestamp int8 NOT NULL,
                              version varchar(255) NULL,
                              CONSTRAINT outbox_pkey PRIMARY KEY (id)
);


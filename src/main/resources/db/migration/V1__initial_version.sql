-- From super user, uncomment for tests
--create extension if not exists "uuid-ossp";

create table client (
	id uuid primary key default uuid_generate_v4(),
	name text not null
);

insert into client (name) values ('nick');


create table organization (
    code varchar(3) primary key
);

insert into organization values ('SBR'), ('ALF'), ('TKF');


create table currency (
    code varchar(3) primary key
);

insert into currency values ('RUB'), ('USD'), ('EUR');


create table account (
	id uuid primary key default uuid_generate_v4(),
	client_id uuid,
	org_code varchar(3),
	currency varchar(3),
	balance numeric,
	FOREIGN KEY (client_id) REFERENCES client (id),
	FOREIGN KEY (org_code) REFERENCES organization (code),
	FOREIGN KEY (currency) REFERENCES currency (code)
);

create table tran (
	id uuid primary key default uuid_generate_v4(),
	date timestamp without time zone default now(),
	account_id uuid,
	amount numeric,
	FOREIGN KEY (account_id) REFERENCES account (id)
);

create table exchange_rate (
	id integer Primary Key,
	date timestamp without time zone,
	currency integer,
	mult numeric
);
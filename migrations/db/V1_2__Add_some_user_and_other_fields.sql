alter table rooms
    add column location varchar(240) not null default '1825 Monetary Ln #104 Carrollton, TX 75006';

create table contacts
(
    id           uuid        not null primary key,
    name         varchar(80) not null,
    phone        varchar(20) null default null,
    email        varchar(70) null default null,
    created      timestamp        default now(),
    last_updated timestamp        default now()
);

alter table events
    add constraint contact_id_fk foreign key (creator_id) references contacts (id);

create table contact_sso_sub_binding
(
    contact_id     uuid not null references contacts (id),
    sub_value      varchar(255),
    oauth_provider varchar(255)
)
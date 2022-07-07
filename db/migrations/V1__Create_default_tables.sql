create table events
(
    id                         uuid             not null primary key,
    name                       varchar(80)      not null,
    short_description          varchar(250)     not null,
    long_description           varchar          null     default null,
    advisories                 varchar          null     default null,
    event_start                timestamp        not null,
    event_end                  timestamp        not null,
    member_cost                double precision not null default 0,
    non_member_cost            double precision not null default 0,
    eventbrite_link            varchar(255)     null     default null,
    members_only               boolean          not null default true,
    age_restriction            smallint         not null default 0,
    attendees_require_approval boolean          not null default false,
    attendee_cancellation_by   timestamp        not null,
    sponsored                  boolean          not null default false,
    status                     varchar(30)      not null default 'pending' CHECK ( status in ('approved', 'completed', 'cancelled', 'pending', 'rejected')),
    creator_id                 uuid             not null,
    archetype                  varchar(255)     null     default null,
    copied_from                uuid             null     default null references events (id),
    created                    timestamp        not null default now(),
    updated                    timestamp        not null default now()
);

create table rooms
(
    id          uuid         not null primary key,
    name        varchar(80)  not null,
    capacity    smallint     not null,
    limitations varchar(255) not null default '' -- This is useful for having information like 'No power'/etc
);

create table room_features
(
    id   uuid        not null primary key,
    name varchar(80) not null
);

create table equipment
(
    id   uuid        not null primary key,
    name varchar(80) not null
);

create table prerequisites
(
    id       uuid        not null primary key,
    name     varchar(80) not null,
    ad_group varchar(80) not null
);

create table room_booking
(
    event_id   uuid      not null references events (id),
    room_id    uuid      not null references rooms (id),
    start_time timestamp not null,
    end_time   timestamp not null
);

create table equipment_booking
(
    event_id     uuid      not null references events (id),
    equipment_id uuid      not null references equipment (id),
    start_time   timestamp not null,
    end_time     timestamp not null,
    sharable     boolean   not null default false
);

create table equipment_requires_prerequisite_binding
(
    equipment_id     uuid    not null references equipment (id),
    prerequisites    uuid    not null references prerequisites (id),
    soft_requirement boolean not null default false
);

create table event_requires_prerequisite_binding
(
    event_id         uuid    not null references events (id),
    prerequisites    uuid    not null references prerequisites (id),
    soft_requirement boolean not null default false
);

create table event_gives_prerequisite_binding
(
    event_id      uuid not null references events (id),
    prerequisites uuid not null references prerequisites (id)
);

create table room_feature_binding
(
    room_id    uuid     not null references rooms (id),
    feature_id uuid     not null references room_features (id),
    count      smallint not null default 1
);

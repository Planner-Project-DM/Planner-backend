create table city_visited
(
    id         uuid not null
        primary key,
    city       varchar(255),
    visited_at timestamp(6)
);

alter table city_visited
    owner to postgres;

create table groups
(
    id   uuid not null
        primary key,
    name varchar(255)
);

alter table groups
    owner to postgres;

create table trip_items
(
    id           uuid not null
        primary key,
    city         varchar(255),
    country      varchar(255),
    house_number varchar(255),
    postal_code  varchar(255),
    street       varchar(255),
    category     varchar(255)
        constraint trip_items_category_check
            check ((category)::text = ANY
        ((ARRAY ['HOTEL'::character varying, 'ATTRACTION'::character varying, 'FOOD'::character varying, 'OTHER'::character varying])::text[])),
    description  varchar(255),
    email        varchar(255),
    latitude     numeric(11, 8),
    longitude    numeric(11, 8),
    name         varchar(255),
    phone_number varchar(255),
    price        double precision,
    stars        varchar(255),
    tourism      varchar(255),
    website      varchar(255)
);

alter table trip_items
    owner to postgres;

create table schedules
(
    id           uuid    not null
        primary key,
    created_at   timestamp(6),
    end_time     timestamp(6),
    start_time   timestamp(6),
    trip_item_id uuid
        constraint fkoli1dhc28ahny1n1760kq4db3
            references trip_items,
    all_day      boolean not null
);

alter table schedules
    owner to postgres;

create table users
(
    id           uuid default gen_random_uuid() not null
        primary key,
    email        varchar(255)
        constraint uk6dotkott2kjsp8vw4d0m25fb7
            unique,
    first_name   varchar(255),
    is_active    boolean,
    last_name    varchar(255),
    password     varchar(255),
    phone_number varchar(255),
    role         varchar(255)
        constraint users_role_check
            check ((role)::text = ANY ((ARRAY ['USER'::character varying, 'ADMIN'::character varying])::text[]))
    );

alter table users
    owner to postgres;

create table friendships
(
    id               uuid not null
        primary key,
    created_at       timestamp(6),
    status           varchar(255)
        constraint friendships_status_check
            check ((status)::text = ANY
        ((ARRAY ['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'BLOCKED'::character varying])::text[])),
    user_receiver_id uuid
        constraint fkie6g8ynfis09qrlc6ns3e2xbr
            references users,
    user_sender_id   uuid
        constraint fkdbhj0ydxby5ptv3qgifh42kwo
            references users
);

alter table friendships
    owner to postgres;

create table group_user
(
    group_id uuid not null
        constraint fkm4p7t99vp509n4lt2et6hqkgn
            references groups,
    user_id  uuid not null
        constraint fkrqeo92wyuy7jcc54mfbln3wme
            references users,
    role     varchar(255)
        constraint group_user_role_check
            check ((role)::text = ANY
        ((ARRAY ['MEMBER'::character varying, 'ADMIN'::character varying, 'OWNER'::character varying])::text[])),
    balance  double precision,
    primary key (group_id, user_id)
);

alter table group_user
    owner to postgres;

create table trips
(
    id              uuid not null
        primary key,
    actual_cost     double precision,
    budget          double precision,
    created_at      timestamp(6),
    destination     varchar(255),
    end_date        date,
    name            varchar(255),
    start_date      date,
    status          varchar(255)
        constraint trips_status_check
            check ((status)::text = ANY
        ((ARRAY ['PLANNED'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])),
    trip_creator_id uuid
        constraint fksrvr0s0jw6q5gs94a0ltv2fm3
            references users,
    trip_group_id   uuid
        constraint ukq5gwgk5c7p28o3uoch5x9k0c4
            unique
        constraint fk94yimd0w3c8p3ck0l58sujyny
            references groups,
    constraint uk_trip_name_and_creator
        unique (name, trip_creator_id)
);

alter table trips
    owner to postgres;

create table trip_itineraries
(
    trip_id      uuid not null
        constraint fkrwra1d6x36t5730v79lskrc9e
            references trips,
    trip_item_id uuid not null
        constraint fkacljods417luejo71gve6nq8v
            references trip_items,
    price        double precision,
    primary key (trip_id, trip_item_id)
);

alter table trip_itineraries
    owner to postgres;

create table trip_schedules
(
    schedule_id uuid not null
        constraint fkkglde9tu5bwpq6bd4p59iekhf
            references schedules,
    trip_id     uuid not null
        constraint fknlb5onats1x3ig96qo3rt7xm4
            references trips,
    primary key (schedule_id, trip_id)
);

alter table trip_schedules
    owner to postgres;

create table notifications
(
    id          uuid not null
        primary key,
    created_at  timestamp(6),
    message     varchar(255),
    status      varchar(255)
        constraint notifications_status_check
            check ((status)::text = ANY
        ((ARRAY ['PENDING'::character varying, 'SENT'::character varying, 'READ'::character varying])::text[])),
    title       varchar(255),
    receiver_id uuid
        constraint fk9kxl0whvhifo6gw4tjq36v53k
            references users,
    sender_id   uuid
        constraint fk13vcnq3ukas06ho1yrbc5lrb5
            references users
);

alter table notifications
    owner to postgres;

create table user_settings
(
    id                      uuid    not null
        primary key,
    budget_limit            double precision,
    currency                varchar(255)
        constraint user_settings_currency_check
            check ((currency)::text = ANY
        ((ARRAY ['PLN'::character varying, 'USD'::character varying, 'EUR'::character varying])::text[])),
    is_notification_enabled boolean not null,
    language                varchar(255)
        constraint user_settings_language_check
            check ((language)::text = ANY ((ARRAY ['PL'::character varying, 'EN'::character varying])::text[])),
    user_id                 uuid    not null
        constraint uk4bos7satl9xeqd18frfeqg6tt
            unique
        constraint fk8v82nj88rmai0nyck19f873dw
            references users
);

alter table user_settings
    owner to postgres;


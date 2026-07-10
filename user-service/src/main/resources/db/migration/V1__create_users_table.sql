create table users (
    id bigserial primary key,
    username varchar(50) not null unique,
    email varchar(120) not null unique,
    password_encrypted varchar(255) not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    enabled boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table user_roles (
    user_id bigint not null references users (id) on delete cascade,
    role varchar(30) not null,
    primary key (user_id, role)
);

create index idx_users_username on users (username);
create index idx_users_email on users (email);

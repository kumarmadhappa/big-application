create table bank_users (
    id bigserial primary key,
    username varchar(50) not null unique,
    email varchar(120) not null unique,
    password_encrypted varchar(255) not null,
    enabled boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table bank_user_roles (
    user_id bigint not null references bank_users (id) on delete cascade,
    role varchar(30) not null,
    primary key (user_id, role)
);

create table account_holders (
    id bigserial primary key,
    user_id bigint not null unique references bank_users (id) on delete cascade,
    holder_type varchar(20) not null,
    display_name varchar(150) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table bank_accounts (
    id bigserial primary key,
    account_number varchar(30) not null unique,
    account_segment varchar(20) not null,
    account_type varchar(20) not null,
    balance numeric(19,2) not null,
    credit_limit numeric(19,2),
    holder_id bigint not null references account_holders (id) on delete cascade,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table bank_transactions (
    id bigserial primary key,
    account_id bigint not null references bank_accounts (id) on delete cascade,
    transaction_type varchar(20) not null,
    amount numeric(19,2) not null,
    balance_before numeric(19,2) not null,
    balance_after numeric(19,2) not null,
    performed_by varchar(50) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_bank_users_username on bank_users (username);
create index idx_bank_users_email on bank_users (email);
create index idx_bank_accounts_holder_id on bank_accounts (holder_id);
create index idx_bank_transactions_account_id on bank_transactions (account_id);

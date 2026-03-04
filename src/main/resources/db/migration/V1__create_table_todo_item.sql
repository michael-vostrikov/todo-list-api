create table todo_item (
    id integer auto_increment primary key,
    description varchar(2000) not null,
    status varchar(100) not null,
    created_at timestamp with time zone not null,
    due_date timestamp with time zone not null,
    done_at timestamp with time zone null
);

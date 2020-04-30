create table if not exists queue (
    player_id integer not null references players(id),
    request_time timestamp not null,
    primary key (player_id)
)
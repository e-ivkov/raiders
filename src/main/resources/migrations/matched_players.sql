create table if not exists matched_players (
    match_id integer not null references matches(id) on delete cascade,
    player_id integer not null references players(id) on delete cascade,
    primary key (player_id)
)
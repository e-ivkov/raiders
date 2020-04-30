create table if not exists matched_players (
    match_id integer not null references matches(id),
    player_id integer not null references players(id),
    primary key (player_id)
)
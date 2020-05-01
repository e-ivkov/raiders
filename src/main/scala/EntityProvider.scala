trait EntityProvider {
  def players: Entities.Players
  def queue: Entities.Queue
  def matches: Entities.Matches
  def matchedPlayers: Entities.MatchedPlayers
}

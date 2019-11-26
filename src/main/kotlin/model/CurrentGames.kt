package model

data class CurrentGame (
    var config : GameConfig,
    var lastTimeAnnouncement : Long,
    val players: GamePlayers
    )

data class CurrentGames (
    val currentGames : MutableMap<Pair<String, Int>, CurrentGame>
)
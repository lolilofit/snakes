package model

data class CurrentGame (
    var config : GameConfig,
    var lastTimeAnnouncement : Long,
    val players: GamePlayers
    )

data class CurrentGames (
        //master adr and game
    val currentGames : MutableMap<Pair<String, Int>, CurrentGame> = HashMap()
)
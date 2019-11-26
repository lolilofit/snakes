package model


data class GameState(
        var state_order : Int,
        var snakes : ArrayList<Snake>,
        val foods : ArrayList<Coord>,
        val game_players : GamePlayers,
        val config : GameConfig
)
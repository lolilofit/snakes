package model

import me.ippolitov.fit.snakes.SnakesProto

data class GamePlayer(
        val name : String,
        val id : Int,
        val ip_address : String,
        val port : Int,
        var role : SnakesProto.NodeRole,
        var score : Int
)
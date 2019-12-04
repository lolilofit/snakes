package model

import me.ippolitov.fit.snakes.SnakesProto

data class GamePlayer(
        val name : String,
        val id : Int,
        var ip_address : String,
        var port : Int,
        var role : SnakesProto.NodeRole,
        var score : Int
)
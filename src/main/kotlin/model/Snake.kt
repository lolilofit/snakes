package model

import me.ippolitov.fit.snakes.SnakesProto

data class Snake (
        val player_id : Int,
        var points : ArrayList<Coord>,
        var snakeState : SnakesProto.GameState.Snake.SnakeState,
        var direction: SnakesProto.Direction
)
package presentation

import creator.config
import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.*
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

object ProtoAdapter {
    fun getProtoConfig(localConfig: GameConfig) : SnakesProto.GameConfig.Builder {
        val config : SnakesProto.GameConfig.Builder = SnakesProto.GameConfig.newBuilder()
        val classObj = localConfig::class
        classObj.memberProperties.forEach{field ->
            var methodName = field.name
            methodName = "set" + methodName[0].toUpperCase() + methodName.slice(1 until (methodName.length))
            val setter = config::class.memberFunctions
                    .filter { it.name == methodName }
                    .first()
            if (field.visibility == KVisibility.PUBLIC) {
                val value = field.getter.call(localConfig)
                setter.call(config, value)
            }
        }
        return config
    }

    fun getProtoGamePlayers(gamePlayers: GamePlayers) : SnakesProto.GamePlayers.Builder {
        val players : SnakesProto.GamePlayers.Builder = SnakesProto.GamePlayers.newBuilder()
        gamePlayers.players.forEach{player ->
            players.addPlayers(SnakesProto.GamePlayer.newBuilder()
                    .setId(player.id)
                    .setIpAddress(player.ip_address)
                    .setName(player.name)
                    .setPort(player.port)
                    .setScore(player.score)
                    .setRole(player.role))
        }
        return players
    }
    fun getConfig(protoConfig : SnakesProto.GameConfig) = GameConfig(
                protoConfig.width,
                protoConfig.height,
                protoConfig.foodStatic,
                protoConfig.foodPerPlayer,
                protoConfig.stateDelayMs,
                protoConfig.pingDelayMs,
                protoConfig.deadFoodProb,
                protoConfig.nodeTimeoutMs
    )


    fun getPlayers(protoGamePlayer : SnakesProto.GamePlayers) : GamePlayers {
        val listPlayers = ArrayList<GamePlayer>()
        protoGamePlayer.playersList.forEach{player ->
            listPlayers.add(GamePlayer(player.name, player.id, player.ipAddress, player.port, player.role, player.score))
        }
        return GamePlayers(listPlayers)
    }

    fun getProtoListCoords(localList : List<Coord>) : List<SnakesProto.GameState.Coord> {
        val list = ArrayList<SnakesProto.GameState.Coord>()
        localList.forEach{coord ->
            list.add(SnakesProto.GameState.Coord.newBuilder().setX(coord.x).setY(coord.y).build())
        }
        return list
    }

    fun getProtoSnakes(snakes : List<Snake>) : List<SnakesProto.GameState.Snake> {
        val list = ArrayList<SnakesProto.GameState.Snake>()
        snakes.forEach {snake ->
            list.add(SnakesProto.GameState.Snake.newBuilder()
                    .setPlayerId(snake.player_id)
                    .setHeadDirection(snake.direction)
                    .setState(snake.snakeState)
                    .addAllPoints(getProtoListCoords(snake.points))
                    .build())
        }
        return list
    }

    fun getProtoGameState(localGameState: GameState) =
         SnakesProto.GameState.newBuilder().setConfig(getProtoConfig(localGameState.config))
                 .setStateOrder(localGameState.state_order)
                 .setPlayers(getProtoGamePlayers(localGameState.game_players))
                 .addAllFoods(getProtoListCoords(localGameState.foods))
                 .addAllSnakes(getProtoSnakes(localGameState.snakes))

    fun getListCoord(protoList : List<SnakesProto.GameState.Coord>) : ArrayList<Coord> {
        val list = ArrayList<Coord>()
        protoList.forEach{protoCoord -> list.add(Coord(protoCoord.x, protoCoord.y))}
        return list
    }

    fun getSnakes(protoSnakes : List<SnakesProto.GameState.Snake>) : ArrayList<Snake> {
        val list = ArrayList<Snake>()
        protoSnakes.forEach{protoSnake ->
            list.add(Snake(protoSnake.playerId, getListCoord(protoSnake.pointsList), protoSnake.state, protoSnake.headDirection))
        }
        return list
    }

    fun getGameState(protoGameState: SnakesProto.GameState) : GameState {
        val gameState = GameState(protoGameState.stateOrder,
                getSnakes(protoGameState.snakesList),
                getListCoord(protoGameState.foodsList),
                getPlayers(protoGameState.players),
                getConfig(protoGameState.config)
                )
        return gameState
    }
}
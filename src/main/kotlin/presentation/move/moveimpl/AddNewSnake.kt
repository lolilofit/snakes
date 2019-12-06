package presentation.move.moveimpl

import creator.config
import creator.globalState
import creator.mesSeq
import me.ippolitov.fit.snakes.SnakesProto
import model.Coord
import model.Field
import model.Snake
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.move.Move
import java.net.DatagramPacket
import java.net.InetAddress
import kotlin.random.Random

object AddNewSnake : Move{
    override fun execute(param: List<Any?>) {
        if (param.size != 2) return
        val playerId = if (param[0] is Int) (param[0] as Int) else return
        val field = if (param[1] is Field) (param[1] as Field) else return
        var stepX = 1
        var findStep = 0
        var flag = true
        var pointX = 0
        var pointY = 0
        var height = 0
        var width = 0
        synchronized(ImmediateQueue::class) {
            height = config.height
            width = config.width
        }
        loop@ for (y in 0 until (height)) {
            //for (x in 0 until (width) step stepX) {
            var x = 0
            while(x < width) {
                stepX = 1
                flag = true
                findStep = 0
                loop1@ for (y1 in 0 until (5)) {
                    for (x1 in 4 downTo 0 step 1) {
                        if (field.field[(y + y1 + height)%height][(x + x1 + width) % width][0] != 0) {
                            while (field.field[y + y1][(x + x1 + findStep + width)%width][0] != 0) {
                                findStep++
                            }
                            stepX = x1 + findStep
                            flag = false
                            break@loop1
                        }
                    }
                }
                if (flag == true) {
                    pointX = x
                    pointY = y
                    break@loop
                }
                x += stepX
            }
        }
        if (flag) {
            val directions = listOf(
                    Pair(SnakesProto.Direction.UP, Coord(0, 1)),
                    Pair(SnakesProto.Direction.DOWN, Coord(0, -1)),
                    Pair(SnakesProto.Direction.LEFT, Coord(1, 0)),
                    Pair(SnakesProto.Direction.RIGHT, Coord(-1, 0)))
            val pos = Random.nextInt(0, 4)
            val snake = Snake(playerId, ArrayList(listOf(Coord(pointX + 2, pointY + 2),
                    directions[pos].second)), SnakesProto.GameState.Snake.SnakeState.ALIVE, directions[pos].first)
            synchronized(ImmediateQueue::class) {
                globalState.snakes.add(snake)
            }
        }
        else {
            val message = SnakesProto.GameMessage.newBuilder()
                    .setMsgSeq(mesSeq.get())
                    .setError(SnakesProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage("Can't find free place in field"))
                    .build()
            val binaryMessage = message.toByteArray()
            val masterInfo = globalState.game_players.players.stream().filter{it.role == SnakesProto.NodeRole.MASTER}.findFirst()
            if(!masterInfo.isPresent) return
            val packet = DatagramPacket(binaryMessage, binaryMessage.size, InetAddress.getByName(masterInfo.get().ip_address), masterInfo.get().port)
            SelfInfo.regularSocket.send(packet)
            mesSeq.incrementAndGet()
        }
    }
}
package presentation.move.moveimpl

import creator.globalState
import creator.mesSeq
import me.ippolitov.fit.snakes.SnakesProto
import model.GamePlayer
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.TimeoutQueue
import presentation.move.Move
import presentation.notmaster.Resender
import java.net.DatagramPacket
import java.net.InetAddress
import java.util.*


object MoveFromButton : Move {
    override fun execute(param: List<Any?>) {
        val direction = if(param[0] is SnakesProto.Direction) param[0] as SnakesProto.Direction else return
        var masterInfo : Optional<GamePlayer>
        synchronized(ImmediateQueue::class) {
                masterInfo = globalState.game_players.players.stream().filter{it.role == SnakesProto.NodeRole.MASTER}.findFirst()
                if(!masterInfo.isPresent) return
                if (masterInfo.get().id != -1 && masterInfo.get().id == SelfInfo.selfId) {
                    //find my snake
                    val mySnakeOptional = globalState.snakes.stream().filter { it.player_id == SelfInfo.selfId }.findFirst()
                    if (mySnakeOptional.isPresent) {
                        val mySnake = mySnakeOptional.get()
                        if (param.size == 1) {
                            TimeoutQueue.addToQueue(SteerMsgImpl, listOf(direction, mySnake))
                        }
                    }
                    return
                }
        }

        val message = SnakesProto.GameMessage.newBuilder()
                    .setSteer(SnakesProto.GameMessage.SteerMsg.newBuilder().setDirection(direction))
                    .setSenderId(SelfInfo.selfId)
                    .setMsgSeq(mesSeq.get())
                    .build()
        val binaryMessage = message.toByteArray()
        val packet = DatagramPacket(binaryMessage, binaryMessage.size, InetAddress.getByName(SelfInfo.masterIp), SelfInfo.masterPort)
        SelfInfo.regularSocket.send(packet)
        mesSeq.incrementAndGet()
        Resender.addToResendQueue(packet.address.toString().replace("/", ""), packet.port, message, System.currentTimeMillis())
        //send to master
    }
}
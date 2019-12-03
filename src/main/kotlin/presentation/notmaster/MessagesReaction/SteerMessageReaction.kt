package presentation.notmaster.MessagesReaction

import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGames
import model.Snake
import presentation.ImmediateQueue
import presentation.master.TimeoutQueue
import presentation.move.moveimpl.SendAck
import presentation.move.moveimpl.SteerMsgImpl
import presentation.notmaster.MessagesType
import view.View
import java.net.DatagramPacket
import java.util.concurrent.atomic.AtomicLong

object SteerMessageReaction : MessagesType {
    override fun execute(protoElement: SnakesProto.GameMessage, packet: DatagramPacket, currentGames: CurrentGames, view: View, acked: MutableMap<Pair<String, Int>, AtomicLong>) {
        var flag = true
        val el = acked[Pair(packet.address.toString().replace("/", ""), packet.port)]
        if(el != null) {
            if(el.get() >= protoElement.msgSeq) {
                flag = false
            }
        }
        else {
            acked[Pair(packet.address.toString().replace("/", ""), packet.port)] = AtomicLong(protoElement.msgSeq)
        }

        if(flag) {
            val snake: Snake
            synchronized(ImmediateQueue::class) {
                val snakeOptional = globalState.snakes.stream().filter { it.player_id == protoElement.senderId }.findFirst()
                if (!snakeOptional.isPresent) return
                snake = snakeOptional.get()
            }
            TimeoutQueue.addToQueue(SteerMsgImpl, listOf(protoElement.steer.direction, snake))
            el?.set(protoElement.msgSeq)
        }
        SendAck.execute(listOf(packet.address.toString().replace("/", ""), packet.port, protoElement.msgSeq))
    }
}
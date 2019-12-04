package presentation.move.moveimpl

import creator.globalState
import creator.mesSeq
import me.ippolitov.fit.snakes.SnakesProto
import presentation.ImmediateQueue
import presentation.ProtoAdapter
import presentation.SelfInfo
import presentation.move.Move
import presentation.notmaster.Resender
import java.net.DatagramPacket
import java.net.InetAddress

object StateMsgImpl : Move{
    override fun execute(param: List<Any?>) {
        lateinit var message : SnakesProto.GameMessage
        synchronized(ImmediateQueue::class) {
            globalState.game_players.players.forEach { player ->
                if(player.ip_address != "") {
                    message = SnakesProto.GameMessage.newBuilder()
                        .setMsgSeq(mesSeq.get())
                        .setReceiverId(player.id)
                        .setState(SnakesProto.GameMessage.StateMsg.newBuilder().setState(ProtoAdapter.getProtoGameState(globalState)))
                        .build()
                    val binaryMessage = message.toByteArray()
                    val packet = DatagramPacket(binaryMessage, binaryMessage.size, InetAddress.getByName(player.ip_address), player.port)
                    SelfInfo.regularSocket.send(packet)
                    mesSeq.incrementAndGet()

                   Resender.addToResendQueue(packet.address.toString().replace("/", ""), packet.port, message, System.currentTimeMillis(), player.id)
                }
            }
        }
    }

}
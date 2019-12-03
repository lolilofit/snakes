package presentation.move.moveimpl

import creator.mesSeq
import me.ippolitov.fit.snakes.SnakesProto
import presentation.SelfInfo
import presentation.move.Move
import java.net.DatagramPacket
import java.net.InetAddress

object SendPing: Move {
    override fun execute(param: List<Any?>) {

        val ip = if(param[0] is String) param[0] as String else return
        val port = if(param[1] is Int) param[1] as Int else return

        val message = SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(mesSeq.incrementAndGet()).setPing(SnakesProto.GameMessage.PingMsg.newBuilder())
                .build()
        val binaryMessage = message.toByteArray()
        val packet = DatagramPacket(binaryMessage, binaryMessage.size, InetAddress.getByName(ip), port)
        SelfInfo.regularSocket.send(packet)
    }

}
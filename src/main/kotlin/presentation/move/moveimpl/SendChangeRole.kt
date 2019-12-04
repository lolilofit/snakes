package presentation.move.moveimpl

import creator.mesSeq
import me.ippolitov.fit.snakes.SnakesProto
import presentation.SelfInfo
import presentation.move.Move
import java.net.DatagramPacket
import java.net.InetAddress

object SendChangeRole : Move {
    override fun execute(param: List<Any?>) {
        if(param.size != 6) return
        val ip = if(param[0] is String) param[0] as String else return
        val port = if(param[1] is Int) param[1] as Int else return
        val senderRole = if(param[2] is SnakesProto.NodeRole) param[2] as SnakesProto.NodeRole else return
        val receiverRole = if(param[3] is SnakesProto.NodeRole) param[3] as SnakesProto.NodeRole else return
        val senderId = if(param[4] is Int) param[4] as Int else return
        val recverId = if(param[5] is Int) param[5] as Int else return

        val message = SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(mesSeq.incrementAndGet())
                .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg.newBuilder().setSenderRole(senderRole).setReceiverRole(receiverRole))
                .setSenderId(senderId)
                .setReceiverId(recverId)
                .build()
        val binaryMessage = message.toByteArray()
        val packet = DatagramPacket(binaryMessage, binaryMessage.size, InetAddress.getByName(ip), port)
        SelfInfo.regularSocket.send(packet)
    }
}
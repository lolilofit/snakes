package presentation.notmaster.MessagesReaction

import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGames
import presentation.notmaster.MessagesType
import presentation.notmaster.Resender
import view.View
import java.net.DatagramPacket
import java.util.concurrent.atomic.AtomicLong

object AckReaction : MessagesType {
    override fun execute(protoElement: SnakesProto.GameMessage, packet: DatagramPacket, currentGames: CurrentGames, view: View, acked: MutableMap<Pair<String, Int>, AtomicLong>) {
        Resender.addAcked(protoElement.msgSeq)

    }
}
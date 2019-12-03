package presentation.notmaster

import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGames
import view.View
import java.net.DatagramPacket
import java.util.concurrent.atomic.AtomicLong

interface MessagesType {
    fun execute(protoElement : SnakesProto.GameMessage, packet: DatagramPacket, currentGames : CurrentGames, view: View, acked : MutableMap<Pair<String, Int>, AtomicLong>)
}
package presentation.notmaster.MessagesReaction

import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGame
import model.CurrentGames
import model.GameConfig
import presentation.ProtoAdapter
import presentation.notmaster.MessagesType
import view.View
import java.net.DatagramPacket
import java.util.concurrent.atomic.AtomicLong

object AnnouncementReaction : MessagesType {
    override fun execute(protoElement: SnakesProto.GameMessage, packet: DatagramPacket, currentGames: CurrentGames, view: View, acked: MutableMap<Pair<String, Int>, AtomicLong>) {
        val newConfig: GameConfig = ProtoAdapter.getConfig(protoElement.announcement.config)
        val players = ProtoAdapter.getPlayers(protoElement.announcement.players)
        currentGames.currentGames[Pair(packet.address.toString().replace("/", ""), packet.port)] = CurrentGame(newConfig, System.currentTimeMillis(), players)
    }
}
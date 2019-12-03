package presentation.notmaster

import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGame
import model.CurrentGames
import presentation.SelfInfo
import presentation.master.Task
import presentation.move.moveimpl.JoinSender
import presentation.notmaster.MessagesReaction.AnnouncementReaction
import presentation.notmaster.MessagesReaction.JoinReaction
import presentation.notmaster.MessagesReaction.StateMsgReaction
import presentation.notmaster.MessagesReaction.SteerMessageReaction
import view.View
import java.net.DatagramPacket
import java.util.concurrent.atomic.AtomicLong

object GetMulticastMessage : Task {
    private lateinit var view : View
    private val currentGames : CurrentGames = CurrentGames(HashMap())
    fun getCurrentGames() = currentGames

    private val acked : MutableMap<Pair<String, Int>, AtomicLong> = HashMap()
    override fun cleanup() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun run() {
        val buf = ByteArray(2048)

        while(true) {
            val packet = DatagramPacket(buf, buf.size)
            SelfInfo.multicastSocket.receive(packet)

            val resized = ByteArray(packet.length)
            packet.data.copyInto(resized, 0 ,0 , packet.length)
            val protoElement = SnakesProto.GameMessage.parseFrom(resized)

            if(protoElement.hasAnnouncement()) {
                AnnouncementReaction.execute(protoElement, packet, currentGames, view, acked)
                if(globalState.game_players.players.size == 0) {
                    val p : CurrentGame? = currentGames.currentGames[Pair(packet.address.toString().replace("/", ""), packet.port)]
                    if(p != null) {
                        JoinSender.execute(listOf(packet.address.toString().replace("/", ""),
                                packet.port,
                                p as CurrentGame,
                                "play",
                                0.toLong()
                        ))
                    }
                }
            }
            if(protoElement.hasJoin())
                JoinReaction.execute(protoElement, packet, currentGames, view, acked)
            //if not master
            if(protoElement.hasState()) {
                StateMsgReaction.execute(protoElement, packet, currentGames, view, acked)
            }
            if(protoElement.hasSteer()) {
                SteerMessageReaction.execute(protoElement, packet, currentGames, view, acked)
            }
         //   System.out.println("got message from other player")
        }
    }

    override fun setView(view: View) {
        this.view = view
    }
}
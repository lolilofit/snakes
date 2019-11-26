package presentation.notmaster

import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGame
import model.CurrentGames
import model.GameConfig
import presentation.ProtoAdapter
import presentation.SelfInfo
import presentation.master.Task
import view.View
import java.net.DatagramPacket

object GetRegularPayerMessage : Task {
    private lateinit var view : View
    private val currentGames : CurrentGames = CurrentGames(HashMap())

    override fun cleanup() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun run() {
        val buf = ByteArray(2048)

        while(true) {
            val packet = DatagramPacket(buf, buf.size)
            SelfInfo.socket.receive(packet)
            val resized = ByteArray(packet.length)
            packet.data.copyInto(resized, 0 ,0 , packet.length)
            val protoElement = SnakesProto.GameMessage.parseFrom(resized)

            if(protoElement.hasAnnouncement()) {
                val newConfig: GameConfig = ProtoAdapter.getConfig(protoElement.announcement.config)
                val players = ProtoAdapter.getPlayers(protoElement.announcement.players)
                currentGames.currentGames.put(Pair(packet.address.toString().replace("/", ""), packet.port),
                        CurrentGame(newConfig, System.currentTimeMillis(), players))
            }
            System.out.println("got message from other player")
        }
    }

    override fun setView(view: View) {
        this.view = view
    }
}
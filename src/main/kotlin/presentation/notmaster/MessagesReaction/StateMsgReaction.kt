package presentation.notmaster.MessagesReaction

import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGames
import model.GamePlayer
import presentation.ImmediateQueue
import presentation.ProtoAdapter
import presentation.SelfInfo
import presentation.move.moveimpl.SendAck
import presentation.notmaster.MessagesType
import view.View
import java.net.DatagramPacket
import java.util.*
import java.util.concurrent.atomic.AtomicLong

object StateMsgReaction : MessagesType {
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
            val newState = ProtoAdapter.getGameState(protoElement.state.state)
            synchronized(ImmediateQueue::class) {
                globalState = newState
            }
            val masterInfo: Optional<GamePlayer>
            synchronized(ImmediateQueue::class) {
                masterInfo = globalState.game_players.players.stream().filter { it.role == SnakesProto.NodeRole.MASTER }.findFirst()
            }
            if (!masterInfo.isPresent) {
                System.out.println("NO MASTER")
                return
            }
            masterInfo.get().ip_address = packet.address.toString().replace("/", "")
            masterInfo.get().port = packet.port
            SelfInfo.masterIp = packet.address.toString().replace("/", "")
            SelfInfo.masterInfo = masterInfo.get().id
            SelfInfo.masterPort = packet.port
            SelfInfo.selfId = protoElement.receiverId

            el?.set(protoElement.msgSeq)
            SendAck.execute(listOf(packet.address.toString().replace("/", ""), packet.port, protoElement.msgSeq))
            view.update()
        }
    }
}
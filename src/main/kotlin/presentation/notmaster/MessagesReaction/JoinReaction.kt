package presentation.notmaster.MessagesReaction

import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGames
import model.GamePlayer
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.TimeoutQueue
import presentation.move.moveimpl.AddNewSnake
import presentation.move.moveimpl.SendAck
import presentation.move.moveimpl.SendChangeRole
import presentation.notmaster.MessagesType
import presentation.notmaster.Resender
import view.View
import java.net.DatagramPacket
import java.util.concurrent.atomic.AtomicLong

object JoinReaction : MessagesType {
    private var playerId = 1

    private fun newDeputy() {
        val newDeputy = globalState.game_players.players.find{it.role != SnakesProto.NodeRole.MASTER && it.role != SnakesProto.NodeRole.VIEWER}
        newDeputy?.role = SnakesProto.NodeRole.DEPUTY
        SendChangeRole.execute(listOf(newDeputy?.ip_address, newDeputy?.port, SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.DEPUTY, SelfInfo.selfId, newDeputy?.id))
    }

    override fun execute(protoElement: SnakesProto.GameMessage, packet: DatagramPacket, currentGames: CurrentGames, view: View, acked: MutableMap<Pair<String, Int>, AtomicLong>) {
        synchronized(ImmediateQueue::class) {
            val deputy  = globalState.game_players.players.find{it.role == SnakesProto.NodeRole.DEPUTY}

            val found = globalState.game_players.players.find { it.ip_address == packet.address.toString().replace("/", "") && it.port == packet.port }
            if (found == null) {
                globalState.game_players.players.add(
                        GamePlayer(
                                protoElement.join.name,
                                playerId,
                                packet.address.toString().replace("/", ""),
                                packet.port,
                                //if (globalState.game_players.players.size == 1) SnakesProto.NodeRole.DEPUTY else SnakesProto.NodeRole.NORMAL,
                                if(deputy == null) {
                                    newDeputy()
                                    SnakesProto.NodeRole.DEPUTY
                                }
                                else SnakesProto.NodeRole.NORMAL,
                                0))

                SelfInfo.masterPort = packet.port
                SelfInfo.masterIp = packet.address.toString().replace("/", "")
                TimeoutQueue.addToQueue(AddNewSnake, listOf(playerId))
                playerId++
            }
        }
        SendAck.execute(listOf(packet.address.toString().replace("/", ""), packet.port, protoElement.msgSeq))
    }
}
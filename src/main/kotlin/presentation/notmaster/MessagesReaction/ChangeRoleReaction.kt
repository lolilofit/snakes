package presentation.notmaster.MessagesReaction

import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGames
import presentation.ImmediateQueue
import presentation.move.moveimpl.SendChangeRole
import presentation.notmaster.MessagesType
import view.View
import java.net.DatagramPacket
import java.util.concurrent.atomic.AtomicLong

object ChangeRoleReaction : MessagesType {
    private fun sayImMaster(protoElement: SnakesProto.GameMessage) {
        synchronized(ImmediateQueue::class) {
            globalState.game_players.players.find {it.role == SnakesProto.NodeRole.MASTER}?.role = SnakesProto.NodeRole.VIEWER
            globalState.game_players.players.find { it.id == protoElement.senderId }?.role = SnakesProto.NodeRole.MASTER
        }
    }

    private fun claimDead(protoElement: SnakesProto.GameMessage) {
        synchronized(ImmediateQueue::class) {
            globalState.snakes.remove(globalState.snakes.find { it.player_id == protoElement.receiverId })
            globalState.game_players.players.find { it.id == protoElement.receiverId}?.role = SnakesProto.NodeRole.VIEWER
        }
    }

    private fun playerExited(protoElement: SnakesProto.GameMessage) {
        synchronized(ImmediateQueue::class) {
            val snake = globalState.snakes.find{it.player_id == protoElement.senderId}
            globalState.snakes.remove(snake)
            globalState.game_players.players.find { it.id == protoElement.senderId }?.role = SnakesProto.NodeRole.VIEWER
            val deputy = globalState.game_players.players.find{it.role == SnakesProto.NodeRole.DEPUTY}

            if(protoElement.roleChange.receiverRole == SnakesProto.NodeRole.MASTER) {
                if(deputy?.id == protoElement.senderId) {
                    globalState.game_players.players.find{it.role != SnakesProto.NodeRole.MASTER && it.role != SnakesProto.NodeRole.VIEWER}?.role = SnakesProto.NodeRole.DEPUTY
                }
            }
            if(protoElement.roleChange.receiverRole == SnakesProto.NodeRole.DEPUTY) {
                deputy?.role = SnakesProto.NodeRole.MASTER
                val newDeputy = globalState.game_players.players.find{it.role != SnakesProto.NodeRole.MASTER && it.role != SnakesProto.NodeRole.VIEWER}
                newDeputy?.role = SnakesProto.NodeRole.DEPUTY
                globalState.game_players.players.forEach{player ->
                    if(player.role != SnakesProto.NodeRole.MASTER)
                        SendChangeRole.execute(listOf(player.ip_address, player.port, SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.NORMAL, deputy?.id, player.id))
                }
            }

        }
    }
    override fun execute(protoElement: SnakesProto.GameMessage, packet: DatagramPacket, currentGames: CurrentGames, view: View, acked: MutableMap<Pair<String, Int>, AtomicLong>) {

            if(protoElement.roleChange.senderRole == SnakesProto.NodeRole.VIEWER)
                playerExited(protoElement)
            if(protoElement.roleChange.receiverRole == SnakesProto.NodeRole.MASTER && protoElement.roleChange.senderRole != SnakesProto.NodeRole.VIEWER)
                sayImMaster(protoElement)
            if(protoElement.roleChange.receiverRole == SnakesProto.NodeRole.VIEWER)
                claimDead(protoElement);
    }
}
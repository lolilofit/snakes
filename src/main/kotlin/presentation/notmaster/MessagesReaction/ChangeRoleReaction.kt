package presentation.notmaster.MessagesReaction

import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGames
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.MasterTools
import presentation.move.moveimpl.SendChangeRole
import presentation.notmaster.MessagesType
import view.View
import java.net.DatagramPacket
import java.util.concurrent.atomic.AtomicLong

object ChangeRoleReaction : MessagesType {
    private fun sayImMaster(protoElement: SnakesProto.GameMessage,  packet: DatagramPacket) {
        synchronized(ImmediateQueue::class) {
            val currentMaster = globalState.game_players.players.find{it.role == SnakesProto.NodeRole.MASTER}
            currentMaster?.role = SnakesProto.NodeRole.VIEWER
            globalState.game_players.players.find { it.id == protoElement.senderId }?.role = SnakesProto.NodeRole.MASTER
            SelfInfo.masterInfo = protoElement.senderId
            SelfInfo.masterIp = packet.address.toString().replace("/", "")
            SelfInfo.masterPort = packet.port

            currentMaster?.ip_address = packet.address.toString().replace("/", "")
            currentMaster?.port = packet.port
        }
    }

    private fun claimDead(protoElement: SnakesProto.GameMessage) {
        synchronized(ImmediateQueue::class) {
            globalState.snakes.remove(globalState.snakes.find { it.player_id == protoElement.receiverId })
            globalState.game_players.players.find { it.id == protoElement.receiverId}?.role = SnakesProto.NodeRole.VIEWER
        }
    }

    private fun deputyIsMaster(protoElement: SnakesProto.GameMessage) {
        synchronized(ImmediateQueue::class) {
            globalState.game_players.players.find{it.role == SnakesProto.NodeRole.MASTER}?.role = SnakesProto.NodeRole.VIEWER
            globalState.game_players.players.find{it.id == SelfInfo.selfId}?.role = SnakesProto.NodeRole.MASTER
        }
        MasterTools.startMasterTasks()
    }

    private fun normalIsDeputy(protoElement: SnakesProto.GameMessage) {
        synchronized(ImmediateQueue::class) {
            globalState.game_players.players.find{it.id == SelfInfo.selfId}?.role = SnakesProto.NodeRole.DEPUTY
        }
    }

    private fun playerExited(protoElement: SnakesProto.GameMessage,  packet: DatagramPacket) {
        synchronized(ImmediateQueue::class) {
            val currentMaster = globalState.game_players.players.find{it.role == SnakesProto.NodeRole.MASTER}
            val deputy = globalState.game_players.players.find{it.role == SnakesProto.NodeRole.DEPUTY}

            val snake = globalState.snakes.find{it.player_id == protoElement.senderId}
            globalState.snakes.remove(snake)
            globalState.game_players.players.find { it.id == protoElement.senderId }?.role = SnakesProto.NodeRole.VIEWER


            if(deputy?.id == protoElement.senderId) {
                val newDeputy = globalState.game_players.players.find{it.role != SnakesProto.NodeRole.MASTER && it.role != SnakesProto.NodeRole.VIEWER}
                newDeputy?.role = SnakesProto.NodeRole.DEPUTY
                SendChangeRole.execute(listOf(newDeputy?.ip_address, newDeputy?.port, SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.DEPUTY, currentMaster?.id, newDeputy?.id))
            }

            if(protoElement.senderId == currentMaster?.id) {
                deputy?.role = SnakesProto.NodeRole.MASTER
                val newDeputy = globalState.game_players.players.find{it.role != SnakesProto.NodeRole.MASTER && it.role != SnakesProto.NodeRole.VIEWER}
                newDeputy?.role = SnakesProto.NodeRole.DEPUTY
                globalState.game_players.players.forEach{player ->
                    if(player.role != SnakesProto.NodeRole.MASTER)
                        SendChangeRole.execute(listOf(player.ip_address, player.port, SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.NORMAL, deputy?.id, player.id))
                }
                SendChangeRole.execute(listOf(newDeputy?.ip_address, newDeputy?.port, SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.DEPUTY, currentMaster.id, newDeputy?.id))
                //self info
                currentMaster.ip_address = packet.address.toString().replace("/", "")
                currentMaster.port = packet.port
                SelfInfo.masterInfo = deputy?.id!!
                SelfInfo.masterIp = packet.address.toString().replace("/", "")
                SelfInfo.masterPort = packet.port

                MasterTools.startMasterTasks()
            }

        }
    }
    override fun execute(protoElement: SnakesProto.GameMessage, packet: DatagramPacket, currentGames: CurrentGames, view: View, acked: MutableMap<Pair<String, Int>, AtomicLong>) {

            if(protoElement.roleChange.senderRole == SnakesProto.NodeRole.VIEWER)
                playerExited(protoElement, packet)
            if(protoElement.roleChange.senderRole == SnakesProto.NodeRole.MASTER && protoElement.roleChange.receiverRole != SnakesProto.NodeRole.VIEWER)
                sayImMaster(protoElement, packet)
            if(protoElement.roleChange.receiverRole == SnakesProto.NodeRole.VIEWER)
                claimDead(protoElement)
            if(protoElement.roleChange.receiverRole == SnakesProto.NodeRole.DEPUTY)
                normalIsDeputy(protoElement)
            if(protoElement.roleChange.receiverRole == SnakesProto.NodeRole.MASTER && protoElement.roleChange.senderRole != SnakesProto.NodeRole.VIEWER)
                deputyIsMaster(protoElement)
    }
}
package presentation.notmaster

import creator.config
import creator.globalState
import kotlinx.coroutines.delay
import me.ippolitov.fit.snakes.SnakesProto
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.move.moveimpl.SendChangeRole
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ArrayBlockingQueue
import kotlin.streams.toList

object Resender {
    private data class ResendObj(val ip : String, val port : Int, val message : SnakesProto.GameMessage, val timeSended : Long, val recverId : Int)
    private val acked : MutableList<Long> = ArrayList()
    private val queue: ArrayBlockingQueue<ResendObj> = ArrayBlockingQueue(100, true)
    private val backToResend : MutableList<ResendObj> = ArrayList()

    fun addToResendQueue(ip : String, port : Int, message: SnakesProto.GameMessage, time : Long, recverId: Int) {
        synchronized(ImmediateQueue::class) {
            val player = globalState.game_players.players.find { it.id == recverId }
            if(player?.role != SnakesProto.NodeRole.VIEWER)
                queue.add(ResendObj(ip, port, message, time, recverId))
        }
    }
    fun addAcked(mesId : Long) {
        acked.add(mesId)
    }
    fun clearResending() {
        acked.clear()
        queue.clear()
        backToResend.clear()
    }

    private fun gamerDied(resendThis : ResendObj) {
        synchronized(ImmediateQueue::class) {
            val masteAndDeputy = globalState.game_players.players.stream()
                    .filter{it.role == SnakesProto.NodeRole.MASTER || it.role == SnakesProto.NodeRole.DEPUTY}
                    .toList()
            val master = masteAndDeputy.find { it.role ==  SnakesProto.NodeRole.MASTER}
            val deputy = masteAndDeputy.find { it.role == SnakesProto.NodeRole.DEPUTY }

            //???
            if(resendThis.recverId == master?.id) {
                deputy?.role = SnakesProto.NodeRole.MASTER
                SendChangeRole.execute(listOf(deputy?.ip_address, deputy?.port, SnakesProto.NodeRole.NORMAL, SnakesProto.NodeRole.MASTER, SelfInfo.selfId, deputy?.id))

                globalState.game_players.players.forEach{player ->
                    if(player.role != SnakesProto.NodeRole.MASTER)
                        SendChangeRole.execute(listOf(player.ip_address, player.port, SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.NORMAL, deputy?.id, player.id))
                }
            }
            if(SelfInfo.selfId == master?.id && resendThis.recverId == deputy?.id) {
                val newDeputy = globalState.game_players.players.find{it.role != SnakesProto.NodeRole.MASTER && it.role != SnakesProto.NodeRole.VIEWER}
                newDeputy?.role = SnakesProto.NodeRole.DEPUTY
                SendChangeRole.execute(listOf(newDeputy?.ip_address, newDeputy?.port, SnakesProto.NodeRole.NORMAL, SnakesProto.NodeRole.DEPUTY, SelfInfo.selfId, newDeputy?.id))
                ///
            }
            globalState.snakes.remove(globalState.snakes.find { it.player_id == resendThis.recverId })
            globalState.game_players.players.find { it.id == resendThis.recverId }?.role = SnakesProto.NodeRole.VIEWER
        }
    }

    private fun resendMessage(resendThis : ResendObj, nodeTimeoutMs : Int, socket: DatagramSocket) {
        if(acked.contains(resendThis.message.msgSeq)) {
            //acked.remove(resendThis.message.msgSeq)
            return
        }

        if(System.currentTimeMillis() - resendThis.timeSended >= nodeTimeoutMs) {
            print("gamer died")
            gamerDied(resendThis)
        }
        else {
            val binaryMessage  = resendThis.message.toByteArray()
            socket.send(DatagramPacket(binaryMessage, binaryMessage.size, InetAddress.getByName(resendThis.ip), resendThis.port))
            backToResend.add(resendThis)
        }
    }

    suspend fun runResend() {

        var pingDelayMs = 0
        var nodeTimeoutMs = 0
        lateinit var socket : DatagramSocket
        synchronized(ImmediateQueue::class) {
            socket = SelfInfo.regularSocket
            pingDelayMs = config.pingDelayMs
            nodeTimeoutMs = config.nodeTimeoutMs
        }

        while (true) {
            while(!queue.isEmpty()) {
                val resendThis = queue.poll()
                resendMessage(resendThis, nodeTimeoutMs, socket)
            }
            delay(pingDelayMs.toLong())
            queue.addAll(backToResend)
            backToResend.clear()
            synchronized(ImmediateQueue::class) {
                pingDelayMs = config.pingDelayMs
                nodeTimeoutMs = config.nodeTimeoutMs
            }

        }
    }
}
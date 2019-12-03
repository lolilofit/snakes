package presentation.notmaster

import creator.config
import creator.globalState
import kotlinx.coroutines.delay
import me.ippolitov.fit.snakes.SnakesProto
import presentation.ImmediateQueue
import presentation.SelfInfo
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ArrayBlockingQueue
import kotlin.random.Random
import kotlin.streams.toList

object Resender {
    private data class ResendObj(val ip : String, val port : Int, val message : SnakesProto.GameMessage, val timeSended : Long)
    private val acked : MutableList<Long> = ArrayList()
    private val queue: ArrayBlockingQueue<ResendObj> = ArrayBlockingQueue(100, true)
    private val backToResend : MutableList<ResendObj> = ArrayList()

    fun addToResendQueue(ip : String, port : Int, message: SnakesProto.GameMessage, time : Long) {
        queue.add(ResendObj(ip, port, message, time))
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

            if(resendThis.message.msgSeq == master?.id?.toLong()) {
                globalState.snakes.remove(globalState.snakes.find { it.player_id.toLong() == resendThis.message.msgSeq })
                globalState.game_players.players.remove(master)
                deputy?.role = SnakesProto.NodeRole.MASTER
            }
            if(SelfInfo.selfId == master?.id && resendThis.message.msgSeq == deputy?.id?.toLong()) {
                globalState.snakes.remove(globalState.snakes.find { it.player_id.toLong() == resendThis.message.msgSeq })
                globalState.game_players.players.remove(deputy)
                globalState.game_players.players[Random.nextInt(0, globalState.game_players.players.size)].role = SnakesProto.NodeRole.DEPUTY
            }
        }
    }

    private fun resendMessage(resendThis : ResendObj, nodeTimeoutMs : Int, socket: DatagramSocket) {
        if(acked.contains(resendThis.message.msgSeq)) {
            //acked.remove(resendThis.message.msgSeq)
            print("got ack")
            return
        }

        if(System.currentTimeMillis() - resendThis.timeSended >= nodeTimeoutMs) {
            print("gamer died")
            gamerDied(resendThis)
        }
        else {
            print("resend")
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
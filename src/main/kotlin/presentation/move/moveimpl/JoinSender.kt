package presentation.move.moveimpl

import creator.config
import creator.globalState
import creator.mesSeq
import me.ippolitov.fit.snakes.SnakesProto
import model.CurrentGame
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.MasterTools
import presentation.move.Move
import presentation.notmaster.Resender
import java.net.DatagramPacket
import java.net.InetAddress


//убрать последний аргумент
object JoinSender : Move {
    override fun execute(param: List<Any?>) {
        if(param.size != 5) return
        val ipStr = if(param[0] is String) (param[0] as String) else return
        val port = if(param[1] is Int) (param[1] as Int) else return
        val currentGame = if(param[2] is CurrentGame) (param[2] as CurrentGame) else return
        val name = if(param[3] is String) (param[3] as String) else return

        synchronized(ImmediateQueue::class) {
            globalState.foods.clear()
            config = currentGame.config
            globalState.snakes.clear()
            globalState.game_players.players.clear()
            globalState.game_players.players = currentGame.players.players
            SelfInfo.masterIp = ipStr
            SelfInfo.masterPort = port
            SelfInfo.masterInfo = -1
            SelfInfo.selfId = -1
        }
        MasterTools.endMasterTasks()

        val protoMessage = SnakesProto.GameMessage.newBuilder()
                .setJoin(SnakesProto.GameMessage.JoinMsg.newBuilder().setName(name))
                .setMsgSeq(mesSeq.get())
                .build()
        val binaryMessage = protoMessage.toByteArray()
        val packet = DatagramPacket(binaryMessage, binaryMessage.size, InetAddress.getByName(ipStr), port)
        SelfInfo.regularSocket.send(packet)

        Resender.addToResendQueue(ipStr, port, protoMessage, System.currentTimeMillis(), -1)
        mesSeq.incrementAndGet()
    }
}
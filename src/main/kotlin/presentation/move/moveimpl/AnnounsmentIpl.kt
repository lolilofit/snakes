package presentation.move.moveimpl

import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.GameConfig
import model.GamePlayer
import presentation.ImmediateQueue
import presentation.ProtoAdapter
import presentation.SelfInfo
import presentation.move.Move
import java.net.DatagramPacket
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

object AnnounsmentIpl : Move {
    override fun execute(param: List<Any?>) {
        if (param.size != 1) return
        val message_num = param[0] as Long
        var players : SnakesProto.GamePlayers.Builder
        var config : SnakesProto.GameConfig.Builder

        synchronized(ImmediateQueue::class) {
            players = ProtoAdapter.getProtoGamePlayers(globalState.game_players)
            config = ProtoAdapter.getProtoConfig(globalState.config)
        }
        val message = SnakesProto.GameMessage.newBuilder().setSenderId(SelfInfo.selfId).setMsgSeq(message_num)
                .setAnnouncement(SnakesProto.GameMessage.AnnouncementMsg.newBuilder().setCanJoin(true).setConfig(config).setPlayers(players))
                .build()
        val protobufMes = message.toByteArray()
        val packet = DatagramPacket(protobufMes, protobufMes.size, SelfInfo.group, SelfInfo.port)
        SelfInfo.socket.send(packet)
    }
}
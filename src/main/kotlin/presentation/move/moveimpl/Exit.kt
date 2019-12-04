package presentation.move.moveimpl

import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.MasterTools
import presentation.move.Move

object Exit : Move {
    override fun execute(param: List<Any?>) {
        synchronized(ImmediateQueue::class) {
            //change role, send to master or deputy
            val master = globalState.game_players.players.find { it.role == SnakesProto.NodeRole.MASTER }
            val deputy = globalState.game_players.players.find{it.role == SnakesProto.NodeRole.DEPUTY}

            if(master?.id == SelfInfo.selfId) {
                SendChangeRole.execute(listOf(deputy?.ip_address, deputy?.port, SnakesProto.NodeRole.VIEWER, SnakesProto.NodeRole.MASTER, master.id,  deputy?.id))
            }
            else {
                SendChangeRole.execute(listOf(master?.ip_address, master?.port, SnakesProto.NodeRole.VIEWER, SnakesProto.NodeRole.MASTER, SelfInfo.selfId, master?.id))
            }
            val me = globalState.game_players.players.find{ it.id == SelfInfo.selfId}
            globalState.game_players.players.remove(me)
            val mySnake = globalState.snakes.find { it.player_id == SelfInfo.selfId }
            globalState.snakes.remove(mySnake)
            MasterTools.endMasterTasks()
        }
    }
}
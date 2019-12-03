package presentation.move.moveimpl

import creator.config
import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.*
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.MasterTools
import presentation.move.Move
import view.View


object StartNewGame : Move {
    private fun cleanState() {
        globalState.foods.clear()
        globalState.snakes.clear()
        globalState.game_players.players.clear()
    }

    override fun execute(param: List<Any?>) {
        if (param.size != 1) return
        val view: View = param[0] as View
        val masterSnake = ArrayList<Coord>()

        synchronized(ImmediateQueue::class) {
            cleanState()
            globalState.game_players.players.add(GamePlayer("first player", 0, "", 9192, SnakesProto.NodeRole.MASTER, 0))
            globalState.snakes.add(Snake(0, masterSnake, SnakesProto.GameState.Snake.SnakeState.ALIVE, SnakesProto.Direction.DOWN))
        }
        masterSnake.add(Coord(1, 1))
        masterSnake.add(Coord(1, 0))

        MasterTools.startMasterTasks()
        SelfInfo.selfId = 0
        SelfInfo.masterInfo = 0
        view.update()
        return
    }
}
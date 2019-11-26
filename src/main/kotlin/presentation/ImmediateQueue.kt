package presentation

import creator.config
import creator.globalState
import model.Coord
import model.GamePlayer
import model.NodeRole
import model.Snake
import presentation.master.MasterTools
import presentation.move.Move
import view.View
import java.util.concurrent.ArrayBlockingQueue


class ImmediateQueue(view : View, queue: ArrayBlockingQueue<Pair<Move, List<Any?>>>) {

    private var view = view
    private val queue = queue

    //init as master for test
    fun run() {
        //add snake

        //------

        while(true) {
            val move = queue.take()
            move.first.execute(move.second)
        }
    }
}
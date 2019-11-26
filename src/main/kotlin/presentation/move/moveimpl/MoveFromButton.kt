package presentation.move.moveimpl

import creator.globalState
import model.Direction
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.TimeoutQueue
import presentation.move.Move
import presentation.move.moveimpl.message.SteerMsgImpl


object MoveFromButton : Move {
    override fun execute(param: List<Any?>) {
            if(SelfInfo.masterinfo!= -1 && SelfInfo.masterinfo == SelfInfo.selfId) {
                synchronized(ImmediateQueue::class) {
                    //find my snake
                    val mySnakeOptional = globalState.snakes.stream().filter{it.player_id == SelfInfo.selfId}.findFirst()
                    if(mySnakeOptional.isPresent) {
                        val mySnake = mySnakeOptional.get()
                        if (param.size == 1) {
                            val direction = param[0] as Direction
                            TimeoutQueue.addToQueue(SteerMsgImpl, listOf(direction, mySnake))
                        }
                    }
                }
            }
            return
            //send to master
    }
}
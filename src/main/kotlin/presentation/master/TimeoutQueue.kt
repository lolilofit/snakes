package presentation.master

import creator.globalState
import kotlinx.coroutines.delay
import model.Field
import presentation.ImmediateQueue
import presentation.move.Move
import presentation.move.moveimpl.SendAnnounsment
import presentation.move.moveimpl.StateMsgImpl
import presentation.move.moveimpl.ThrowFood
import presentation.move.moveimpl.SteerMsgImpl
import view.View
import java.util.concurrent.ArrayBlockingQueue
import kotlin.collections.ArrayList

object TimeoutQueue : Task {
    private val queue: ArrayBlockingQueue<Pair<Move, List<Any?>>> = ArrayBlockingQueue(50, true)
    private lateinit var view : View
    private val field : Field = Field()

    override fun cleanup() {
        queue.clear()
    }
    override fun setView(view: View) {this.view = view}

    fun addToQueue(move : Move, params : List<Any?>) {
        synchronized(TimeoutQueue::class) {
            queue.put(Pair(move, params))
        }
    }

    private fun pushForwardUnsended() {
        val list : MutableList<List<Any>> = ArrayList()
        synchronized(ImmediateQueue::class) {
                globalState.snakes.forEach{snake ->
                            val direction = SteerMsgImpl.calkForwardDirection(snake)
                            if(direction != null)
                                list.add(listOf(direction, snake, field, true))
                        }
            }
        list.forEach{param ->
            SteerMsgImpl.execute(param)
            field.createField()
        }
    }

    override suspend fun run() {
        var countMes : Long = 0
        var lastPushForward = System.currentTimeMillis()
        var lastAnnouncementMessage : Long = 0
        var lastMessageSended : Long = System.currentTimeMillis()

        while(true) {

            field.createField()
            if(System.currentTimeMillis() - lastAnnouncementMessage >= 1000) {
                lastAnnouncementMessage = System.currentTimeMillis()
                lastMessageSended = System.currentTimeMillis()
                SendAnnounsment.execute(listOf(countMes))
            }
            countMes++

            if(!queue.isEmpty()) {
                val move = queue.poll()
                val params = ArrayList(move.second)
                params.add(field)
                if(move.first is SteerMsgImpl)
                    params.add(false)
                move.first.execute(params)
                ThrowFood.execute(listOf(field))
            }

            if(System.currentTimeMillis() - lastPushForward > 500) {
                lastPushForward = System.currentTimeMillis()
                pushForwardUnsended()
                StateMsgImpl.execute(emptyList())
            }
            view.update()
            delay(10)
        }
    }
}
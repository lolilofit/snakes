package presentation.move.moveimpl

import creator.config
import creator.globalState
import model.Coord
import model.Field
import model.NodeRole
import presentation.ImmediateQueue
import presentation.move.Move
import kotlin.random.Random

object ThrowFood : Move {
    private fun aliveCount(): Int {
        var count = 0
        globalState.game_players.players.forEach { player ->
            if (player.role != NodeRole.VIEWER)
                count++
        }
        return count
    }

    override fun execute(param: List<Any?>) {
        if (param.size != 1) return
        if (param[0] !is Field) return
        val free = param[0] as Field

        synchronized(ImmediateQueue::class) {
            val aliveCount = aliveCount()
            val foodToPlace : Int = ((aliveCount * config.foodPerPlayer + config.foodStatic).toInt() - globalState.foods.size)
            for (i in 0 until (foodToPlace)) {
                if (free.free.size > 0) {
                    val newFood = Random.nextInt(0, free.free.size)
                    globalState.foods.add(free.free[newFood])
                    free.free.removeAt(newFood)
                }
            }
        }
        return
    }
}
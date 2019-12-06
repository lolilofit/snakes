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
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType


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

        val properties = Properties()
        val file = FileInputStream("src/main/resources/settings.property")
        properties.load(file)

        synchronized(ImmediateQueue::class) {

            properties.keys().toList().forEach{prop ->
                val classConfig = config::class
                classConfig.declaredMemberProperties
                        .filter{it.name == prop.toString()}
                        .filterIsInstance<KMutableProperty<*>>()
                        .forEach{
                            value ->

                                  value.setter.call(config,
                                        if(value.returnType == Float::class.createType())
                                            properties.getValue(prop).toString().toFloat()
                                        else properties.getValue(prop).toString().toInt())
                        }
                SelfInfo.myName = properties.getProperty("name")
            }

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
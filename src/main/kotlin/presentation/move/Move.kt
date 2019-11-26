package presentation.move

import model.GameState
import presentation.SelfInfo
import java.net.MulticastSocket

interface Move {
    fun execute(param : List<Any?>)
}
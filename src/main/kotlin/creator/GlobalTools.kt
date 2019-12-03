package creator

import model.GameConfig
import model.GamePlayers
import model.GameState
import java.util.concurrent.atomic.AtomicLong

var config : GameConfig = GameConfig(10, 10, 1, (1.0).toFloat(), 10, 100, 100.0.toFloat(), 5000)
var globalState : GameState = GameState(1, ArrayList(), ArrayList(), GamePlayers(ArrayList()), config)
val mesSeq = AtomicLong(0)

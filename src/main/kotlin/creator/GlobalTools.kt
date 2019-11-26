package creator

import model.GameConfig
import model.GamePlayers
import model.GameState
import presentation.SelfInfo

var config : GameConfig = GameConfig(10, 10, 1, (1.0).toFloat(), 10, 100, 100.0.toFloat(), 100)
var globalState : GameState = GameState(1, ArrayList(), ArrayList(), GamePlayers(ArrayList()), config)


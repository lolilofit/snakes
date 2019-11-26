package model

data class GameConfig (
        var width : Int,
        var height : Int,
        var foodStatic : Int,
        var foodPerPlayer : Float,
        var stateDelayMs :Int,
        var pingDelayMs  : Int,
        var deadFoodProb : Float,
        var nodeTimeoutMs : Int
)
package presentation.move.moveimpl

import creator.config
import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import model.Coord
import model.Field
import model.Snake
import presentation.ImmediateQueue
import presentation.move.Move


object SteerMsgImpl : Move {
    private var forwardMove : Boolean = false

    private fun checkForFood(snake : Snake, field: Field) : Boolean {
        globalState.foods.forEach { food ->
            if ((snake.points[0].x + config.width)% config.width == food.x && (snake.points[0].y + config.height)% config.height == food.y) {
                val player = globalState.game_players.players.stream().filter{it.id == snake.player_id}.findFirst()
                if(player.isPresent) player.get().score++

                field.field[food.y][food.y] = 0
                field.free.add(food)
                globalState.foods.remove(food)
                return true
            }
        }
         /*
        if(field.field[(snake.points[0].y + config.height)% config.height][(snake.points[0].x + config.width)% config.width] < 0) {
            globalState.foods.remove(snake.points[0])
            return true
        }
          */
        return false
    }
    fun calkForwardDirection(snake: Snake) : SnakesProto.Direction? {
        val second = snake.points[1]
        if (second.y < 0)
            return SnakesProto.Direction.DOWN
        if (second.y > 0)
               return SnakesProto.Direction.UP
        if (second.x > 0)
              return SnakesProto.Direction.LEFT
        if (second.x < 0)
                return SnakesProto.Direction.RIGHT
        return null
    }

     private fun checkValidDirection(snake : Snake, direction: SnakesProto.Direction, forwardAllow : Boolean) : Boolean {
            val second = snake.points[1]
            if (direction == SnakesProto.Direction.DOWN) {
                if (second.y > 0)
                    return false
                if (second.y < 0)
                    forwardMove = true
            }
            if (direction == SnakesProto.Direction.UP) {
                if (second.y < 0)
                    return false
                if (second.y > 0)
                    forwardMove = true
            }
            if (direction == SnakesProto.Direction.LEFT) {
                if (second.x < 0)
                    return false
                if (second.x > 0)
                    forwardMove = true
            }
            if (direction == SnakesProto.Direction.RIGHT) {
                if (second.x > 0)
                    return false
                if (second.x < 0)
                    forwardMove = true
            }
         if(!forwardAllow && forwardMove)
             return false
         return true
    }

    //if two heads to one cell
    private fun checkForSnakes(field: Field, selfSnake: Snake) : Boolean {
        val head = selfSnake.points[0]
         if(field.field[(head.y+ config.height)%config.height][(head.x+ config.width)% config.width] > 0)
             return false
        return true
    }

    private fun angle(snake: Snake) {
        if (snake.points[snake.points.size - 1].y > 0) {
            snake.points[snake.points.size - 1].y--
            if (snake.points[snake.points.size - 1].y == 0) {
                snake.points.removeAt(snake.points.size - 1)
            }
            return
        }
        if (snake.points[snake.points.size - 1].y < 0) {
            snake.points[snake.points.size - 1].y++
            if (snake.points[snake.points.size - 1].y == 0) {
                snake.points.removeAt(snake.points.size - 1)
            }
            return
        }
        if (snake.points[snake.points.size - 1].x > 0) {
                snake.points[snake.points.size - 1].x--
                if (snake.points[snake.points.size - 1].x == 0) {
                    snake.points.removeAt(snake.points.size - 1)
                }
            return
        }
        if (snake.points[snake.points.size - 1].x < 0) {
                snake.points[snake.points.size - 1].x++
                if (snake.points[snake.points.size - 1].x == 0) {
                    snake.points.removeAt(snake.points.size - 1)
                }
            return
        }
    }

    private fun addPoint(snake: Snake, point : Coord, x : Int, y: Int) {
        val copy = ArrayList<Coord>()
        copy.add(point)
        snake.points[0].x = x
        snake.points[0].y = y
        copy.addAll(snake.points)
        snake.points = copy
    }

    private fun makeMove(direction: SnakesProto.Direction, snake: Snake, field: Field)  : Boolean {
        field.removeSnake(snake)

        if(direction == SnakesProto.Direction.DOWN) {
            if(forwardMove) {
                if ((snake.points[0].y + 1) > (config.height - 1))
                    snake.points[0].y = 0
                else
                    snake.points[0].y++
                snake.points[1].y--
            }
            else {
                addPoint(snake, Coord(snake.points[0].x, (snake.points[0].y + 1) % (config.height)), 0, -1)
            }
        }
        if(direction == SnakesProto.Direction.UP) {
            if (forwardMove) {
                if ((snake.points[0].y - 1) < 0)
                    snake.points[0].y = config.height - 1
                else
                    snake.points[0].y--
                snake.points[1].y++
            } else {
                addPoint(snake, Coord(snake.points[0].x, (snake.points[0].y - 1) % (config.height)), 0, 1)
            }

        }
        if(direction == SnakesProto.Direction.LEFT) {
            if(forwardMove) {
                if((snake.points[0].x - 1) < 0)
                    snake.points[0].x = config.width - 1
                else
                    snake.points[0].x--
                snake.points[1].x++
            }
            else {
                addPoint(snake, Coord((snake.points[0].x - 1) % (config.width), snake.points[0].y), 1, 0)
            }
        }
        if(direction == SnakesProto.Direction.RIGHT) {
            if(forwardMove) {
                if((snake.points[0].x + 1) > (config.width - 1))
                    snake.points[0].x = 0
                else
                    snake.points[0].x++
                snake.points[1].x--
            }
            else {
                addPoint(snake, Coord((snake.points[0].x + 1) % (config.width), snake.points[0].y), -1, 0)
            }

        }
        if(!checkForFood(snake, field)) {
            angle(snake)
            field.putSnake(snake)
        }
        else {
            System.out.println("Eaten!")
            field.putSnake(snake)
            return true
        }
        return false
    }

    override fun execute(param: List<Any?>) {
        if(param.size != 4) return
        if(param[0] !is SnakesProto.Direction) return
        if(param[1] !is Snake)  return
        if(param[2] !is Field)  return
        if(param[3] !is Boolean) return
        val direction : SnakesProto.Direction = param[0] as SnakesProto.Direction
        val field = param[2] as Field
        val snake : Snake = param[1] as Snake
        val forwardAllow = param[3] as Boolean
        forwardMove = false

        synchronized(ImmediateQueue::class) {
            if(!checkValidDirection(snake, direction, forwardAllow)) return
            if(makeMove(direction, snake, field)) {
                return
            }

            if(!checkForSnakes(field, snake)) {
                System.out.println("REMOVE")
                val player = globalState.game_players.players.find { it.id == snake.player_id }
                player?.role = SnakesProto.NodeRole.VIEWER
                field.removeSnake(snake)
                globalState.snakes.remove(snake)
                return
            }
            field.putPoint(snake.points[0])
        }
        return
    }

}
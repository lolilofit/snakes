package model

import creator.config
import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import presentation.ImmediateQueue
import kotlin.random.Random

class Field {
    var field = List(5) { List(5) { MutableList(1) {0} }  }
    val free: ArrayList<Coord> = ArrayList()

    fun removeSnake(snake: Snake) {
        changeSnakePath(-1, snake, freeFlag = true, foodFlag = false)
    }

    fun putSnake(snake: Snake) {
        val points : ArrayList<Coord> = ArrayList()
        snake.points.forEach{point -> points.add(point.copy())}

        //change dir
        val copySnake = Snake(snake.player_id, points, SnakesProto.GameState.Snake.SnakeState.ALIVE, SnakesProto.Direction.DOWN)
        copySnake.points[1].x += copySnake.points[0].x
        copySnake.points[1].y += copySnake.points[0].y
        copySnake.points.removeAt(0)
        changeSnakePath(1, copySnake,  freeFlag = true, foodFlag = false)
        //add free
    }

    fun putPoint(point: Coord) {
        field[(point.y + config.height) % config.height][(point.x + config.width) % config.width][0]++
    }

    private fun freeSnakePoints(i : Int, j : Int, value: Int) {
        free.remove(Coord(i, j))
        if (value < 0) {
            free.remove(Coord(i, j))
            free.add(Coord(i, j))
        }
        else {free.remove(Coord(i, j))}
    }

    private fun linePart(foodFlag: Boolean, freeFlag: Boolean, x : Int, y : Int, value: Int, snake: Snake) {
        if(!foodFlag) {
            field[y][x][0] += value
            if (value == 1) field[y][x].add(snake.player_id)
            if (value == -1) field[y][x].subList(1, field[y][x].size).remove(snake.player_id)

            if (freeFlag) {
                freeSnakePoints(x, y, value)
            }
        }
        else {
            if(Random.nextInt(99) < config.deadFoodProb)
                globalState.foods.add(Coord(x, y))
        }
    }

    fun changeSnakePath(value: Int, snake: Snake, freeFlag : Boolean, foodFlag : Boolean) {
        if(config.width == 0  || config.height == 0) return

        var x = 0
        var y = 0
        var prevx = -1
        var prevy = -1
        var leftx = 0
        var rightx = 0
        var lefty = 0
        var righty = 0

        snake.points.forEach { point ->
            if (prevx != -1) {
                prevx = x
                prevy = y
                x = prevx + point.x
                y = prevy + point.y
                if(prevx == x) {
                    if(y < 0) {
                        for(i in 0 until (prevy))
                            linePart(foodFlag, freeFlag, x, i, value, snake)
                        y = (y + config.height)% config.height
                        for(i in y until (config.height))
                            linePart(foodFlag, freeFlag, x, i, value, snake)
                    }
                    else {
                        if(y >= config.height) {
                            for(i in prevy + 1 until (config.height))
                                linePart(foodFlag, freeFlag, x, i, value, snake)
                            y = (y + config.height)% config.height
                            for(i in 0 until (y+1))
                                linePart(foodFlag, freeFlag, x, i, value, snake)
                        }
                        else {
                            leftx = x
                            rightx = x
                            lefty = if (y < prevy) y else (prevy + 1)
                            righty = if (y < prevy) (prevy - 1) else y
                            for (i in leftx until (rightx + 1)) {
                                for (j in lefty until (righty + 1)) {
                                    linePart(foodFlag, freeFlag, i, j, value, snake)
                                }
                            }
                        }
                    }
                }
                if(prevy == y) {
                    if(x < 0) {
                        for(i in 0 until (prevx))
                            linePart(foodFlag, freeFlag, i, y, value, snake)
                        x = (x + config.width)% config.width
                        for(i in x until (config.width))
                            linePart(foodFlag, freeFlag, i, y, value, snake)
                    }
                    else {
                        if(x >= config.width) {
                            for(i in prevx+1 until (config.width))
                                linePart(foodFlag, freeFlag, i, y, value, snake)
                            x = (x + config.width)% config.width
                            for(i in 0 until (x+1))
                                linePart(foodFlag, freeFlag, i, y, value, snake)
                        }
                        else {
                            leftx = if (x < prevx) x else (prevx + 1)
                            rightx = if (x < prevx) (prevx - 1) else x
                            lefty = y
                            righty = y

                            for (i in leftx until (rightx + 1)) {
                                for (j in lefty until (righty + 1)) {
                                    linePart(foodFlag, freeFlag, i, j, value, snake)
                                }
                            }
                        }
                    }
                }

            } else {
                x += (point.x + config.width) % config.width
                y+= (point.y + config.height) % config.height
                prevx = x
                prevy = y

                field[y][x][0] += value
                if(value == 1) field[y][x].add(snake.player_id)
                if(value == -1) field[y][x].subList(1, field[y][x].size).remove(snake.player_id)

            }
        }
    }

    fun createField() {

        free.clear()
        synchronized(ImmediateQueue::class) {
            if(config.height > 0 && config.width > 0) {
                field = List(config.height) { List(config.width) { MutableList(1) { 0 } } }
                globalState.snakes.forEach { snake ->
                    changeSnakePath(1, snake, freeFlag = false, foodFlag = false)
                }

                globalState.foods.forEach { food ->
                    field[food.y][food.x][0] = -1
                }

                for (i in 0 until (field.size)) {
                    for (j in 0 until (field[i].size)) {
                        if (field[i][j][0] == 0)
                            free.add(Coord(j, i))
                    }
                }
            }
        }
    }
}
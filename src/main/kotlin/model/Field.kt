package model

import creator.config
import creator.globalState
import me.ippolitov.fit.snakes.SnakesProto
import presentation.ImmediateQueue
import presentation.move.moveimpl.message.SteerMsgImpl

class Field {
    val field = Array(config.height) { Array(config.width) { 0 } }
    val free: ArrayList<Coord> = ArrayList()

    fun removeSnake(snake: Snake) {
        changeSnakePath(-1, snake, true)
    }

    fun putSnake(snake: Snake) {
        val points : ArrayList<Coord> = ArrayList()
        snake.points.forEach{point -> points.add(point.copy())}

        //change dir
        val copySnake = Snake(snake.player_id, points, SnakesProto.GameState.Snake.SnakeState.ALIVE, SnakesProto.Direction.DOWN)
        copySnake.points[1].x += copySnake.points[0].x
        copySnake.points[1].y += copySnake.points[0].y
        copySnake.points.removeAt(0)
        changeSnakePath(1, copySnake, true)
        //add free
    }

    fun putPoint(point: Coord) {
        field[(point.y + config.height) % config.height][(point.x + config.width) % config.width]++
    }

    private fun freeSnakePoints(i : Int, j : Int, value: Int) {
        free.remove(Coord(i, j))
        if (value < 0) {
            free.remove(Coord(i, j))
            free.add(Coord(i, j))
        }
        else {free.remove(Coord(i, j))}
    }

    private fun changeSnakePath(value: Int, snake: Snake, freeFlag : Boolean) {
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
                x = (x + point.x + config.height) % config.height
                y = (y + point.y + config.width) % config.width

                if (prevx == x) {
                    leftx = x
                    rightx = x
                    lefty = if (y < prevy) y else (prevy + 1)
                    righty = if (y < prevy) (prevy - 1) else y
                }
                if (prevy == y) {
                    leftx = if (x < prevx) x else (prevx + 1)
                    rightx = if (x < prevx) (prevx - 1) else x
                    lefty = y
                    righty = y
                }

                for (i in leftx until (rightx + 1)) {
                    for (j in lefty until (righty + 1)) {
                        field[j][i] += value
                        if(freeFlag) {
                            freeSnakePoints(i, j, value)
                        }
                    }
                }
            } else {
                x = (x + point.x + config.height) % config.height
                y = (y + point.y + config.width) % config.width
                prevx = x
                field[y][x] += value
                if(freeFlag) {
                    freeSnakePoints(y, x, value)
                }
            }
        }
    }

    fun createField() {
        for (row in field)
            for (i in 0 until (row.size))
                row[i] = 0

        free.clear()
        synchronized(ImmediateQueue::class) {
            globalState.snakes.forEach { snake ->
                changeSnakePath(1, snake, false)
            }

            globalState.foods.forEach{ food ->
                field[food.y][food.x] = -1
            }

            for (i in 0 until (field.size)) {
                for (j in 0 until (field[i].size)) {
                    if (field[i][j] == 0)
                        free.add(Coord(j, i))
                }
            }
        }
    }
}
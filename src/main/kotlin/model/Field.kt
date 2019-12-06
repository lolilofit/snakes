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
                        for(i in 0 until (prevy)) {
                            if(!foodFlag) {
                                field[i][x][0] += value
                                if (value == 1) field[i][x].add(snake.player_id)
                                if (value == -1) field[i][x].remove(snake.player_id)

                                if (freeFlag) {
                                    freeSnakePoints(x, i, value)
                                }
                            }
                            else {
                                if(Random.nextInt(99) < config.deadFoodProb)
                                    globalState.foods.add(Coord(x, i))
                            }
                        }
                        y = (y + config.height)% config.height
                        for(i in y until (config.height)) {
                            if(!foodFlag) {
                                field[i][x][0] += value
                                if (value == 1) field[i][x].add(snake.player_id)
                                if (value == -1) field[i][x].remove(snake.player_id)

                                if (freeFlag) {
                                    freeSnakePoints(x, i, value)
                                }
                            }
                            else {
                                if(Random.nextInt(99) < config.deadFoodProb)
                                    globalState.foods.add(Coord(x, i))
                            }
                        }
                    }
                    else {
                        if(y >= config.height) {
                            for(i in prevy + 1 until (config.height)) {
                                if(!foodFlag) {
                                    field[i][x][0] += value
                                    if (value == 1) field[i][x].add(snake.player_id)
                                    if (value == -1) field[i][x].remove(snake.player_id)

                                    if (freeFlag) {
                                        freeSnakePoints(x, i, value)
                                    }
                                }
                                else {
                                    if(Random.nextInt(99) < config.deadFoodProb)
                                        globalState.foods.add(Coord(x, i))
                                }
                            }
                            y = (y + config.height)% config.height
                            for(i in 0 until (y+1)) {
                                if(!foodFlag) {
                                    field[i][x][0] += value
                                    if (value == 1) field[i][x].add(snake.player_id)
                                    if (value == -1) field[i][x].remove(snake.player_id)

                                    if (freeFlag) {
                                        freeSnakePoints(x, i, value)
                                    }
                                }
                                else {
                                    if(Random.nextInt(99) < config.deadFoodProb)
                                        globalState.foods.add(Coord(x, i))
                                }
                            }
                        }
                        else {
                            leftx = x
                            rightx = x
                            lefty = if (y < prevy) y else (prevy + 1)
                            righty = if (y < prevy) (prevy - 1) else y
                            for (i in leftx until (rightx + 1)) {
                                for (j in lefty until (righty + 1)) {
                                    if(!foodFlag) {
                                        field[j][i][0] += value
                                        if (value == 1) field[j][i].add(snake.player_id)
                                        if (value == -1) field[j][i].remove(snake.player_id)

                                        if (freeFlag) {
                                            freeSnakePoints(i, j, value)
                                        }
                                    }
                                    else {
                                        if(Random.nextInt(99) < config.deadFoodProb)
                                            globalState.foods.add(Coord(x, i))
                                    }
                                }
                            }
                        }
                    }
                }
                if(prevy == y) {
                    if(x < 0) {
                        for(i in 0 until (prevx)) {
                            if(!foodFlag) {
                                field[y][i][0] += value
                                if (value == 1) field[y][i].add(snake.player_id)
                                if (value == -1) field[y][i].remove(snake.player_id)

                                if (freeFlag) {
                                    freeSnakePoints(i, y, value)
                                }
                            }
                            else {
                                if(Random.nextInt(99) < config.deadFoodProb)
                                    globalState.foods.add(Coord(i, y))
                            }
                        }
                        x = (x + config.width)% config.width
                        for(i in x until (config.width)) {
                            if(!foodFlag) {
                                field[y][i][0] += value
                                if (value == 1) field[y][i].add(snake.player_id)
                                if (value == -1) field[y][i].remove(snake.player_id)

                                if (freeFlag) {
                                    freeSnakePoints(i, y, value)
                                }
                            }
                            else {
                                if(Random.nextInt(99) < config.deadFoodProb)
                                    globalState.foods.add(Coord(i, y))
                            }
                        }
                    }
                    else {
                        if(x >= config.width) {
                            for(i in prevx+1 until (config.width)) {
                                if(!foodFlag) {
                                    field[y][i][0] += value
                                    if (value == 1) field[y][i].add(snake.player_id)
                                    if (value == -1) field[y][i].remove(snake.player_id)

                                    if (freeFlag) {
                                        freeSnakePoints(i, y, value)
                                    }
                                }
                                else {
                                    if(Random.nextInt(99) < config.deadFoodProb)
                                        globalState.foods.add(Coord(i, y))
                                }
                            }
                            x = (x + config.width)% config.width
                            for(i in 0 until (x+1)) {
                                if(!foodFlag) {
                                    field[y][i][0] += value
                                    if (value == 1) field[y][i].add(snake.player_id)
                                    if (value == -1) field[y][i].remove(snake.player_id)

                                    if (freeFlag) {
                                        freeSnakePoints(i, y, value)
                                    }
                                }
                                else {
                                    if(Random.nextInt(99) < config.deadFoodProb)
                                        globalState.foods.add(Coord(i, y))
                                }
                            }
                        }
                        else {
                            leftx = if (x < prevx) x else (prevx + 1)
                            rightx = if (x < prevx) (prevx - 1) else x
                            lefty = y
                            righty = y

                            for (i in leftx until (rightx + 1)) {
                                for (j in lefty until (righty + 1)) {
                                    if(!foodFlag) {
                                        field[j][i][0] += value
                                        if (value == 1) field[j][i].add(snake.player_id)
                                        if (value == -1) field[j][i].remove(snake.player_id)

                                        if (freeFlag) {
                                            freeSnakePoints(i, j, value)
                                        }
                                    }
                                    else {
                                        if(Random.nextInt(99) < config.deadFoodProb)
                                            globalState.foods.add(Coord(i, j))
                                    }
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
                if(value == -1) field[y][x].remove(snake.player_id)

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
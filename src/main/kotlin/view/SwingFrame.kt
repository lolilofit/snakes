package view

import creator.globalState
import model.Snake
import presentation.ImmediateQueue
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent
import javax.swing.JFrame
import kotlin.math.abs

val cellSize = 20

class SwingFrame(title : String) : JFrame() {

    lateinit var snakes : ArrayList<Snake>

    init {
        createFrame(title);
    }
    private fun createFrame(title: String) {
        setTitle(title)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(1200, 600)
        setLocationRelativeTo(null)
    }
}


class CustomGraphics(width : Int, height : Int) : JComponent() {

    init {
        preferredSize = Dimension(width*cellSize, height* cellSize)
    }

    private fun parabol(x : Int) : Int {
        return cellSize + (x-(3/10)*cellSize)*(x-(3/10)*cellSize) - (1/100)* cellSize*cellSize
    }

    private fun drawBackground(g : Graphics?) {
        g?.fillRect(0, 0, width* cellSize, height*cellSize)
        g?.color = Color.GRAY
        for(i in 0..width)
            g?.drawLine(0, cellSize*i + cellSize, width*cellSize, cellSize*i+ cellSize)
        for(i in 0..height)
            g?.drawLine(cellSize*i + cellSize, 0, cellSize*i+ cellSize, height* cellSize)
    }
    private fun drawFood(g : Graphics?) {
        g?.color = Color.BLUE
        globalState.foods.forEach{food ->
            for (i in (cellSize / 5)..(cellSize - cellSize / 5)) {
                g?.drawLine(food.x * cellSize, (food.y) * cellSize + i, food.x * cellSize + cellSize, (food.y) * cellSize + i)
            }
        }
    }

    public override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        drawBackground(g)

        synchronized(ImmediateQueue::class) {
            drawFood(g)
            g?.color = Color.GREEN
            globalState.snakes.forEach { snake ->
                var x1 = snake.points[0].x
                var y1 = snake.points[0].y
                var x2 = x1
                var y2 = y1
                for (iter in 0..(snake.points.size - 2)) {
                    x1 = x2
                    y1 = y2
                    x2 = x1 + snake.points[iter + 1].x
                    y2 = y1 + snake.points[iter + 1].y

                    //??
                    for (i in (cellSize / 5)..(cellSize - cellSize / 5)) {
                        if (x1 != x2 && y1 == y1 || x1 == x2 && y1 == y2) {
                            if(x2 >= x1)
                                g?.drawLine(x1 * cellSize, (y1) * cellSize + i, x2 * cellSize + cellSize, (y2) * cellSize + i)
                            else
                                g?.drawLine(x2 * cellSize, (y1) * cellSize + i, x1 * cellSize + cellSize, (y2) * cellSize + i)
                        }
                        if (y1 != y2 && x1 == x2) {
                            if(y2 >= y1)
                                g?.drawLine((x1) * cellSize + i, y1 * cellSize, (x2) * cellSize + i, y2 * cellSize + cellSize)
                            else
                                g?.drawLine((x1) * cellSize + i, y2 * cellSize, (x2) * cellSize + i, y1 * cellSize + cellSize)
                        }
                    }

                    if (x1 == x2) {
                        if (y2 < 0) {
                            y2 += height / cellSize
                            y1 = height / cellSize
                            for (i in (cellSize / 5)..(cellSize - cellSize / 5)) {
                                g?.drawLine((x1) * cellSize + i, y2 * cellSize, (x2) * cellSize + i, y1 * cellSize + cellSize)
                            }
                        }
                        if (y2 >= height / cellSize) {
                                y1 = 0
                                y2 -= height / cellSize
                                for (i in (cellSize / 5)..(cellSize - cellSize / 5)) {
                                    g?.drawLine((x1) * cellSize + i, y1 * cellSize, (x2) * cellSize + i, y2 * cellSize + cellSize)
                                }
                        }
                    }
                    if(y1 == y2) {
                        if(x2 < 0) {
                            x2 += width / cellSize
                            x1 = width / cellSize
                            for (i in (cellSize / 5)..(cellSize - cellSize / 5)) {
                                g?.drawLine(x2 * cellSize, (y1) * cellSize + i, x1 * cellSize + cellSize, (y2) * cellSize + i)
                            }
                        }
                        if(x2 >= width / cellSize) {
                            x1 = 0
                            x2 -= width/ cellSize
                            for (i in (cellSize / 5)..(cellSize - cellSize / 5)) {
                                g?.drawLine(x1 * cellSize, (y1) * cellSize + i, x2 * cellSize + cellSize, (y2) * cellSize + i)
                            }
                        }
                    }
                }
            }
        }
    }


}
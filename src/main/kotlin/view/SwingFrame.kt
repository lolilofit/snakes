package view

import creator.globalState
import model.Snake
import presentation.ImmediateQueue
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import javax.swing.JComponent
import javax.swing.JFrame



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
    private var cellSizeW = 40
    private var cellSizeH = 40

    var widthLoc = 0
    var heightLoc = 0

    fun setSizes(h : Int, w : Int) {
        cellSizeH = 400 / h
        cellSizeW = 400 / w
    }
    init {
        preferredSize = Dimension(width*cellSizeW, height* cellSizeH)
        widthLoc = width*cellSizeW
        heightLoc = height*cellSizeH
    }

    private fun drawBackground(g : Graphics?) {
        g?.fillRect(0, 0, widthLoc* cellSizeW, heightLoc*cellSizeH)
        g?.color = Color.GRAY
        for(i in 0..widthLoc)
            g?.drawLine(0, cellSizeH*i + cellSizeH, widthLoc*cellSizeW, cellSizeH*i+ cellSizeH)
        for(i in 0..heightLoc)
            g?.drawLine(cellSizeW*i + cellSizeW, 0, cellSizeW*i+ cellSizeW, heightLoc* cellSizeH)
    }
    private fun drawFood(g : Graphics?) {
        g?.color = Color.WHITE
        globalState.foods.forEach{food ->
            for (i in (cellSizeH / 5)..(cellSizeH - cellSizeH / 5)) {
                g?.drawLine(food.x * cellSizeW, (food.y) * cellSizeH + i, food.x * cellSizeW + cellSizeW, (food.y) * cellSizeH + i)
            }
        }
    }


    public override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        drawBackground(g)


        synchronized(ImmediateQueue::class) {
            drawFood(g)

            globalState.snakes.forEach { snake ->

                g?.color = Color((snake.player_id*snake.player_id*200 + (snake.player_id%3)*100)%255, ((snake.player_id*snake.player_id*100 + 200*(((snake.player_id+1)*520)%3))%255), (snake.player_id*10))

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

                        if (x1 != x2 && y1 == y1 || x1 == x2 && y1 == y2) {
                            for (i in (cellSizeH / 5)..(cellSizeH - cellSizeH / 5)) {
                                if (x2 >= x1)
                                    g?.drawLine(x1 * cellSizeW, (y1) * cellSizeH + i, x2 * cellSizeW + cellSizeW, (y2) * cellSizeH + i)
                                else
                                    g?.drawLine(x2 * cellSizeW, (y1) * cellSizeH + i, x1 * cellSizeW + cellSizeW, (y2) * cellSizeH + i)
                            }
                        }
                        if (y1 != y2 && x1 == x2) {
                            for (i in (cellSizeW / 5)..(cellSizeW - cellSizeW / 5)) {
                                if (y2 >= y1)
                                    g?.drawLine((x1) * cellSizeW + i, y1 * cellSizeH, (x2) * cellSizeW + i, y2 * cellSizeH + cellSizeH)
                                else
                                    g?.drawLine((x1) * cellSizeW + i, y2 * cellSizeH, (x2) * cellSizeW + i, y1 * cellSizeH + cellSizeH)
                            }
                        }


                    if (x1 == x2) {
                        if (y2 < 0) {
                            y2 += heightLoc / cellSizeH
                            y1 = heightLoc / cellSizeH
                            for (i in (cellSizeW / 5)..(cellSizeW - cellSizeW / 5)) {
                                g?.drawLine((x1) * cellSizeW + i, y2 * cellSizeH, (x2) * cellSizeW + i, y1 * cellSizeH + cellSizeH)
                            }
                        }
                        if (y2 >= heightLoc / cellSizeH) {
                            y1 = 0
                            y2 -= heightLoc / cellSizeH
                            for (i in (cellSizeW / 5)..(cellSizeW - cellSizeW / 5)) {
                                g?.drawLine((x1) * cellSizeW + i, y1 * cellSizeH, (x2) * cellSizeW + i, y2 * cellSizeH + cellSizeH)
                            }
                        }
                    }
                    if(y1 == y2) {
                        if(x2 < 0) {
                            x2 += widthLoc / cellSizeW
                            x1 = widthLoc / cellSizeW
                            for (i in (cellSizeH / 5)..(cellSizeH - cellSizeH / 5)) {
                                g?.drawLine(x2 * cellSizeW, (y1) * cellSizeH + i, x1 * cellSizeW + cellSizeW, (y2) * cellSizeH + i)
                            }
                        }
                        if(x2 >= widthLoc / cellSizeW) {
                            x1 = 0
                            x2 -= widthLoc / cellSizeW
                            for (i in (cellSizeH / 5)..(cellSizeH - cellSizeH / 5)) {
                                g?.drawLine(x1 * cellSizeW, (y1) * cellSizeH + i, x2 * cellSizeW + cellSizeW, (y2) * cellSizeH + i)
                            }
                        }
                    }
                }
                g?.color = Color.WHITE
                val x = snake.points[0].x* cellSizeW + cellSizeW/5
                val y  = snake.points[0].y* cellSizeH + cellSizeH/5

                val g2d = g as Graphics2D
                val circle : Ellipse2D.Double  = Ellipse2D.Double(x.toDouble(), y.toDouble() + cellSizeH.toDouble()/5,  (cellSizeW).toDouble()/4.0, (cellSizeH).toDouble()/4.0);
                g2d.fill(circle)
            }
        }
    }


}
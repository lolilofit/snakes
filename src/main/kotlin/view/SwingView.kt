package view

import creator.config
import me.ippolitov.fit.snakes.SnakesProto
import presentation.move.Move
import presentation.move.moveimpl.Exit
import presentation.move.moveimpl.MoveFromButton
import presentation.move.moveimpl.StartNewGame
import java.awt.BorderLayout
import java.awt.GridLayout
import java.util.concurrent.ArrayBlockingQueue
import javax.swing.JButton
import javax.swing.JPanel

class SwingView(queue : ArrayBlockingQueue<Pair<Move, List<Any?>>>) : View {
    private lateinit var graphic : CustomGraphics
    private lateinit var frame : SwingFrame
    private val queue = queue

    private fun addMoveButtons(panel : JPanel) {
        val contentsButtons = JPanel(BorderLayout())
        val upButton = JButton("up")
        upButton.addActionListener { queue.put(Pair(MoveFromButton, listOf(SnakesProto.Direction.UP))) }
        contentsButtons.add(upButton, BorderLayout.NORTH)

        val downButton = JButton("down")
        downButton.addActionListener { queue.put(Pair(MoveFromButton, listOf(SnakesProto.Direction.DOWN))) }
        contentsButtons.add(downButton, BorderLayout.SOUTH)

        val rightButton = JButton("right")
        rightButton.addActionListener { queue.put(Pair(MoveFromButton, listOf(SnakesProto.Direction.RIGHT))) }
        contentsButtons.add(rightButton, BorderLayout.EAST)

        val leftButton = JButton("left")
        leftButton.addActionListener { queue.put(Pair(MoveFromButton, listOf(SnakesProto.Direction.LEFT))) }
        contentsButtons.add(leftButton, BorderLayout.WEST)

        panel.add(contentsButtons)

    }

    private fun addRightPanel(panel : JPanel) {
        val newGame = JButton("New Game")
        newGame.addActionListener { queue.put(Pair(StartNewGame, listOf(this))) }
        panel.add(newGame)
        val connectTogame = JButton("Show games")
        panel.add(connectTogame)
        val exit = JButton("Exit")
        exit.addActionListener { Exit.execute(emptyList()) }
        panel.add(exit)
    }

    private fun createGameField() {
        frame = SwingFrame("Snakes")
        val contents = JPanel(GridLayout(1, 3, 5, 1))
        val panel = JPanel()
        val secondary = JPanel()
        panel.add(graphic)
        addMoveButtons(panel)
        addRightPanel(secondary)
        contents.add(panel)
        contents.add(secondary)
        frame.contentPane = contents
        frame.pack()
        frame.isVisible = true
    }

    override fun update() {
        synchronized(SwingView::class) {
            frame.validate()
            frame.repaint()
        }
    }

    fun run() {
        graphic = CustomGraphics(config.width, config.height)
        createGameField()
    }

}
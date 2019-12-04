package view

import creator.config
import creator.globalState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ippolitov.fit.snakes.SnakesProto
import presentation.move.Move
import presentation.move.moveimpl.Exit
import presentation.move.moveimpl.MoveFromButton
import presentation.move.moveimpl.StartNewGame
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.util.concurrent.ArrayBlockingQueue
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class SwingView(queue : ArrayBlockingQueue<Pair<Move, List<Any?>>>) : View {

    private lateinit var graphic : CustomGraphics
    private lateinit var frame : SwingFrame
    private val queue = queue
    private val tableData = TableData()
    private val table = JTable(DefaultTableModel(tableData.data, tableData.names))
    private val connectGame = ConnectGameView()

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
        connectTogame.addActionListener{
            GlobalScope.launch { connectGame.run() }
        }
        panel.add(connectTogame)
        val exit = JButton("Exit")
        exit.addActionListener { Exit.execute(emptyList()) }
        panel.add(exit)
    }

    private fun drawTable(panel : JPanel) {
        val pane = JScrollPane(table)
        panel.add(pane)
    }

    private fun createGameField() {
        frame = SwingFrame("Snakes")
        val contents = JPanel(GridLayout(2, 3, 1, 1))
        val panel = JPanel()
        val secondary = JPanel()
        val table = JPanel()


        panel.add(graphic)
        addMoveButtons(secondary)
        addRightPanel(secondary)
        drawTable(table)
        table.preferredSize = Dimension(300, 200)

        contents.add(panel )
        contents.add(table)
        contents.add(secondary)

        frame.contentPane = contents
        frame.pack()
        frame.isVisible = true
    }

    override fun update() {
        synchronized(SwingView::class) {
            val model : DefaultTableModel = table.model as DefaultTableModel
            globalState.game_players.players.forEach{player ->
                var flag = true
                for(i in 0 until model.rowCount) {
                    if(player.name == model.getValueAt(i, 0)) {
                        model.setValueAt(player.score.toString(), i, 1)
                        flag = false
                        break
                    }
                }
                if(flag)
                    model.addRow(arrayOf(player.name, player.score.toString()))
            }
            frame.validate()
            frame.repaint()
        }
    }

    fun run() {
        graphic = CustomGraphics(config.width, config.height)
        createGameField()
    }

}
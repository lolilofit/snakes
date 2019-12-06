package view

import creator.config
import creator.currentGames
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import model.CurrentGames
import presentation.ImmediateQueue
import presentation.move.moveimpl.JoinSender
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane


class ConnectGameView {
    public fun run() {
        val frame = SwingFrame("Current Games")
        val pane = JPanel()
        val buttons = JPanel()
        buttons.layout = BoxLayout(buttons, BoxLayout.PAGE_AXIS)

        val jScrollPane = JScrollPane()
        //jScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        jScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        var number = 0
        val newCurrentGame = CurrentGames()

        synchronized(ImmediateQueue::class) {
            currentGames.currentGames.forEach{game ->
                if(System.currentTimeMillis() - game.value.lastTimeAnnouncement < config.nodeTimeoutMs) {
                    var button = JButton("master adress: " + game.key.first + " " + game.key.second)
                    button.addActionListener {
                        JoinSender.execute(listOf(game.key.first,
                                game.key.second,
                                game.value,
                                "player",
                                0.toLong()
                        ))
                        frame.dispose()
                    }
                    buttons.add(button)
                }
            }
        }
        jScrollPane.viewport.add(buttons)
        pane.add(jScrollPane)
        frame.contentPane = pane
        frame.pack()
        frame.isVisible = true

    }
}
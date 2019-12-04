package view

import creator.currentGames
import presentation.ImmediateQueue
import presentation.move.moveimpl.JoinSender
import javax.swing.*

class ConnectGameView {
    public fun run() {
        val frame = SwingFrame("Current Games")
        val pane = JPanel()
        val jScrollPane = JScrollPane()
        jScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        jScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

        synchronized(ImmediateQueue::class) {
            currentGames.currentGames.forEach{game ->
                val button = JButton("master adress: " + game.key.first + " " + game.key.second)
                button.addActionListener{
                        JoinSender.execute(listOf(game.key.first,
                                game.key.second,
                                game.value,
                                "player",
                                0.toLong()
                        ))
                        frame.dispose()
                    }
                jScrollPane.viewport.add(button)
            }
        }

        pane.add(jScrollPane)
        frame.contentPane = pane
        frame.pack()
        frame.isVisible = true

    }
}
package presentation.notmaster

import model.CurrentGames
import java.net.DatagramPacket

interface MessagesType {
    fun execute(packet: DatagramPacket, currentGames : CurrentGames)
}
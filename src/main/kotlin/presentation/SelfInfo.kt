package presentation

import java.net.InetAddress
import java.net.MulticastSocket

object SelfInfo {
    var socket : MulticastSocket = MulticastSocket(9192)
    var port = 9192
    var selfId : Int = -1
    var masterinfo : Int = -1
    var group = InetAddress.getByName("239.192.0.4")
}
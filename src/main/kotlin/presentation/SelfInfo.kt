package presentation

import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import kotlin.properties.Delegates

object SelfInfo {
    val multicastSocket : MulticastSocket = MulticastSocket(9192)
    val multicastport = 9192
    var regularPort by Delegates.notNull<Int>()
    lateinit var regularSocket : DatagramSocket
    var selfId : Int = -1
    var masterInfo = -1
    var masterPort : Int = -1
    var masterIp : String = ""
    var group = InetAddress.getByName("239.192.0.4")
}
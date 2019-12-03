import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.MasterTools
import presentation.move.Move
import presentation.notmaster.Resender
import view.SwingView
import java.net.DatagramSocket
import java.net.MulticastSocket
import java.util.concurrent.ArrayBlockingQueue
import java.net.InetAddress

fun main(args: Array<String>) {
        //init udp socket
        val port = readLine()?.toInt()
        val group = InetAddress.getByName("239.192.0.4")
        SelfInfo.multicastSocket.joinGroup(group)
        SelfInfo.group = group
        if (port != null) {
                SelfInfo.regularPort = port
                SelfInfo.regularSocket = DatagramSocket(port)
        }

        val queue = ArrayBlockingQueue<Pair<Move, List<Any?>>>(10, true)
        val view = SwingView(queue)
        GlobalScope.launch { view.run() }
        MasterTools.initThreadPool(view)
        MasterTools.startNonMaster()
        GlobalScope.launch { Resender.runResend() }

        val presenter = ImmediateQueue(view, queue)
        presenter.run()
}

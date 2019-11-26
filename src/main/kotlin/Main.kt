import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import presentation.ImmediateQueue
import presentation.SelfInfo
import presentation.master.MasterTools
import presentation.move.Move
import view.SwingView
import java.net.MulticastSocket
import java.util.concurrent.ArrayBlockingQueue
import java.net.InetAddress

fun main(args: Array<String>) {
        //init udp socket
        val group = InetAddress.getByName("239.192.0.4")
        SelfInfo.socket.joinGroup(group)
        SelfInfo.group = group

        val queue = ArrayBlockingQueue<Pair<Move, List<Any?>>>(10, true)
        val view = SwingView(queue)
        GlobalScope.launch { view.run() }
        MasterTools.initThreadPool(view)
        MasterTools.startNonMaster()

        val presenter = ImmediateQueue(view, queue)
        presenter.run()
}

package presentation.master

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import presentation.notmaster.GetMulticastMessage
import presentation.notmaster.GetRegularPayerMessage
import view.View
import java.util.*
import kotlin.collections.HashMap

object MasterTools {
    private val masterTasks : MutableMap<String, Task> = HashMap()
    private val regularTasks : MutableMap<String, Task> = HashMap()
    private val launchedTasks = Stack<Job>()

    fun initThreadPool(view : View) {
        masterTasks["TimeoutQueue"] = TimeoutQueue
        masterTasks["TimeoutQueue"]?.setView(view)
        regularTasks["GetRegularPlayerMessage"] = GetRegularPayerMessage
        regularTasks["GetRegularPlayerMessage"]?.setView(view)
        regularTasks["GetMulticastPlayerMessage"] = GetMulticastMessage
        regularTasks["GetMulticastPlayerMessage"]?.setView(view)
    }

    fun startMasterTasks() {
        launchedTasks.forEach{job -> job.cancel()}
        launchedTasks.clear()

        masterTasks.forEach { task ->
            launchedTasks.push(GlobalScope.launch { task.value.run() })
        }
    }

    fun startNonMaster() {
        launchedTasks.clear()
        regularTasks.forEach{task -> launchedTasks.push(GlobalScope.launch {
            task.value.run() })
        }
    }

    fun endMasterTasks() {
        launchedTasks.forEach{job -> job.cancel()}
        launchedTasks.clear()
        regularTasks.forEach{task -> launchedTasks.push(GlobalScope.launch { task.value.run() }) }
    }
}
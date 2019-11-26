package presentation.master

import view.View

interface Task {
    fun cleanup()
    suspend fun run()
    fun setView(view : View)
}
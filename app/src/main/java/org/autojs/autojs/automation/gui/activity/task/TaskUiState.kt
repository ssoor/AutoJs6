package org.autojs.autojs.ui.modern.activity.task

/**
 * 任务 UI 状态
 */
data class TaskUiState(
    val runningTasks: List<TaskItem> = emptyList(),
    val pendingTasks: List<TaskItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean
        get() = runningTasks.isEmpty() && pendingTasks.isEmpty()

    /**
     * 任务项
     */
    data class TaskItem(
        val id: Long,
        val name: String,
        val desc: String,
        val scriptPath: String,
        val engineName: String,
        val icon: Int,
        val type: TaskType,
        val startTime: Long = 0L,  // 运行中任务才有
        val nextRunTime: Long = 0L  // 待执行任务才有
    )
}

/**
 * 任务类型
 */
enum class TaskType {
    RUNNING,      // 运行中
    TIMED_TASK,   // 定时任务
    INTENT_TASK   // Intent 任务
}

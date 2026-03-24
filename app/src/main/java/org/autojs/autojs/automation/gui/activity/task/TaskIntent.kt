package org.autojs.autojs.ui.modern.activity.task

/**
 * 任务意图
 */
sealed class TaskIntent {
    object Refresh : TaskIntent()
    object StopAll : TaskIntent()
    data class StopTask(val taskId: Long) : TaskIntent()
    data class ShowTaskDetail(val taskId: Long) : TaskIntent()
    data class OpenScriptFile(val scriptPath: String) : TaskIntent()
    data class RestartTask(val taskId: Long, val scriptPath: String) : TaskIntent()
    data class ShowTaskLog(val taskId: Long) : TaskIntent()
}
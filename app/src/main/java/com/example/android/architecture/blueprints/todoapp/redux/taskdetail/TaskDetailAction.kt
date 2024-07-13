package com.example.android.architecture.blueprints.todoapp.redux.taskdetail

import com.example.android.architecture.blueprints.todoapp.data.Task

sealed class TaskDetailAction {

    data class GetTaskAction(val taskId: String) : TaskDetailAction()

    data class LoadedTask(val task: Task) : TaskDetailAction()

    data class LoadedError(val errorMessage: Int) : TaskDetailAction()

    data object Loading : TaskDetailAction()

    data class DeleteTaskAction(val taskId: String) : TaskDetailAction()
    data object DeletedTask : TaskDetailAction()

    data class CompleteTaskAction(val taskId: String) : TaskDetailAction()
    data object CompleteTask : TaskDetailAction()

    data class ActivateTaskAction(val taskId: String) : TaskDetailAction()

    data class RefreshTaskAction(val taskId: String) : TaskDetailAction()
    data object RefreshTask : TaskDetailAction()
}
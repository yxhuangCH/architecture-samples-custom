package com.example.android.architecture.blueprints.todoapp.redux.tasks

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType

sealed class TasksActions {

    data class GetTasksActions(
        val requestType: TasksFilterType
    ) : TasksActions()

    data class Loading(val isLoading: Boolean) : TasksActions()

    data class Error(val errorMsg: Int = 0) : TasksActions()

    data class LoadedTasks(val items: List<Task>) : TasksActions()

    data object ClearCompletedTasksActions : TasksActions()

    data class CompleteTaskActions(val taskId: String) : TasksActions()

    data class ActivateTaskActions(val taskId: String) : TasksActions()

    data object RefreshActions : TasksActions()

    data class TasksFilterAction(val requestType: TasksFilterType): TasksActions()

    data class UpdateUserMessaging(val message: Int? = null): TasksActions()
}
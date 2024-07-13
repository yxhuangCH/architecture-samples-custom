package com.example.android.architecture.blueprints.todoapp

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiState
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskAction
import com.example.android.architecture.blueprints.todoapp.redux.statistics.StatisticsAction
import com.example.android.architecture.blueprints.todoapp.redux.taskdetail.TaskDetailAction
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsUiState
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailUiState

data class AppState(
    val addEditTaskUiState: AddEditTaskUiState = AddEditTaskUiState(),
    val statisticsUiState: StatisticsUiState = StatisticsUiState(),
    val taskDetailUiState: TaskDetailUiState = TaskDetailUiState()
)

sealed class AppAction {
    data class AddEditTask(val actions: AddEditTaskAction) : AppAction()
    data class Statistics(val actions: StatisticsAction) : AppAction()

    data class TaskDetail(val actions: TaskDetailAction) : AppAction()
}

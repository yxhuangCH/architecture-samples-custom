package com.example.android.architecture.blueprints.todoapp

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiState
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskAction
import com.example.android.architecture.blueprints.todoapp.redux.statistics.StatisticsAction
import com.example.android.architecture.blueprints.todoapp.redux.taskdetail.TaskDetailAction
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsUiState
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailUiState
import com.toggl.komposable.architecture.Store
import javax.inject.Inject

class AppStoreManager @Inject constructor() {

    @Inject
    lateinit var appStore: Store<AppState, AppAction>

    fun addEditTaskStore(): Store<AddEditTaskUiState, AddEditTaskAction> = appStore.view(
        mapToLocalState = { it.addEditTaskUiState },
        mapToGlobalAction = { AppAction.AddEditTask(it) }
    )

    fun statisticsStore(): Store<StatisticsUiState, StatisticsAction> = appStore.view(
        mapToLocalState = { it.statisticsUiState },
        mapToGlobalAction = { AppAction.Statistics(it) }
    )

    fun taskDetailStore(): Store<TaskDetailUiState, TaskDetailAction> = appStore.view(
        mapToLocalState = { it.taskDetailUiState },
        mapToGlobalAction = { AppAction.TaskDetail(it) }
    )

}

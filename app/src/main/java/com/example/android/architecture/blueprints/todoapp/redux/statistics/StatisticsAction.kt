package com.example.android.architecture.blueprints.todoapp.redux.statistics

import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsUiModel

sealed class StatisticsAction {

    data object RefreshAction : StatisticsAction()

    data object RefreshCompleted : StatisticsAction()

    data object GetTasksAction : StatisticsAction()

    data object Loading : StatisticsAction()

    data object Error : StatisticsAction()

    data class LoadedTasksAction(val uiModel: StatisticsUiModel) : StatisticsAction()

}

package com.example.android.architecture.blueprints.todoapp.redux.statistics

import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.di.ApplicationScope
import com.example.android.architecture.blueprints.todoapp.di.IoDispatcher
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsUiState
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withFlowEffect
import com.toggl.komposable.extensions.withoutEffect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class StatisticsReducer(
    private val taskRepository: TaskRepository,
    @ApplicationScope
    private val scope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : Reducer<StatisticsUiState, StatisticsAction> {
    override fun reduce(
        state: StatisticsUiState,
        action: StatisticsAction
    ): ReduceResult<StatisticsUiState, StatisticsAction> = when (action) {
        is StatisticsAction.RefreshAction,
        is StatisticsAction.GetTasksAction -> {
            Timber.tag(TAG).d("StatisticsAction.GetTasksAction ")
            state.withFlowEffect(
                StatisticsEffect(
                    action = action,
                    taskRepository = taskRepository,
                    scope = scope,
                    ioDispatcher = ioDispatcher
                ).run()
            )
        }

        is StatisticsAction.RefreshCompleted -> {
            Timber.tag(TAG).d("StatisticsAction.RefreshCompleted")
            state.copy(
                isLoading = false,
                isEmpty = false
            ).withoutEffect()
        }

        is StatisticsAction.LoadedTasksAction -> {
            Timber.tag(TAG).d("StatisticsAction.LoadedTasksAction: $action")
            StatisticsUiState(
                isEmpty = action.uiModel.isEmpty,
                isLoading = false,
                activeTasksPercent = action.uiModel.activeTasksPercent,
                completedTasksPercent = action.uiModel.completedTasksPercent
            ).withoutEffect()
        }

        is StatisticsAction.Loading -> {
            Timber.tag(TAG).d("StatisticsAction.Loading")
            state.copy(
                isLoading = true,
                isEmpty = true
            ).withoutEffect()
        }

        is StatisticsAction.Error -> {
            Timber.tag(TAG).d("StatisticsAction.Error")
            state.copy(
                isLoading = false,
                isEmpty = true
            ).withoutEffect()
        }
    }

    companion object {
        private const val TAG = "StatisticsReducer"
    }
}
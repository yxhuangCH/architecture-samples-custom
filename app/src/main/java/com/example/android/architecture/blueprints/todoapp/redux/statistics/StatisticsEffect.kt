package com.example.android.architecture.blueprints.todoapp.redux.statistics

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.di.ApplicationScope
import com.example.android.architecture.blueprints.todoapp.di.IoDispatcher
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsUiModel
import com.example.android.architecture.blueprints.todoapp.statistics.getActiveAndCompletedStats
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.toggl.komposable.architecture.Effect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class StatisticsEffect(
    private val action: StatisticsAction,
    private val taskRepository: TaskRepository,
    @ApplicationScope
    private val scope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : Effect<StatisticsAction> {
    override fun run(): Flow<StatisticsAction> = channelFlow {
        when (action) {
            is StatisticsAction.GetTasksAction -> {
                Timber.tag(TAG).d("StatisticsAction.GetTasksAction ")
                handleGetTasks(this)
                awaitClose()
            }

            is StatisticsAction.RefreshAction -> {
                Timber.tag(TAG).d("StatisticsAction.RefreshAction ")
                handleRefresh()
            }

            else -> {}
        }
    }

    private fun handleRefresh() {
        scope.launch {
            taskRepository.refresh()
        }
    }

    private fun handleGetTasks(producerScope: ProducerScope<StatisticsAction>) {
        scope.launch {
            taskRepository.getTasksStream()
                .map { Async.Success(it) }
                .catch<Async<List<Task>>> { emit(Async.Error(R.string.loading_tasks_error)) }
                .map { taskAsync ->
                    produceStatistics2Action(taskAsync)
                }.flowOn(ioDispatcher)
                .collect { action ->
                    Timber.tag(TAG).d("handleGetTasks action: $action")
                    producerScope.channel.send(action)
                }
        }
    }

    private fun produceStatistics2Action(taskLoad: Async<List<Task>>) =
        when (taskLoad) {
            Async.Loading -> {
                StatisticsAction.Loading
            }

            is Async.Error -> {
                StatisticsAction.Error
            }

            is Async.Success -> {
                val stats = getActiveAndCompletedStats(taskLoad.data)
                StatisticsAction.LoadedTasksAction(
                    StatisticsUiModel(
                        isEmpty = taskLoad.data.isEmpty(),
                        activeTasksPercent = stats.activeTasksPercent,
                        completedTasksPercent = stats.completedTasksPercent,
                    )
                )
            }
        }

    companion object {
        private const val TAG = "StatisticsEffect"
    }
}

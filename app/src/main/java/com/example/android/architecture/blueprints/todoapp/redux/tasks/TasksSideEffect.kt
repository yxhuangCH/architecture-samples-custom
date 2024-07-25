package com.example.android.architecture.blueprints.todoapp.redux.tasks

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.di.ApplicationScope
import com.example.android.architecture.blueprints.todoapp.di.IoDispatcher
import com.toggl.komposable.architecture.Effect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class TasksSideEffect(
    private val action: TasksActions,
    private val taskRepository: TaskRepository,
    @ApplicationScope
    private val scope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : Effect<TasksActions> {
    override fun run(): Flow<TasksActions> = channelFlow {
        when (action) {
            is TasksActions.GetTasksActions -> {
                Timber.tag(TAG).d("TasksAction.GetTasksAction $action")
                handleGetTask(this)
            }

            is TasksActions.ClearCompletedTasksActions -> {
                handleCleanTask()
            }

            is TasksActions.RefreshActions -> {
                Timber.tag(TAG).d("TasksAction.RefreshActions")
                handleRefresh(this)
            }

            is TasksActions.CompleteTaskActions -> {
                Timber.tag(TAG).d("TasksAction.CompleteTaskActions id: ${action.taskId}")
                handleCompleteTask(action, this)
            }

            is TasksActions.ActivateTaskActions -> {
                Timber.tag(TAG).d("TasksAction.ActivateTaskActions id: ${action.taskId}")
                handleActivateTask(action, this)
            }

            else -> {

            }
        }
        awaitClose {}
    }

    private fun handleGetTask(
        producer: ProducerScope<TasksActions>
    ) {
        scope.launch(ioDispatcher) {
            taskRepository.getTasksStream()
                .map {
//                    it.forEach { _task ->
//                        Timber.tag(TAG).d("handleGetTask $_task")
//                    }
                    producer.send(
                        TasksActions.LoadedTasks(
                            items = it
                        )
                    )
                }.catch {
                    producer.send(TasksActions.Error())
                }.launchIn(this)
        }
    }

    private fun handleCleanTask() {
        scope.launch {
            taskRepository.clearCompletedTasks()
        }
    }

    private fun handleRefresh(
        producer: ProducerScope<TasksActions>
    ) {
        scope.launch {
            producer.send(TasksActions.Loading(true))
            taskRepository.refresh()
            producer.send(TasksActions.Loading(false))
        }
    }

    private fun handleCompleteTask(
        action: TasksActions.CompleteTaskActions,
        producer: ProducerScope<TasksActions>
    ) {
        scope.launch {
            producer.send(TasksActions.Loading(true))
            taskRepository.completeTask(action.taskId)
        }
    }

    private fun handleActivateTask(
        action: TasksActions.ActivateTaskActions,
        producer: ProducerScope<TasksActions>
    ) {
        scope.launch {
            producer.send(TasksActions.Loading(true))
            taskRepository.activateTask(action.taskId)
        }
    }

    companion object {
        private const val TAG = "TasksSideEffect"
    }
}
package com.example.android.architecture.blueprints.todoapp.redux.taskdetail

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.di.ApplicationScope
import com.example.android.architecture.blueprints.todoapp.di.IoDispatcher
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.toggl.komposable.architecture.Effect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class TaskDetailSideEffect(
    private val action: TaskDetailAction,
    private val taskRepository: TaskRepository,
    @ApplicationScope
    private val scope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : Effect<TaskDetailAction> {
    override fun run(): Flow<TaskDetailAction> = channelFlow {
        when (action) {
            is TaskDetailAction.GetTaskAction -> {
                Timber.tag(TAG).d("GetTaskAction: $action")
                send(TaskDetailAction.Loading)
                handleGetTask(action, this)
                awaitClose()
            }

            is TaskDetailAction.DeleteTaskAction -> {
                Timber.tag(TAG).d("DeleteTaskAction: $action")
                handleDelete(action, this)
                awaitClose()
            }

            is TaskDetailAction.CompleteTaskAction -> {
                Timber.tag(TAG).d("CompleteTaskAction: $action")
                handleComplete(action, this)
                awaitClose()
            }

            is TaskDetailAction.ActivateTaskAction -> {
                Timber.tag(TAG).d("ActivateTaskAction: $action")
                handleActivate(action, this)
                awaitClose()
            }

            is TaskDetailAction.RefreshTaskAction -> {
                Timber.tag(TAG).d("RefreshTaskAction: $action")
                send(TaskDetailAction.Loading)
                handleRefresh(action, this)
                awaitClose()
            }

            else -> {
                send(TaskDetailAction.Loading)
            }
        }
    }


    private fun handleGetTask(
        action: TaskDetailAction.GetTaskAction,
        producer: ProducerScope<TaskDetailAction>
    ) {
        scope.launch {
            val taskId = action.taskId
            taskRepository.getTaskStream(taskId)
                .map { handleTask(it) }
                .catch {
                    emit(TaskDetailAction.LoadedError(R.string.task_not_found))
                }.flowOn(ioDispatcher)
                .collect {
                    producer.send(it)
                }
        }
    }

    private fun handleTask(task: Task?): TaskDetailAction {
        if (task == null) {
            return TaskDetailAction.LoadedError(R.string.task_not_found)
        }
        return TaskDetailAction.LoadedTask(task)
    }

    private fun handleDelete(
        action: TaskDetailAction.DeleteTaskAction,
        producer: ProducerScope<TaskDetailAction>
    ) {
        scope.launch {
            taskRepository.deleteTask(action.taskId)
            producer.send(TaskDetailAction.DeletedTask)
        }
    }

    private fun handleComplete(
        action: TaskDetailAction.CompleteTaskAction,
        producer: ProducerScope<TaskDetailAction>
    ) {
        scope.launch {
            taskRepository.completeTask(action.taskId)
            producer.send(TaskDetailAction.CompleteTask)
        }
    }

    private fun handleActivate(
        action: TaskDetailAction.ActivateTaskAction,
        producer: ProducerScope<TaskDetailAction>
    ) {
        scope.launch {
            taskRepository.completeTask(action.taskId)
            producer.send(TaskDetailAction.CompleteTask)
        }
    }

    private fun handleRefresh(
        action: TaskDetailAction.RefreshTaskAction,
        producer: ProducerScope<TaskDetailAction>
    ) {
        scope.launch {
            taskRepository.refreshTask(action.taskId)
            producer.send(TaskDetailAction.RefreshTask)
        }
    }

    companion object {
        private const val TAG = "TaskDetailSideEffect"
    }

}
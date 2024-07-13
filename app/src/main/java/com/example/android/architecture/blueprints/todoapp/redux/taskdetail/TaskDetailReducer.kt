package com.example.android.architecture.blueprints.todoapp.redux.taskdetail

import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.di.ApplicationScope
import com.example.android.architecture.blueprints.todoapp.di.IoDispatcher
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailUiState
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withFlowEffect
import com.toggl.komposable.extensions.withoutEffect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class TaskDetailReducer(
    private val taskRepository: TaskRepository,
    @ApplicationScope
    private val scope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : Reducer<TaskDetailUiState, TaskDetailAction> {
    override fun reduce(
        state: TaskDetailUiState,
        action: TaskDetailAction
    ): ReduceResult<TaskDetailUiState, TaskDetailAction> = when (action) {
        is TaskDetailAction.GetTaskAction,
        is TaskDetailAction.DeleteTaskAction,
        is TaskDetailAction.CompleteTaskAction,
        is TaskDetailAction.ActivateTaskAction -> {
            Timber.tag(TAG).d("reduce action $action")
            state.withFlowEffect(
                TaskDetailSideEffect(
                    action = action,
                    taskRepository = taskRepository,
                    scope = scope,
                    ioDispatcher = ioDispatcher
                ).run()
            )
        }

        is TaskDetailAction.LoadedTask -> {
            Timber.tag(TAG).d("LoadedTask action $action")
            TaskDetailUiState(
                task = action.task,
                isLoading = false,
                userMessage = null,
                isTaskDeleted = false
            ).withoutEffect()
        }

        is TaskDetailAction.LoadedError -> {
            Timber.tag(TAG).d("LoadedError action $action")
            TaskDetailUiState(
                userMessage = action.errorMessage,
                isTaskDeleted = false
            )
            state.withoutEffect()
        }

        is TaskDetailAction.Loading -> {
            Timber.tag(TAG).d("Loading action $action")
            state.copy(
                isLoading = true
            ).withoutEffect()
        }

        is TaskDetailAction.DeletedTask -> {
            Timber.tag(TAG).d("DeletedTask $action")
            state.copy(
                isTaskDeleted = true
            ).withoutEffect()
        }

        is TaskDetailAction.CompleteTask -> {
            Timber.tag(TAG).d("CompleteTask $action")
            state.copy(
                isLoading = false
            ).withoutEffect()
        }

        is TaskDetailAction.RefreshTask -> {
            Timber.tag(TAG).d("RefreshTask $action")
            state.copy(
                isLoading = false
            ).withoutEffect()
        }

        else -> state.withoutEffect()
    }

    companion object {
        private const val TAG = "TaskDetailReducer"
    }
}
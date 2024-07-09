package com.example.android.architecture.blueprints.todoapp.redux.addedittask

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiMode
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiState
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withFlowEffect
import com.toggl.komposable.extensions.withSuspendEffect
import com.toggl.komposable.extensions.withoutEffect
import kotlinx.coroutines.flow.FlowCollector
import timber.log.Timber

class AddEditTaskReducer(
    private val taskRepository: TaskRepository,
): Reducer<AddEditTaskUiState, AddEditTaskAction> {
    override fun reduce(
        state: AddEditTaskUiState,
        action: AddEditTaskAction
    ): ReduceResult<AddEditTaskUiState, AddEditTaskAction> = when (action) {
        is AddEditTaskAction.LoadEditTask -> {
            Timber.tag(TAG).d("AddEditTaskAction.LoadEditTask")
            state.withFlowEffect(
                AddEditTaskSideEffect(action = action, taskRepository = taskRepository).run()
            )
        }

        is AddEditTaskAction.Loading -> {
            Timber.tag(TAG).d("AddEditTaskAction.Loading")
            state.copy(isLoading = true).withoutEffect()
        }

        is AddEditTaskAction.LoadedEditTask -> {
            Timber.tag(TAG).d("AddEditTaskAction.LoadedEditTask action: ${action.addEditTaskUiMode}")
            AddEditTaskUiState(
                title = action.addEditTaskUiMode.title,
                description = action.addEditTaskUiMode.description,
                isTaskCompleted = action.addEditTaskUiMode.isTaskCompleted,
                isLoading = action.addEditTaskUiMode.isLoading,
                userMessage = null,
                isTaskSaved = action.addEditTaskUiMode.isTaskSaved
            ).withoutEffect()
        }

        is AddEditTaskAction.UpdateEditTask -> {
            Timber.tag(TAG).d("AddEditTaskAction.UpdateEditTask action: ${action.description}")
            state.withFlowEffect(
                AddEditTaskSideEffect(action = action, taskRepository = taskRepository).run()
            )
        }

        else -> {
            state.withoutEffect()
        }
    }

    companion object {
        private const val TAG = "AddEditTaskReducer"
    }
}
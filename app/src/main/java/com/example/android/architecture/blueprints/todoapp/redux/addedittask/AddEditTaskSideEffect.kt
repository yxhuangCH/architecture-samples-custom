package com.example.android.architecture.blueprints.todoapp.redux.addedittask

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiMode
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.toggl.komposable.architecture.Effect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class AddEditTaskSideEffect(
    private val action: AddEditTaskAction,
    private val taskRepository: TaskRepository,
) : Effect<AddEditTaskAction> {
    override fun run(): Flow<AddEditTaskAction> = flow {
        when (action) {
            is AddEditTaskAction.LoadEditTask -> {
                Timber.tag(TAG).d("AddEditTaskAction.LoadEditTask")
                emit(AddEditTaskAction.Loading)
                handleLoadTask(action, this)
            }

            is AddEditTaskAction.CreateNewEditTask -> {
                Timber.tag(TAG).d("AddEditTaskAction.CreateNewEditTask")
                handleCreateNewTask(action, this)
            }

            is AddEditTaskAction.UpdateEditTask -> {
                Timber.tag(TAG).d("AddEditTaskAction.UpdateEditTask")
                handleUpdateTask(action, this)
            }

            else -> {}
        }
    }

    private suspend fun handleLoadTask(
        action: AddEditTaskAction.LoadEditTask,
        collector: FlowCollector<AddEditTaskAction>
    ) {
        val task = taskRepository.getTask(action.taskId)

        val loadedAction = if (task != null) {
            AddEditTaskAction.LoadedEditTask(
                AddEditTaskUiMode(
                    title = task.title,
                    description = task.description,
                    isTaskCompleted = task.isCompleted,
                    isLoading = false
                )
            )
        } else {
            AddEditTaskAction.LoadedEditTask(
                AddEditTaskUiMode(
                    isLoading = false
                )
            )
        }
        collector.emit(loadedAction)  // Emit safely within the suspend context
    }

    private suspend fun handleCreateNewTask(
        action: AddEditTaskAction.CreateNewEditTask,
        collector: FlowCollector<AddEditTaskAction>
    ) {
        taskRepository.createTask(action.title, action.description)
        collector.emit(
            AddEditTaskAction.LoadedEditTask(AddEditTaskUiMode(isTaskSaved = true))
        )
    }

    private suspend fun handleUpdateTask(
        action: AddEditTaskAction.UpdateEditTask,
        collector: FlowCollector<AddEditTaskAction>
    ) {
        taskRepository.updateTask(
            action.taskId,
            title = action.title,
            description = action.description,
        )
        collector.emit(
            AddEditTaskAction.LoadedEditTask(AddEditTaskUiMode(isTaskSaved = true))
        )
    }

    companion object {
        private const val TAG = "AddEditTaskSideEffect"
    }
}
package com.example.android.architecture.blueprints.todoapp.redux.tasks

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.di.ApplicationScope
import com.example.android.architecture.blueprints.todoapp.di.IoDispatcher
import com.example.android.architecture.blueprints.todoapp.tasks.FilteringUiInfo
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType
import com.example.android.architecture.blueprints.todoapp.tasks.TasksUiState
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withFlowEffect
import com.toggl.komposable.extensions.withoutEffect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class TasksReducer(
    private val taskRepository: TaskRepository,
    @ApplicationScope
    private val scope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
): Reducer<TasksUiState, TasksActions> {
    override fun reduce(
        state: TasksUiState,
        action: TasksActions
    ): ReduceResult<TasksUiState, TasksActions> = when(action){
        is TasksActions.GetTasksActions -> {
            state.copy(
                filter = action.requestType
            ).withFlowEffect(
                TasksSideEffect(
                    action = action,
                    taskRepository = taskRepository,
                    scope = scope,
                    ioDispatcher = ioDispatcher
                ).run()
            )
        }
        is TasksActions.ClearCompletedTasksActions,
        is TasksActions.CompleteTaskActions,
        is TasksActions.ActivateTaskActions -> {
            state.withFlowEffect(
                TasksSideEffect(
                    action = action,
                    taskRepository = taskRepository,
                    scope = scope,
                    ioDispatcher = ioDispatcher
                ).run()
            )
        }

        is TasksActions.Loading -> {
            state.copy(
                isLoading = true
            ).withoutEffect()
        }

        is TasksActions.LoadedTasks -> {
            Timber.tag(TAG).d("TasksActions.LoadedTasks state: $state")
            state.copy(
                items = action.items,
                isLoading = false,
                filteredTodos = filterTasks(action.items, state.filter)
            ).withoutEffect()
        }

        is TasksActions.Error -> {
            TasksUiState(userMessage = action.errorMsg).withoutEffect()
        }

        is TasksActions.TasksFilterAction -> {
            Timber.tag(TAG).d("TasksActions.TasksFilterAction state: $state")
            state.copy(
                filteredTodos = filterTasks(state.items, action.requestType)
            ).withoutEffect()
        }

        is TasksActions.UpdateUserMessaging -> {
            Timber.tag(TAG).d("TasksActions.TasksFilterAction state: $action")
            state.copy(
                userMessage = action.message
            ).withoutEffect()
        }

        else -> {
            state.withoutEffect()
        }
    }

    private fun filterTasks(tasks: List<Task>, filteringType: TasksFilterType): List<Task> {
        val tasksToShow = ArrayList<Task>()
        // We filter the tasks based on the requestType
        for (task in tasks) {
            when (filteringType) {
                TasksFilterType.ALL_TASKS -> tasksToShow.add(task)
                TasksFilterType.ACTIVE_TASKS -> if (task.isActive) {
                    tasksToShow.add(task)
                }
                TasksFilterType.COMPLETED_TASKS -> if (task.isCompleted) {
                    tasksToShow.add(task)
                }
            }
        }
        return tasksToShow
    }

    private fun getFilterUiInfo(requestType: TasksFilterType): FilteringUiInfo =
        when (requestType) {
            TasksFilterType.ALL_TASKS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.logo_no_fill
                )
            }
            TasksFilterType.ACTIVE_TASKS -> {
                FilteringUiInfo(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp
                )
            }
            TasksFilterType.COMPLETED_TASKS -> {
                FilteringUiInfo(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp
                )
            }
        }

    companion object {
        private const val TAG = "TasksReducer"
    }
}
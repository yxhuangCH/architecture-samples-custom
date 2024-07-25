/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.AppStoreManager
import com.example.android.architecture.blueprints.todoapp.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.redux.tasks.TasksActions
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL_TASKS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UiState for the task list screen.
 */
data class TasksUiState(
    val items: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    val userMessage: Int? = null,
    val filter: TasksFilterType = ALL_TASKS,
    val filteredTodos: List<Task> = emptyList()
)

/**
 * ViewModel for the task list screen.
 */
@HiltViewModel
class TasksViewModel @Inject constructor(
    storeManager: AppStoreManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val tasksStore = storeManager.tasksStore()

    private val _uiState =  MutableStateFlow(TasksUiState())
    val uiState = _uiState.asStateFlow()

    init {
        tasksStore.state.onEach {
            handleUiState(it)
        }.launchIn(viewModelScope)

        val savedFilterType = savedStateHandle.get<TasksFilterType>(TASKS_FILTER_SAVED_STATE_KEY) ?: ALL_TASKS
        Timber.tag(TAG).i("savedFilterType $savedFilterType")
        tasksStore.send(TasksActions.GetTasksActions(savedFilterType))
    }

    private fun handleUiState(tasksUiState: TasksUiState){
        Timber.tag(TAG).d("handleUiState tasksUiState: $tasksUiState")
        _uiState.value = tasksUiState
    }

    fun setFiltering(requestType: TasksFilterType) {
        savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = requestType
        tasksStore.send(TasksActions.TasksFilterAction(requestType))
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            tasksStore.send(TasksActions.ClearCompletedTasksActions)
            showSnackbarMessage(R.string.completed_tasks_cleared)
            refresh()
        }
    }

    fun completeTask(task: Task, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            tasksStore.send(TasksActions.CompleteTaskActions(task.id))
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksStore.send(TasksActions.ActivateTaskActions(task.id))
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_task_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_task_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_task_message)
        }
    }

    fun snackbarMessageShown() {
        tasksStore.send(TasksActions.UpdateUserMessaging(null))
    }

    private fun showSnackbarMessage(message: Int) {
        tasksStore.send(TasksActions.UpdateUserMessaging(message))
    }

    fun refresh() {
        viewModelScope.launch {
            tasksStore.send(TasksActions.RefreshActions)
        }
    }
}

// Used to save the current filtering in SavedStateHandle.
const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"
const val TAG = "TasksViewModel"

data class FilteringUiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noTasksLabel: Int = R.string.no_tasks_all,
    val noTaskIconRes: Int = R.drawable.logo_no_fill,
)

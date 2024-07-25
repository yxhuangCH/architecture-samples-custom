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

package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.AppStoreManager
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.di.IoDispatcher
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UiState for the Add/Edit screen
 */
data class AddEditTaskUiState(
    val title: String = "",
    val description: String = "",
    val isTaskCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isTaskSaved: Boolean = false
)

/**
 * ViewModel for the Add/Edit screen.
 */
@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    storeManager: AppStoreManager
) : ViewModel() {

    private val addEditStore = storeManager.addEditTaskStore()
    private val taskId: String? = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]

    // A MutableStateFlow needs to be created in this ViewModel. The source of truth of the current
    // editable Task is the ViewModel, we need to mutate the UI state directly in methods such as
    // `updateTitle` or `updateDescription`
    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init {
        addEditStore.state.onEach {
            Timber.tag(TAG).d("appStore.state $it")
            _uiState.emit(it)
        }.launchIn(viewModelScope)

        if (taskId != null) {
            loadTask(taskId)
        }
    }

    // Called when clicking on fab.
    fun saveTask() {
        if (uiState.value.title.isEmpty() || uiState.value.description.isEmpty()) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_task_message)
            }
            return
        }

        if (taskId == null) {
            createNewTask()
        } else {
            updateTask()
        }
    }

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null)
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update {
            it.copy(title = newTitle)
        }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update {
            it.copy(description = newDescription)
        }
    }

    private fun createNewTask() = viewModelScope.launch {
        Timber.tag(TAG).d("createNewTask Thread: ${Thread.currentThread().name}")
        addEditStore.send(AddEditTaskAction.CreateNewEditTask(
            title = uiState.value.title,
            description = uiState.value.description)
        )
    }

    private fun updateTask() {
        if (taskId == null) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        viewModelScope.launch(dispatcher) {
            addEditStore.send(
                AddEditTaskAction.UpdateEditTask(
                    taskId = taskId,
                    title = uiState.value.title,
                    description = uiState.value.description
                )
            )
        }
    }

    private fun loadTask(taskId: String) {
        Timber.tag(TAG).d("AddEditTaskAction.LoadEditTask taskId $taskId")
        addEditStore.send(AddEditTaskAction.LoadEditTask(taskId))
    }

    companion object {
        private const val TAG = "AddEditTaskViewModel"
    }
}

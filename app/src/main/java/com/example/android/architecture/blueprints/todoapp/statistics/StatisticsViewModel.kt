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

package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.AppStoreManager
import com.example.android.architecture.blueprints.todoapp.redux.statistics.StatisticsAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UiState for the statistics screen.
 */
data class StatisticsUiState(
    val isEmpty: Boolean = false,
    val isLoading: Boolean = false,
    val activeTasksPercent: Float = 0f,
    val completedTasksPercent: Float = 0f
)

/**
 * ViewModel for the statistics screen.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    storeManager: AppStoreManager
) : ViewModel() {
    private val statisticsStore = storeManager.statisticsStore()

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    init {
        statisticsStore.state.onEach {
            Timber.tag(TAG).d("MutableStateFlowStore StatisticsViewModel statisticsStore.state data: $it")
            _uiState.emit(it)
        }.launchIn(viewModelScope)

        Timber.tag(TAG).d("MutableStateFlowStore StatisticsViewModel StatisticsAction.GetTasksAction")
        statisticsStore.send(StatisticsAction.GetTasksAction)
    }

    fun refresh() {
        viewModelScope.launch {
            statisticsStore.send(StatisticsAction.RefreshAction)
        }
    }

    companion object {
        private const val TAG = "StatisticsViewModel"
    }
}

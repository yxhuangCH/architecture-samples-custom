package com.example.android.architecture.blueprints.todoapp.redux.addedittask

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiMode

sealed class AddEditTaskAction {

    data class FetchTask(val taskId: String) : AddEditTaskAction()

    data class LoadedEditTask(
        val addEditTaskUiMode: AddEditTaskUiMode
    ) : AddEditTaskAction()

    data object Loading : AddEditTaskAction()

    data class CreateNewEditTask(
        val title: String,
        val description: String
    ) : AddEditTaskAction()

    data class UpdateEditTask(
        val taskId: String,
        val title: String,
        val description: String
    ) : AddEditTaskAction()
}

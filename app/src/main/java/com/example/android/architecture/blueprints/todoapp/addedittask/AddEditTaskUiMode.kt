package com.example.android.architecture.blueprints.todoapp.addedittask

data class AddEditTaskUiMode(
    val title: String = "",
    val description: String = "",
    val isTaskCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val isTaskSaved: Boolean = false
)

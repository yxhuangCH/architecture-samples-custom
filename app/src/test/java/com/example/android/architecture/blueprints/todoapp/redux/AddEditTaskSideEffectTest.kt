package com.example.android.architecture.blueprints.todoapp.redux

import app.cash.turbine.turbineScope
import com.example.android.architecture.blueprints.todoapp.data.FakeTaskRepository
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskAction
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskSideEffect
import com.google.common.truth.Truth
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddEditTaskSideEffectTest {

    private lateinit var tasksRepository: FakeTaskRepository

    private val taskId = "12345"
    private val title1 = "Title1"
    private val description1 = "Description1"
    private val task = Task(title = title1, description = description1, id = taskId)

    @Before
    fun setup() {
        tasksRepository = FakeTaskRepository()

    }

    @Test
    fun `LoadEditTask get task is no null`() = runTest {
        tasksRepository.addTasks(task)

        val sideEffect = AddEditTaskSideEffect(
            AddEditTaskAction.FetchTask(taskId),
            tasksRepository
        )

        turbineScope {
            val effectTurbine = sideEffect.run().testIn(backgroundScope)

            val result1 = effectTurbine.awaitItem()
            println("LoadEditTask get task is no null result1: $result1")
            Truth.assertThat(result1).isEqualTo(AddEditTaskAction.Loading)

            val action = effectTurbine.awaitItem() as AddEditTaskAction.LoadedEditTask
            println("LoadEditTask get task is no null action: $action")
            Truth.assertThat(action.addEditTaskUiMode.title).isEqualTo(title1)
            Truth.assertThat(action.addEditTaskUiMode.description).isEqualTo(description1)
        }
    }
}
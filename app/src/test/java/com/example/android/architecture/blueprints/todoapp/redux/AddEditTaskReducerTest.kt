package com.example.android.architecture.blueprints.todoapp.redux

import app.cash.turbine.test
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiMode
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiState
import com.example.android.architecture.blueprints.todoapp.data.FakeTaskRepository
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskAction
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskReducer
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.test.testReduce
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class AddEditTaskReducerTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var reducer: AddEditTaskReducer

    private lateinit var tasksRepository: FakeTaskRepository

    private val initialState = AddEditTaskUiState()


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        tasksRepository = FakeTaskRepository()
        reducer = AddEditTaskReducer(tasksRepository)
    }

    @After
    fun down() = runTest {
        tasksRepository.deleteAllTasks()
    }

    @Test
    fun `loading`() = runTest {
        reducer.testReduce(
            initialState = initialState,
            action = AddEditTaskAction.Loading
        ) { state, effect ->
            println("AddEditTaskAction Loading state: $state \neffect: $effect")
            state shouldBe initialState.copy(isLoading = true)
            effect shouldBe NoEffect
        }
    }

    @Test
    fun `AddEditTaskAction LoadedEditTask`() = runTest {
        val addEditTaskUiMode = AddEditTaskUiMode(
            title = "title1",
            description = "description1",
            isTaskCompleted = true,
            isLoading = false,
            isTaskSaved = true
        )

        reducer.testReduce(
            initialState =  initialState,
            action = AddEditTaskAction.LoadedEditTask(addEditTaskUiMode)
        ) { state, effect ->
            println("AddEditTaskAction LoadedEditTask state: $state \neffect: $effect")
            state shouldBe initialState.copy(
                title = addEditTaskUiMode.title,
                description = addEditTaskUiMode.description,
                isTaskCompleted = addEditTaskUiMode.isTaskCompleted,
                isLoading = addEditTaskUiMode.isLoading,
                userMessage = null,
                isTaskSaved = addEditTaskUiMode.isTaskSaved
            )
            effect shouldBe NoEffect
        }
    }


    @Test
    fun `AddEditTaskAction FetchTask return no empty AddEditTaskUiMode`() = runTest {
        tasksRepository.addTasks(
            Task(
                title = "title1",
                description = "description1",
                id = TASK_ID,
                isCompleted = true
            )
        )
        reducer.testReduce(
            initialState = initialState,
            action = AddEditTaskAction.FetchTask(TASK_ID)
        ) { state, effect ->
            println("AddEditTaskAction FetchTask state: $state \neffect: $effect")
            state shouldBe initialState
            effect.run().test {
                val item1 = awaitItem()
                println("item1 $item1")

                item1 shouldBe AddEditTaskAction.Loading

                val item2 = awaitItem()
                println("item2 $item2")

                item2 shouldBe AddEditTaskAction.LoadedEditTask(
                    AddEditTaskUiMode(
                        title = "title1",
                        description = "description1",
                        isTaskCompleted = true,
                        isLoading = false
                    )
                )
                awaitComplete()
            }
        }
    }


    @Test
    fun `AddEditTaskAction FetchTask return empty AddEditTaskUiMode`() = runTest {
        reducer.testReduce(
            initialState = initialState,
            action = AddEditTaskAction.FetchTask(TASK_ID)
        ) { state, effect ->
            println("AddEditTaskAction FetchTask state: $state \neffect: $effect")
            state shouldBe initialState
            effect.run().test {
                val item1 = awaitItem()
                println("item1 $item1")

                item1 shouldBe AddEditTaskAction.Loading

                val item2 = awaitItem()
                println("item2 $item2")

                item2 shouldBe AddEditTaskAction.LoadedEditTask(
                    AddEditTaskUiMode(
                        isLoading = false
                    )
                )
                awaitComplete()
            }
        }
    }


    @Test
    fun `AddEditTaskAction UpdateEditTask`() = runTest(timeout = 1000.seconds) {
        tasksRepository.addTasks(
            Task(
                title = "title1",
                description = "description1",
                id = TASK_ID,
                isCompleted = true
            )
        )

        reducer.testReduce(
            initialState = initialState,
            action = AddEditTaskAction.UpdateEditTask(
                taskId = TASK_ID,
                title = "title1",
                description = "description1",
            )
        ) { state, effect ->
            state shouldBe initialState
            effect.run().test {
                val item1 = awaitItem()
                println("item1 $item1")

                item1 shouldBe AddEditTaskAction.LoadedEditTask(AddEditTaskUiMode(isTaskSaved = true))

                awaitComplete()
            }
        }
    }

    companion object {
        private const val TASK_ID = "123456"
    }
}

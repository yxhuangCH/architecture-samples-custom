package com.example.android.architecture.blueprints.todoapp.di

import com.example.android.architecture.blueprints.todoapp.AppAction
import com.example.android.architecture.blueprints.todoapp.AppState
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskReducer
import com.example.android.architecture.blueprints.todoapp.redux.statistics.StatisticsReducer
import com.example.android.architecture.blueprints.todoapp.redux.taskdetail.TaskDetailReducer
import com.example.android.architecture.blueprints.todoapp.redux.tasks.TasksReducer
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.architecture.Store
import com.toggl.komposable.extensions.combine
import com.toggl.komposable.extensions.createStore
import com.toggl.komposable.extensions.pullback
import com.toggl.komposable.scope.DispatcherProvider
import com.toggl.komposable.scope.StoreScopeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
object KomposableModule {

    @Singleton
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider = DispatcherProvider(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
    )

    @Singleton
    @Provides
    fun provideCoroutineScope(
        dispatcherProvider: DispatcherProvider
    ): CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = dispatcherProvider.main
    }

    @Singleton
    @Provides
    fun provideStoreScopeProvider(
        coroutineScope: CoroutineScope
    ): StoreScopeProvider = StoreScopeProvider { coroutineScope }

    @Singleton
    @Provides
    fun provideStore(
        appReducer: Reducer<AppState, AppAction>,
        storeScopeProvider: StoreScopeProvider,
        dispatcherProvider: DispatcherProvider
    ): Store<AppState, AppAction> = createStore(
        initialState = AppState(),
        reducer = appReducer,
        storeScopeProvider = storeScopeProvider,
        dispatcherProvider = dispatcherProvider,
    )

    @Singleton
    @Provides
    fun provideAppReducer(
        addEditTaskReducer: AddEditTaskReducer,
        statisticsReducer: StatisticsReducer,
        taskDetailReducer: TaskDetailReducer,
        tasksReducer: TasksReducer
    ): Reducer<AppState, AppAction> {
        return combine(
            addEditTaskReducer.pullback(
                mapToLocalState = { it.addEditTaskUiState},
                mapToLocalAction = { (it as? AppAction.AddEditTask)?.actions},
                mapToGlobalState = { globalState, addEditTaskUiState ->
                    globalState.copy(addEditTaskUiState = addEditTaskUiState)
                },
                mapToGlobalAction = { AppAction.AddEditTask(it) }
            ),
            statisticsReducer.pullback(
                mapToLocalState = { it.statisticsUiState},
                mapToLocalAction = { (it as? AppAction.Statistics)?.actions},
                mapToGlobalState = { globalState, statisticsUiState ->
                    globalState.copy(statisticsUiState = statisticsUiState)
                },
                mapToGlobalAction = { AppAction.Statistics(it) }
            ),
            taskDetailReducer.pullback(
                mapToLocalState = { it.taskDetailUiState},
                mapToLocalAction = { (it as? AppAction.TaskDetail)?.actions},
                mapToGlobalState = { globalState, taskDetailUiState ->
                    globalState.copy(taskDetailUiState = taskDetailUiState)
                },
                mapToGlobalAction = { AppAction.TaskDetail(it) }
            ),
            tasksReducer.pullback(
                mapToLocalState = { it.tasksUiState},
                mapToLocalAction = { (it as? AppAction.TasksAction)?.actions},
                mapToGlobalState = { globalState, tasksUiState ->
                    globalState.copy(tasksUiState = tasksUiState)
                },
                mapToGlobalAction = { AppAction.TasksAction(it) }
            )
        )
    }

    @Singleton
    @Provides
    fun provideAddEditTaskReducer(
        taskRepository: TaskRepository
    ): AddEditTaskReducer = AddEditTaskReducer(
        taskRepository = taskRepository
    )

    @Singleton
    @Provides
    fun provideStatisticsReducer(
        taskRepository: TaskRepository,
        @ApplicationScope
        scope: CoroutineScope,
        @IoDispatcher
        ioDispatcher: CoroutineDispatcher
    ): StatisticsReducer = StatisticsReducer(
        taskRepository = taskRepository,
        scope = scope,
        ioDispatcher = ioDispatcher
    )

    @Singleton
    @Provides
    fun provideTaskDetailReducer(
        taskRepository: TaskRepository,
        @ApplicationScope
        scope: CoroutineScope,
        @IoDispatcher
        ioDispatcher: CoroutineDispatcher
    ): TaskDetailReducer = TaskDetailReducer(
        taskRepository = taskRepository,
        scope = scope,
        ioDispatcher = ioDispatcher
    )

    @Singleton
    @Provides
    fun provideTasksReducer(
        taskRepository: TaskRepository,
        @ApplicationScope
        scope: CoroutineScope,
        @IoDispatcher
        ioDispatcher: CoroutineDispatcher
    ): TasksReducer = TasksReducer(
        taskRepository = taskRepository,
        scope = scope,
        ioDispatcher = ioDispatcher
    )
}

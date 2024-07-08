package com.example.android.architecture.blueprints.todoapp.di

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskUiState
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskAction
import com.example.android.architecture.blueprints.todoapp.redux.addedittask.AddEditTaskReducer
import com.toggl.komposable.architecture.Store
import com.toggl.komposable.extensions.createStore
import com.toggl.komposable.scope.DispatcherProvider
import com.toggl.komposable.scope.StoreScopeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
        reducer: AddEditTaskReducer,
        storeScopeProvider: StoreScopeProvider,
        dispatcherProvider: DispatcherProvider
    ): Store<AddEditTaskUiState, AddEditTaskAction> = createStore(
        initialState = AddEditTaskUiState(),
        reducer = reducer,
        storeScopeProvider = storeScopeProvider,
        dispatcherProvider = dispatcherProvider,
    )

    @Singleton
    @Provides
    fun provideReducer(
        taskRepository: TaskRepository
    ): AddEditTaskReducer = AddEditTaskReducer(
        taskRepository = taskRepository
    )
}
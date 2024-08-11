package com.toggl.komposable.internal

import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.map

internal class PullbackReducer<LocalState, GlobalState, LocalAction, GlobalAction>(
    private val innerReducer: Reducer<LocalState, LocalAction>,
    private val mapToLocalState: (GlobalState) -> LocalState,
    private val mapToLocalAction: (GlobalAction) -> LocalAction?,
    private val mapToGlobalState: (GlobalState, LocalState) -> GlobalState,
    private val mapToGlobalAction: (LocalAction) -> GlobalAction,
) : Reducer<GlobalState, GlobalAction> {
    override fun reduce(
        state: GlobalState,
        action: GlobalAction,
    ): ReduceResult<GlobalState, GlobalAction> {
        println("MutableStateFlowStore PullbackReducer mapToLocalAction origin action: $action mapToLocalAction: $mapToLocalAction")
        val tempAction = mapToLocalAction(action)
        println("MutableStateFlowStore PullbackReducer mapToLocalAction tempAction: $tempAction")
        val localAction = tempAction ?: return ReduceResult(state, NoEffect)
        println("MutableStateFlowStore PullbackReducer mapToLocalAction localAction action: $localAction")

        println("MutableStateFlowStore PullbackReducer mapToLocalState origin state: $state")
        val localState = mapToLocalState(state)
        println("MutableStateFlowStore PullbackReducer mapToLocalState localState: $localState")
        val localResult = innerReducer.reduce(localState, localAction) // innerReducer is divisional reducer

        println("MutableStateFlowStore PullbackReducer localResult : $localResult")

        // app state, app action
        return ReduceResult(
            mapToGlobalState(state, localResult.state),
            localResult.effect.map(mapToGlobalAction),
        )
    }
}

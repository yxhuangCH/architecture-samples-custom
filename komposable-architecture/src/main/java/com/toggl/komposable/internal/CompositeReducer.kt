package com.toggl.komposable.internal

import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.mergeWith

internal class CompositeReducer<State, Action>(private val reducers: List<Reducer<State, Action>>) :
    Reducer<State, Action> {
    override fun reduce(state: State, action: Action): ReduceResult<State, Action> =
        // reducers is CompositeReducer
        reducers.fold(ReduceResult(state, NoEffect)) { accResult, reducer ->
            // reducer is PullbackReducer
            println("MutableStateFlowStore CompositeReducer this: ${this.hashCode()} accResult: $accResult")
            val result = reducer.reduce(accResult.state, action)
            println("MutableStateFlowStore CompositeReducer this: ${this.hashCode()} result: $result")
            ReduceResult(result.state, accResult.effect mergeWith result.effect)
        }
}

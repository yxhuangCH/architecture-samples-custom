package com.toggl.komposable.internal

import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.map
import com.toggl.komposable.extensions.mergeWith

internal class ForEachReducer<ElementState, ParentState, ElementAction, ParentAction, ID>(
    private val parentReducer: Reducer<ParentState, ParentAction>,
    private val elementReducer: Reducer<ElementState, ElementAction>,
    private val mapToElementAction: (ParentAction) -> Pair<ID, ElementAction>?,
    private val mapToElementState: (ParentState, ID) -> ElementState,
    private val mapToParentAction: (ElementAction, ID) -> ParentAction,
    private val mapToParentState: (ParentState, ElementState, ID) -> ParentState,
) : Reducer<ParentState, ParentAction> {
    override fun reduce(
        state: ParentState,
        action: ParentAction,
    ): ReduceResult<ParentState, ParentAction> {
        println("ForEachReducer reduce ParentState $state ParentAction: $action")
        val (id: ID, elementAction: ElementAction) = mapToElementAction(action) ?: return parentReducer.reduce(state, action)

        println("ForEachReducer reduce after mapToElementAction id: $id elementAction: $elementAction")


        val mapToLocalState: (ParentState) -> ElementState = { parentState: ParentState ->
            mapToElementState(parentState, id)
        }

        val mapToGlobalState: (ParentState, ElementState) -> ParentState = { parentState: ParentState, elementState: ElementState ->
            mapToParentState(parentState, elementState!!, id)
        }

        val (elementState, elementEffect) = elementReducer.reduce(mapToLocalState(state), elementAction)

        println("ForEachReducer elementReducer.reduce elementState  $elementState elementEffect: $elementEffect")

        val (parentState, parentEffect) = parentReducer.reduce(mapToGlobalState(state, elementState), action)

        println("ForEachReducer parentReducer.reduce parentState: $parentState parentEffect: $parentEffect")

        return ReduceResult(
            state = parentState,
            effect = elementEffect.map { mapToParentAction(it, id) } mergeWith parentEffect,
        )
    }

    companion object {
        private const val TAG = "ForEachReducer"
    }

}

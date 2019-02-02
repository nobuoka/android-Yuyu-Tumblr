package info.vividcode.android.app.yuyutumblr.usecase

class NextPageLoaderStateHolder(private var internalState: State) {

    private val changeEventListeners: MutableList<(ChangeEvent) -> Unit> = mutableListOf()

    val state: State get() = internalState

    sealed class State {
        object NoNextPage : State()
        object Idle : State()
        object Progress : State()
        class Error(val message: String) : State()
    }

    fun updateState(state: State) {
        val previousState = this.internalState
        this.internalState = state
        changeEventListeners.forEach {
            it(ChangeEvent.Update(previousState, state))
        }
    }

    fun addChangeEventListener(listener: (ChangeEvent) -> Unit) {
        changeEventListeners.add(listener)
    }

    fun removeChangeEventListener(listener: (ChangeEvent) -> Unit) {
        changeEventListeners.removeAll { it === listener }
    }

    sealed class ChangeEvent {
        data class Update(val previousState: State, val currentState: State) : ChangeEvent()
    }

}

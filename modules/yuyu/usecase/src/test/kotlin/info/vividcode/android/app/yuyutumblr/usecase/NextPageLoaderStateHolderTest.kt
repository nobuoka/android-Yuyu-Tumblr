package info.vividcode.android.app.yuyutumblr.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class NextPageLoaderStateHolderTest {

    @Test
    internal fun constructor() {
        val stateHolder = NextPageLoaderStateHolder(NextPageLoaderStateHolder.State.Idle)

        Assertions.assertEquals(NextPageLoaderStateHolder.State.Idle, stateHolder.state)
    }

    @Test
    internal fun updateState_simple() {
        val stateHolder = NextPageLoaderStateHolder(NextPageLoaderStateHolder.State.Idle)

        stateHolder.updateState(NextPageLoaderStateHolder.State.Progress)

        Assertions.assertEquals(NextPageLoaderStateHolder.State.Progress, stateHolder.state)
    }

    @Test
    internal fun updateState_listeners_eventNotified() {
        val stateHolder = NextPageLoaderStateHolder(NextPageLoaderStateHolder.State.Idle)
        val listener1 = mockk<(NextPageLoaderStateHolder.ChangeEvent) -> Unit>()
        val listener2 = mockk<(NextPageLoaderStateHolder.ChangeEvent) -> Unit>()
        listOf(listener1, listener2).forEach { listener ->
            every { listener.invoke(any()) } returns Unit
            stateHolder.addChangeEventListener(listener)
        }

        stateHolder.updateState(NextPageLoaderStateHolder.State.Progress)

        val expectedEvent = NextPageLoaderStateHolder.ChangeEvent.Update(
                previousState = NextPageLoaderStateHolder.State.Idle,
                currentState = NextPageLoaderStateHolder.State.Progress
        )
        listOf(listener1, listener2).forEach { listener ->
            val actualEvents = mutableListOf<NextPageLoaderStateHolder.ChangeEvent>()
            verify(exactly = 1) { listener.invoke(capture(actualEvents)) }
            Assertions.assertEquals(listOf(expectedEvent), actualEvents)
        }
    }

    @Test
    internal fun updateState_listeners_removedListener() {
        val stateHolder = NextPageLoaderStateHolder(NextPageLoaderStateHolder.State.Idle)
        val listener1 = mockk<(NextPageLoaderStateHolder.ChangeEvent) -> Unit>()
        val listener2 = mockk<(NextPageLoaderStateHolder.ChangeEvent) -> Unit>()
        listOf(listener1, listener2).forEach { listener ->
            every { listener.invoke(any()) } returns Unit
            stateHolder.addChangeEventListener(listener)
        }

        stateHolder.removeChangeEventListener(listener2)

        stateHolder.updateState(NextPageLoaderStateHolder.State.Progress)

        val expectedEvent = NextPageLoaderStateHolder.ChangeEvent.Update(
                previousState = NextPageLoaderStateHolder.State.Idle,
                currentState = NextPageLoaderStateHolder.State.Progress
        )
        listener1.let { listener ->
            val actualEvents = mutableListOf<NextPageLoaderStateHolder.ChangeEvent>()
            verify(exactly = 1) { listener.invoke(capture(actualEvents)) }
            Assertions.assertEquals(listOf(expectedEvent), actualEvents)
        }
        listener2.let { listener ->
            val actualEvents = mutableListOf<NextPageLoaderStateHolder.ChangeEvent>()
            verify(exactly = 0) { listener.invoke(capture(actualEvents)) }
        }
    }

}

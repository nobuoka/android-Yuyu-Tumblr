package info.vividcode.android.app.yuyu.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SubjectTest {

    private class TestObserver : Observer<String> {
        val received: MutableList<String> = mutableListOf()

        override fun invoke(value: String) {
            received.add(value)
        }
    }

    @Test
    internal fun invoke_observed() {
        val subject = Subject<String>()
        val observer = TestObserver()
        subject.setObserver(observer)

        // Act
        subject.invoke("test-1")

        // Assert
        assertEquals(listOf("test-1"), observer.received)
    }

    @Test
    internal fun setObserver_afterInvoke() {
        val subject = Subject<String>()
        val observer = TestObserver()
        subject.invoke("test-1")

        // Act
        subject.setObserver(observer)

        // Assert
        assertEquals(listOf("test-1"), observer.received)
    }

    @Test
    internal fun setObserver_onlyOneObserverIsSet() {
        val subject = Subject<String>()
        val observer1 = TestObserver()
        val observer2 = TestObserver()

        // Act
        subject.setObserver(observer1)
        subject.setObserver(observer2)

        // Assert
        subject.invoke("test-1")
        assertEquals(listOf("test-1"), observer2.received)
        assertEquals(emptyList<String>(), observer1.received)
    }

    @Test
    internal fun unsetObserver() {
        val subject = Subject<String>()
        val observer = TestObserver()
        subject.setObserver(observer)

        // Act
        subject.unsetObserver()

        // Assert
        subject.invoke("test-1")
        assertEquals(emptyList<String>(), observer.received)
    }

}

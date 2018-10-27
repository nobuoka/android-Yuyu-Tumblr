package info.vividcode.android.app.yuyutumblr.usecase

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.json.JSONObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

internal class MainApplicationTest {

    private val mockMainView = mockk<MainView>()
    private val mockTumblrApi = mockk<TumblrApi>()

    @Test
    internal fun init() {
        val mainApplication = MainApplication(mockMainView, mockTumblrApi)

        every { mockMainView.setRefreshEventListener(any()) } returns Unit

        mainApplication.init()

        verify(exactly = 1) { mockMainView.setRefreshEventListener(any()) }
    }

    @Nested
    internal inner class UpdatePosts {
        @Test
        internal fun resultSuccess() {
            val mainApplication = MainApplication(mockMainView, mockTumblrApi)

            every { mockMainView.getLatestPost() } returns null
            every { mockMainView.stopRefreshingIndicator() } returns Unit
            every { mockMainView.addPosts(any()) } returns Unit

            val lambdaSlot = CapturingSlot<(TumblrApi.Result<JSONObject>) -> Unit>()
            every { mockTumblrApi.fetchPosts(any(), capture(lambdaSlot)) } answers {
                lambdaSlot.captured(TumblrApi.Result.Success(JSONObject(
                        """{ "response": [] }"""
                )))
            }

            mainApplication.updatePosts()

            verify(exactly = 1) { mockMainView.getLatestPost() }
            verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockMainView.addPosts(emptyList()) }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(any(), any()) }
        }

        @Test
        internal fun resultFailure() {
            val mainApplication = MainApplication(mockMainView, mockTumblrApi)

            every { mockMainView.getLatestPost() } returns null
            every { mockMainView.stopRefreshingIndicator() } returns Unit

            val lambdaSlot = CapturingSlot<(TumblrApi.Result<JSONObject>) -> Unit>()
            every { mockTumblrApi.fetchPosts(any(), capture(lambdaSlot)) } answers {
                lambdaSlot.captured(TumblrApi.Result.Failure(RuntimeException("Test exception")))
            }

            mainApplication.updatePosts()

            verify(exactly = 1) { mockMainView.getLatestPost() }
            verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(any(), any()) }
        }
    }

}

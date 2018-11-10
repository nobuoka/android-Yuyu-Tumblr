package info.vividcode.android.app.yuyutumblr.usecase

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MainApplicationTest {

    private val mockMainView = mockk<MainView>()
    private val mockTumblrApi = mockk<TumblrApi>()

    init {
        every { mockMainView.setRefreshEventListener(any()) } returns Unit
        every { mockMainView.bindMainApplication(any()) } returns Unit
        every { mockMainView.stopRefreshingIndicator() } returns Unit
    }

    @Test
    internal fun init() {
        val mainApplication = MainApplication(mockMainView, mockTumblrApi)

        mainApplication.init()

        verify(exactly = 1) { mockMainView.setRefreshEventListener(any()) }
        verify(exactly = 1) { mockMainView.bindMainApplication(any()) }
    }

    @Nested
    internal inner class UpdatePosts {
        @Test
        internal fun resultSuccess_noPhoto() {
            val mainApplication = MainApplication(mockMainView, mockTumblrApi)
            mainApplication.init()

            val lambdaSlot = CapturingSlot<(TumblrApi.Result<JSONObject>) -> Unit>()
            every { mockTumblrApi.fetchPosts(any(), capture(lambdaSlot)) } answers {
                lambdaSlot.captured(TumblrApi.Result.Success(JSONObject(
                        """{ "response": [] }"""
                )))
            }

            mainApplication.updatePosts()

            verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(isNull(), any()) }

            // Second request also should fetch latest posts, because response of first request has no posts.
            mainApplication.updatePosts()

            verify(exactly = 2) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 2) { mockTumblrApi.fetchPosts(isNull(), any()) }
        }

        @Test
        internal fun resultSuccess_photoExists() {
            val mainApplication = MainApplication(mockMainView, mockTumblrApi)
            mainApplication.init()

            val lambdaSlot = CapturingSlot<(TumblrApi.Result<JSONObject>) -> Unit>()
            every { mockTumblrApi.fetchPosts(any(), capture(lambdaSlot)) } answers {
                lambdaSlot.captured(TumblrApi.Result.Success(JSONObject(
                        """{ "response": [
                            |  {
                            |    "timestamp": 1370531100,
                            |    "photos": [
                            |      {
                            |        "caption": "",
                            |        "alt_sizes": [
                            |          {"width":400,"height":600,"url":"http://example.com/af8c96/tumblr_mnz8le_400.jpg"},
                            |          {"width":250,"height":375,"url":"http://example.com/af8c96/tumblr_mnz8le_250.jpg"}
                            |        ],
                            |        "original_size": {"width":400,"height":600,"url":"http://example.com/af8c96/tumblr_mnz8le_400.jpg"}
                            |      }
                            |    ]
                            |  }
                            |] }""".trimMargin()
                )))
            }

            mainApplication.updatePosts()

            verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(isNull(), any()) }

            // Second request should fetch next page, because response of first request have a post.
            mainApplication.updatePosts()

            verify(exactly = 2) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(eq(1370531100), any()) }
        }

        @Test
        internal fun resultFailure() {
            val mainApplication = MainApplication(mockMainView, mockTumblrApi)
            mainApplication.init()

            val lambdaSlot = CapturingSlot<(TumblrApi.Result<JSONObject>) -> Unit>()
            every { mockTumblrApi.fetchPosts(any(), capture(lambdaSlot)) } answers {
                lambdaSlot.captured(TumblrApi.Result.Failure(RuntimeException("Test exception")))
            }

            mainApplication.updatePosts()

            verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(isNull(), any()) }
        }
    }

    @Nested
    internal inner class GetAppropriateSizePhotoObjectMethodTest {
        @Test
        internal fun normal_targetSizePosition_top() {
            val json = createTestPhotoJsonWithTargetSizeTop()

            val photo = MainApplication.getAppropriateSizePhotoObject(json)

            val expected = Photo(400, 600, "http://example.com/af8c96/tumblr_mnz8le_400.jpg")
            Assertions.assertEquals(expected, photo)
        }

        @Test
        internal fun normal_targetSizePosition_middle() {
            val json = createTestPhotoJsonWithTargetSizeMiddle()

            val photo = MainApplication.getAppropriateSizePhotoObject(json)

            val expected = Photo(400, 600, "http://example.com/af8c96/tumblr_mnz8le_400.jpg")
            Assertions.assertEquals(expected, photo)
        }

        @Test
        internal fun invalidJson() {
            val json = JSONObject("{}")

            val photo = MainApplication.getAppropriateSizePhotoObject(json)

            Assertions.assertNull(photo)
        }
    }

    companion object {
        fun createTestPhotoJsonWithTargetSizeTop() = JSONObject("""
            {
              "caption": "",
              "alt_sizes": [
                {"width":400,"height":600,"url":"http://example.com/af8c96/tumblr_mnz8le_400.jpg"},
                {"width":250,"height":375,"url":"http://example.com/af8c96/tumblr_mnz8le_250.jpg"},
                {"width":100,"height":150,"url":"http://example.com/af8c96/tumblr_mnz8le_100.jpg"},
                {"width":75,"height":75,"url":"http://example.com/af8c96/tumblr_mnz8le_75.jpg"}
              ],
              "original_size": {"width":400,"height":600,"url":"http://example.com/af8c96/tumblr_mnz8le_400.jpg"}
            }
        """.trimIndent())

        fun createTestPhotoJsonWithTargetSizeMiddle() = JSONObject("""
            {
              "caption": "",
              "alt_sizes": [
                {"width":250,"height":375,"url":"http://example.com/af8c96/tumblr_mnz8le_250.jpg"},
                {"width":100,"height":150,"url":"http://example.com/af8c96/tumblr_mnz8le_100.jpg"},
                {"width":400,"height":600,"url":"http://example.com/af8c96/tumblr_mnz8le_400.jpg"},
                {"width":75,"height":75,"url":"http://example.com/af8c96/tumblr_mnz8le_75.jpg"}
              ],
              "original_size": {"width":400,"height":600,"url":"http://example.com/af8c96/tumblr_mnz8le_400.jpg"}
            }
        """.trimIndent())
    }

}

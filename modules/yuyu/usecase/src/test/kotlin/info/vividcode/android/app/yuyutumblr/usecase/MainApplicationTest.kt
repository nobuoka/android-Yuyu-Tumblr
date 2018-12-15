package info.vividcode.android.app.yuyutumblr.usecase

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

            val lambdaSlot = CapturingSlot<(TumblrApi.Result<List<TumblrPost>>) -> Unit>()
            every { mockTumblrApi.fetchPosts(any(), capture(lambdaSlot)) } answers {
                lambdaSlot.captured(TumblrApi.Result.Success(emptyList()))
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

            val lambdaSlot = CapturingSlot<(TumblrApi.Result<List<TumblrPost>>) -> Unit>()
            every { mockTumblrApi.fetchPosts(any(), capture(lambdaSlot)) } answers {
                lambdaSlot.captured(TumblrApi.Result.Success(listOf(
                        TumblrPost.Photo(
                                timestamp = 1370531100,
                                photos = listOf(
                                        TumblrPhotoInfo(listOf(
                                                Photo(width = 400, height = 600, url = "http://example.com/af8c96/tumblr_mnz8le_400.jpg"),
                                                Photo(width = 250, height = 375, url = "http://example.com/af8c96/tumblr_mnz8le_250.jpg")
                                        ))
                                )
                        )
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

            val lambdaSlot = CapturingSlot<(TumblrApi.Result<List<TumblrPost>>) -> Unit>()
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
        internal fun normal_similarSizes() {
            val photoInfo = TumblrPhotoInfo(listOf(
                    Photo(width = 399, height = 399, url = "http://example.com/af8c96/tumblr_mnz8le_399-2.jpg"),
                    Photo(width = 401, height = 400, url = "http://example.com/af8c96/tumblr_mnz8le_401-1.jpg"),
                    Photo(width = 399, height = 399, url = "http://example.com/af8c96/tumblr_mnz8le_399-1.jpg"),
                    Photo(width = 401, height = 401, url = "http://example.com/af8c96/tumblr_mnz8le_401-2.jpg")
            ))
            val expected = Photo(width = 399, height = 399, url = "http://example.com/af8c96/tumblr_mnz8le_399-1.jpg")

            val photo = MainApplication.getAppropriateSizePhotoObject(photoInfo)

            Assertions.assertEquals(expected, photo)
        }

        @Test
        internal fun invalidJson() {
            val photoInfo = TumblrPhotoInfo(emptyList())

            val photo = MainApplication.getAppropriateSizePhotoObject(photoInfo)

            Assertions.assertNull(photo)
        }
    }

}

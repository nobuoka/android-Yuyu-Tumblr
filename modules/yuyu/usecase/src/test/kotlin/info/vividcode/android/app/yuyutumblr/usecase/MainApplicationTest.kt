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
        internal fun normal_smallSizes_targetSizePosition_top() {
            val photoInfo = TumblrPhotoInfo(createTestPhotoListSmallSizesWithTargetSizeTop())

            val photo = MainApplication.getAppropriateSizePhotoObject(photoInfo)

            val expected = Photo(400, 600, "http://example.com/af8c96/tumblr_mnz8le_400.jpg")
            Assertions.assertEquals(expected, photo)
        }

        @Test
        internal fun normal_smallSizes_targetSizePosition_middle() {
            val photoInfo = TumblrPhotoInfo(createTestPhotoListSmallSizesWithTargetSizeMiddle())

            val photo = MainApplication.getAppropriateSizePhotoObject(photoInfo)

            val expected = Photo(400, 600, "http://example.com/af8c96/tumblr_mnz8le_400.jpg")
            Assertions.assertEquals(expected, photo)
        }

        @Test
        internal fun normal_largeSizes_targetSizePosition_top() {
            val photoInfo = TumblrPhotoInfo(createTestPhotoListLargeSizesWithTargetSizeTop())

            val photo = MainApplication.getAppropriateSizePhotoObject(photoInfo)

            val expected = Photo(400, 600, "http://example.com/af8c96/tumblr_mnz8le_400.jpg")
            Assertions.assertEquals(expected, photo)
        }

        @Test
        internal fun normal_largeSizes_targetSizePosition_middle() {
            val photoInfo = TumblrPhotoInfo(createTestPhotoListLargeSizesWithTargetSizeMiddle())

            val photo = MainApplication.getAppropriateSizePhotoObject(photoInfo)

            val expected = Photo(400, 600, "http://example.com/af8c96/tumblr_mnz8le_400.jpg")
            Assertions.assertEquals(expected, photo)
        }

        @Test
        internal fun invalidJson() {
            val photoInfo = TumblrPhotoInfo(emptyList())

            val photo = MainApplication.getAppropriateSizePhotoObject(photoInfo)

            Assertions.assertNull(photo)
        }
    }

    companion object {
        fun createTestPhotoListSmallSizesWithTargetSizeTop(): List<Photo> = listOf(
                // Target size
                Photo(width = 400, height = 600, url = "http://example.com/af8c96/tumblr_mnz8le_400.jpg"),
                Photo(width = 250, height = 375, url = "http://example.com/af8c96/tumblr_mnz8le_250.jpg"),
                Photo(width = 100, height = 150, url = "http://example.com/af8c96/tumblr_mnz8le_100.jpg"),
                Photo(width = 75, height =75, url = "http://example.com/af8c96/tumblr_mnz8le_75.jpg")
        )

        fun createTestPhotoListSmallSizesWithTargetSizeMiddle(): List<Photo> = listOf(
                Photo(width = 250, height = 375, url = "http://example.com/af8c96/tumblr_mnz8le_250.jpg"),
                Photo(width = 100, height = 150, url = "http://example.com/af8c96/tumblr_mnz8le_100.jpg"),
                // Target size
                Photo(width = 400, height = 600, url = "http://example.com/af8c96/tumblr_mnz8le_400.jpg"),
                Photo(width = 75, height =75, url = "http://example.com/af8c96/tumblr_mnz8le_75.jpg")
        )

        fun createTestPhotoListLargeSizesWithTargetSizeTop(): List<Photo> = listOf(
                // Target size
                Photo(width = 400, height = 600, url = "http://example.com/af8c96/tumblr_mnz8le_400.jpg"),
                Photo(width = 600, height = 900, url = "http://example.com/af8c96/tumblr_mnz8le_600.jpg"),
                Photo(width = 500, height = 750, url = "http://example.com/af8c96/tumblr_mnz8le_500.jpg"),
                Photo(width = 700, height = 1050, url = "http://example.com/af8c96/tumblr_mnz8le_700.jpg")
        )

        fun createTestPhotoListLargeSizesWithTargetSizeMiddle(): List<Photo> = listOf(
                Photo(width = 600, height = 900, url = "http://example.com/af8c96/tumblr_mnz8le_600.jpg"),
                Photo(width = 500, height = 750, url = "http://example.com/af8c96/tumblr_mnz8le_500.jpg"),
                // Target size
                Photo(width = 400, height = 600, url = "http://example.com/af8c96/tumblr_mnz8le_400.jpg"),
                Photo(width = 700, height = 1050, url = "http://example.com/af8c96/tumblr_mnz8le_700.jpg")
        )
    }

}

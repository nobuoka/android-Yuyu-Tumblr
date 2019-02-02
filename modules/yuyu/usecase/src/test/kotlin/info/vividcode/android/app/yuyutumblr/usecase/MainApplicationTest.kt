package info.vividcode.android.app.yuyutumblr.usecase

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MainApplicationTest {

    private val mockMainView = mockk<MainView>()
    private val mockTumblrApi = mockk<TumblrApi>()

    init {
        every { mockMainView.setUserInputEventListener(any()) } returns Unit
        every { mockMainView.unsetUserInputEventListener() } returns Unit
        every { mockMainView.bindMainApplication(any()) } returns Unit
        every { mockMainView.unbindMainApplication() } returns Unit
        every { mockMainView.stopRefreshingIndicator() } returns Unit
    }

    private val retainLifecycleScope = MainApplication.RetainLifecycleScope(mockTumblrApi)

    private val mainApplication = MainApplication(mockMainView, retainLifecycleScope)

    @Test
    internal fun activate() {
        // Act
        mainApplication.activate()

        // Assert
        verify(exactly = 1) { mockMainView.setUserInputEventListener(any()) }
        verify(exactly = 1) { mockMainView.bindMainApplication(any()) }
        assertTrue(retainLifecycleScope.photoListInitialFetchRequester.responseObservable.isActive)
        assertNull(retainLifecycleScope.photoListNextPageFetchRequester)
    }

    @Test
    internal fun activate_withRetainedData() {
        val testTimestamp = 10000

        // Arrange
        mainApplication.activate()
        setupMockTumblrApiFetchPostsResult(TumblrApi.Result.Success(listOf(
                createTestPhotoPost(testTimestamp)
        )))
        mainApplication.updatePosts()
        mainApplication.deactivate()
        verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
        verify(exactly = 1) { mockTumblrApi.fetchPosts(isNull(), any()) }
        verify(exactly = 1) { mockMainView.setUserInputEventListener(any()) }
        verify(exactly = 1) { mockMainView.bindMainApplication(any()) }

        // Act
        val mainApplicationWithRetainedData = MainApplication(mockMainView, retainLifecycleScope)
        mainApplicationWithRetainedData.activate()

        // Assert
        verify(exactly = 2) { mockMainView.setUserInputEventListener(any()) }
        verify(exactly = 2) { mockMainView.bindMainApplication(any()) }
        assertTrue(retainLifecycleScope.photoListInitialFetchRequester.responseObservable.isActive)
        assertTrue(retainLifecycleScope.photoListNextPageFetchRequester?.responseObservable?.isActive == true)
    }

    @Test
    internal fun deactivate_notLoaded() {
        // Arrange
        mainApplication.activate()

        // Act
        mainApplication.deactivate()

        // Assert
        verify(exactly = 1) { mockMainView.unsetUserInputEventListener() }
        verify(exactly = 1) { mockMainView.unbindMainApplication() }
        assertFalse(retainLifecycleScope.photoListInitialFetchRequester.responseObservable.isActive)
        assertNull(retainLifecycleScope.photoListNextPageFetchRequester)
    }

    @Test
    internal fun deactivate_afterLoaded() {
        val testTimestamp = 10000

        // Arrange
        mainApplication.activate()
        setupMockTumblrApiFetchPostsResult(TumblrApi.Result.Success(listOf(
                createTestPhotoPost(testTimestamp)
        )))
        mainApplication.updatePosts()
        verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
        verify(exactly = 1) { mockTumblrApi.fetchPosts(isNull(), any()) }

        // Act
        mainApplication.deactivate()

        // Assert
        verify(exactly = 1) { mockMainView.unsetUserInputEventListener() }
        verify(exactly = 1) { mockMainView.unbindMainApplication() }
        assertFalse(retainLifecycleScope.photoListInitialFetchRequester.responseObservable.isActive)
        assertEquals(false, retainLifecycleScope.photoListNextPageFetchRequester?.responseObservable?.isActive)
    }

    @Nested
    internal inner class RequestInitialLoadIfNeededPosts {
        @Test
        internal fun request_whenPostsEmpty() {
            mainApplication.activate()
            setupMockTumblrApiFetchPostsResult(TumblrApi.Result.Success(emptyList()))

            mainApplication.requestInitialLoadIfNeeded()

            verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(isNull(), any()) }
        }

        @Test
        internal fun notRequest_whenPostsNotEmpty() {
            mainApplication.activate()
            mainApplication.photoTimeline.addPhotos(listOf(
                    TumblrPost.Photo(100, listOf(TumblrPhotoInfo(listOf(
                            Photo(width = 100, height = 100, url = "http://example.com/image-1.jpg")
                    ))))
            ))
            setupMockTumblrApiFetchPostsResult(TumblrApi.Result.Success(emptyList()))

            mainApplication.requestInitialLoadIfNeeded()

            verify(exactly = 0) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 0) { mockTumblrApi.fetchPosts(isNull(), any()) }
        }
    }

    @Nested
    internal inner class UpdatePosts {
        @Test
        internal fun resultSuccess_noPhoto() {
            mainApplication.activate()

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
            val testTimestamp1 = 1370531100
            val testTimestamp2 = 1370531000
            val testTimestamp3 = 1370530000
            setupMockTumblrApiFetchPostsResult(TumblrApi.Result.Success(listOf(
                    createTestPhotoPost(testTimestamp1)
            )), parameterTimestamp = null)
            setupMockTumblrApiFetchPostsResult(TumblrApi.Result.Success(listOf(
                    createTestPhotoPost(testTimestamp2)
            )), parameterTimestamp = testTimestamp1)
            setupMockTumblrApiFetchPostsResult(TumblrApi.Result.Success(listOf(
                    createTestPhotoPost(testTimestamp3)
            )), parameterTimestamp = testTimestamp2)
            mainApplication.activate()

            mainApplication.updatePosts()

            verify(exactly = 1) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(isNull(), any()) }

            // Second request should fetch next page, because response of first request have a post.
            mainApplication.updatePosts()

            verify(exactly = 2) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(eq(testTimestamp1), any()) }

            // Second request should fetch next page, because response of first request have a post.
            mainApplication.updatePosts()

            verify(exactly = 3) { mockMainView.stopRefreshingIndicator() }
            verify(exactly = 1) { mockTumblrApi.fetchPosts(eq(testTimestamp2), any()) }
        }

        @Test
        internal fun resultFailure() {
            mainApplication.activate()

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

    private fun setupMockTumblrApiFetchPostsResult(result: TumblrApi.Result<List<TumblrPost>>, parameterTimestamp: Int? = null) {
        val lambdaSlot = CapturingSlot<(TumblrApi.Result<List<TumblrPost>>) -> Unit>()
        every {
            mockTumblrApi.fetchPosts(
                    if (parameterTimestamp == null) isNull() else eq(parameterTimestamp),
                    capture(lambdaSlot)
            )
        } answers {
            lambdaSlot.captured(result)
        }
    }

    private fun createTestPhotoPost(timestamp: Int) =
            TumblrPost.Photo(
                    timestamp = timestamp,
                    photos = listOf(
                            TumblrPhotoInfo(listOf(
                                    Photo(width = 400, height = 600, url = "http://example.com/af8c96/tumblr_mnz8le_400.jpg"),
                                    Photo(width = 250, height = 375, url = "http://example.com/af8c96/tumblr_mnz8le_250.jpg")
                            ))
                    )
            )

}

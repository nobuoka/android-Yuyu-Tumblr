package info.vividcode.android.app.yuyutumblr.usecase

import info.vividcode.android.app.yuyu.utils.Observer
import info.vividcode.android.app.yuyutumblr.usecase.tools.UseCaseDriver
import kotlinx.coroutines.*

val backgroundCoroutineContext = newFixedThreadPoolContext(4, "BackgroundCoroutineContext")

class MainApplication(
        private val mainView: MainView,
        private val retainLifecycleScope: RetainLifecycleScope
) {

    class RetainLifecycleScope(
            tumblrApi: TumblrApi
    ) {
        val retainLifecycleCoroutineScope = CoroutineScope(Dispatchers.Main)
        val showMainTimelineUseCaseDriver = UseCaseDriver(
                ShowMainTimelineUseCase, retainLifecycleCoroutineScope,
                Dispatchers.Main, backgroundCoroutineContext,
                object : ShowMainTimelineUseCase.BackgroundEnvironment {
                    override val httpClient: String = "Http Client"
                }
        )
        val photoListInitialFetchRequester = PhotoListInitialFetchRequester(tumblrApi)
        var photoListNextPageFetchRequester: PhotoListNextPageFetchRequester? = null

        val photoListNextPageFetchRequesterFactory = PhotoListNextPageFetchRequester.createFactory(tumblrApi)

        val photoTimeline: PhotoTimeline = PhotoTimeline()

        fun replaceNextPageRequester(
                nextPageFetchRequester: PhotoListNextPageFetchRequester?,
                observer: Observer<PhotoListNextPageFetchRequester.Response>
        ) {
            photoListNextPageFetchRequester?.responseObservable?.unsetObserver()
            photoListNextPageFetchRequester = nextPageFetchRequester
            photoListNextPageFetchRequester?.responseObservable?.setObserver(observer)
        }
    }

    val photoTimeline get() = retainLifecycleScope.photoTimeline

    val nextPageLoaderState = NextPageLoaderStateHolder(NextPageLoaderStateHolder.State.NoNextPage)

    private val nextPageFetchRequesterObserver: Observer<PhotoListNextPageFetchRequester.Response> =
            { response -> updateTimeline(response.result) }

    private val userInputEventListener = object : MainView.UserInputEventListener {
        override fun onRefreshRequest() {
            updatePosts()
        }
        override fun onNextPageLoadRequest() {
            requestNextPage()
        }
    }

    fun activate() {
        mainView.setUserInputEventListener(userInputEventListener)
        mainView.bindMainApplication(this)

        retainLifecycleScope.showMainTimelineUseCaseDriver.activate(object : ShowMainTimelineUseCase.ForegroundEnvironment {
            override val mainTimelineView: MainTimelineView = object : MainTimelineView {
                override fun showProgressBar() {
                    println("show progress bar")
                }
            }
        })
        retainLifecycleScope.photoListInitialFetchRequester.responseObservable.setObserver { response ->
            updateTimeline(response.result)
        }
        retainLifecycleScope.photoListNextPageFetchRequester?.
                responseObservable?.setObserver(nextPageFetchRequesterObserver)
    }

    fun deactivate() {
        retainLifecycleScope.photoListInitialFetchRequester.responseObservable.unsetObserver()
        retainLifecycleScope.photoListNextPageFetchRequester?.responseObservable?.unsetObserver()
        retainLifecycleScope.showMainTimelineUseCaseDriver.deactivate()

        mainView.unbindMainApplication()
        mainView.unsetUserInputEventListener()
    }

    private fun replaceNextPageRequester(nextPageFetchRequester: PhotoListNextPageFetchRequester?) {
        retainLifecycleScope.replaceNextPageRequester(nextPageFetchRequester, nextPageFetchRequesterObserver)
        if (nextPageFetchRequester != null) {
            nextPageLoaderState.updateState(NextPageLoaderStateHolder.State.Idle)
        } else {
            nextPageLoaderState.updateState(NextPageLoaderStateHolder.State.NoNextPage)
        }
    }

    fun requestInitialLoadIfNeeded() {
        if (photoTimeline.size == 0) {
            updatePosts()
            retainLifecycleScope.showMainTimelineUseCaseDriver.handleCommand(ShowMainTimelineUseCase.Command.Start)
        }
    }

    fun updatePosts() {
        retainLifecycleScope.photoListNextPageFetchRequester.let { requester ->
            if (requester != null) {
                requestNextPage()
            } else {
                retainLifecycleScope.photoListInitialFetchRequester()
            }
        }
    }

    fun requestNextPage() {
        retainLifecycleScope.photoListNextPageFetchRequester.also { requester ->
            if (requester != null) {
                requester()
                nextPageLoaderState.updateState(NextPageLoaderStateHolder.State.Progress)
            } else {
                logger.warn("NextPageFetchRequester is null but next page load is requested")
            }
        }
    }

    private fun updateTimeline(result: TumblrApi.Result<List<TumblrPost>>) {
        mainView.stopRefreshingIndicator()

        when (result) {
            is TumblrApi.Result.Success -> {
                val posts = result.responseContent
                retainLifecycleScope.photoTimeline.addPhotos(posts)
                replaceNextPageRequester(posts.lastOrNull()?.let {
                    retainLifecycleScope.photoListNextPageFetchRequesterFactory(it.timestamp)
                })
            }
            is TumblrApi.Result.Failure -> {
                logger.error("res: error", result.exception)
                val errorState = NextPageLoaderStateHolder.State.Error(result.exception.message ?: "${result.exception}")
                nextPageLoaderState.updateState(errorState)
            }
        } as? Unit?
    }

    companion object {
        private const val DESIRED_IMAGE_WIDTH = 400

        /**
         * Select a photo of which width is most closest to [DESIRED_IMAGE_WIDTH].
         */
        fun getAppropriateSizePhotoObject(photoInfo: TumblrPhotoInfo): Photo? =
                photoInfo.altSizes.sortedWith(
                        compareBy<Photo> { value -> Math.abs(DESIRED_IMAGE_WIDTH - value.width) }
                                .then(compareBy { value -> value.width })
                                .then(compareBy { value -> value.height })
                                .then(compareBy { value -> value.url })
                ).firstOrNull()
    }

}

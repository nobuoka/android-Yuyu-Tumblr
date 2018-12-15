package info.vividcode.android.app.yuyutumblr.usecase

import info.vividcode.android.app.yuyu.utils.Observer

class MainApplication(
        private val mainView: MainView,
        private val retainLifecycleScope: RetainLifecycleScope
) {

    class RetainLifecycleScope(
            tumblrApi: TumblrApi
    ) {
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

    fun activate() {
        mainView.setRefreshEventListener(::updatePosts)
        mainView.bindMainApplication(this)

        retainLifecycleScope.photoListInitialFetchRequester.responseObservable.setObserver { response ->
            updateTimeline(response.result)
        }
        retainLifecycleScope.photoListNextPageFetchRequester?.responseObservable?.setObserver { response ->
            updateTimeline(response.result)
        }
    }

    fun deactivate() {
        retainLifecycleScope.photoListInitialFetchRequester.responseObservable.unsetObserver()
        retainLifecycleScope.photoListNextPageFetchRequester?.responseObservable?.unsetObserver()

        mainView.unbindMainApplication()
        mainView.unsetRefreshEventListener()
    }

    private fun replaceNextPageRequester(nextPageFetchRequester: PhotoListNextPageFetchRequester?) {
        retainLifecycleScope.replaceNextPageRequester(nextPageFetchRequester) { response ->
            updateTimeline(response.result)
        }
    }

    fun requestInitialLoadIfNeeded() {
        if (photoTimeline.size == 0) {
            updatePosts()
        }
    }

    fun updatePosts() {
        retainLifecycleScope.photoListNextPageFetchRequester.let { requester ->
            if (requester != null) {
                requester()
            } else {
                retainLifecycleScope.photoListInitialFetchRequester()
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

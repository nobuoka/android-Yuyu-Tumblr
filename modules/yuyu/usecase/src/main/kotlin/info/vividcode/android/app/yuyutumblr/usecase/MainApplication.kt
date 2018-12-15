package info.vividcode.android.app.yuyutumblr.usecase

class MainApplication(private val mainView: MainView, tumblrApi: TumblrApi) {

    private val photoListInitialFetchRequester = PhotoListInitialFetchRequester(tumblrApi)
    private var photoListNextPageFetchRequester: PhotoListNextPageFetchRequester? = null

    private val photoListNextPageFetchRequesterFactory =
            PhotoListNextPageFetchRequester.createFactory(tumblrApi)

    val photoTimeline = PhotoTimeline()

    fun init() {
        mainView.setRefreshEventListener(::updatePosts)
        mainView.bindMainApplication(this)

        photoListInitialFetchRequester.responseObservable.connect { response ->
            updateTimeline(response.result)
        }
    }

    private fun replaceNextPageRequester(nextPageFetchRequester: PhotoListNextPageFetchRequester?) {
        photoListNextPageFetchRequester?.responseObservable?.disconnectAll()
        photoListNextPageFetchRequester = nextPageFetchRequester
        photoListNextPageFetchRequester?.responseObservable?.connect { response ->
            updateTimeline(response.result)
        }
    }

    fun updatePosts() {
        photoListNextPageFetchRequester.let { requester ->
            if (requester != null) {
                requester()
            } else {
                photoListInitialFetchRequester()
            }
        }
    }

    private fun updateTimeline(result: TumblrApi.Result<List<TumblrPost>>) {
        mainView.stopRefreshingIndicator()

        when (result) {
            is TumblrApi.Result.Success -> {
                val posts = result.responseContent
                photoTimeline.addPhotos(posts)
                replaceNextPageRequester(posts.lastOrNull()?.let {
                    photoListNextPageFetchRequesterFactory(it.timestamp)
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

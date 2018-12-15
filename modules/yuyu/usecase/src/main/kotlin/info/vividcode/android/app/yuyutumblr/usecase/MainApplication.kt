package info.vividcode.android.app.yuyutumblr.usecase

import org.json.JSONException

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
                try {
                    val posts = result.responseContent
                    photoTimeline.addPhotos(posts)
                    replaceNextPageRequester(posts.lastOrNull()?.let {
                        photoListNextPageFetchRequesterFactory(it.timestamp)
                    })
                } catch (err: JSONException) {
                    logger.error("res: error", err)
                }
            }
            is TumblrApi.Result.Failure -> {
                logger.error("res: error", result.exception)
            }
        } as? Unit?
    }

    companion object {
        fun getAppropriateSizePhotoObject(photoInfo: TumblrPhotoInfo): Photo? {
            var photo: Photo? = null
            try {
                val arr = photoInfo.altSizes
                for (p in arr) {
                    if (photo == null) {
                        photo = p
                    } else {
                        if (p.width <= 400) {
                            if (photo.width < p.width) {
                                photo = p
                            }
                        } else {
                            if (p.width < photo.width) {
                                photo = p
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            return photo
        }
    }

}

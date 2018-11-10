package info.vividcode.android.app.yuyutumblr.usecase

import org.json.JSONException
import org.json.JSONObject
import java.util.*

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

    private fun updateTimeline(result: TumblrApi.Result<JSONObject>) {
        mainView.stopRefreshingIndicator()

        when (result) {
            is TumblrApi.Result.Success -> {
                try {
                    val posts = result.responseContent.getJSONArray("response")
                    val pp = ArrayList<JSONObject>()
                    val length = posts.length()
                    for (i in 0 until length) {
                        pp.add(posts.getJSONObject(i))
                    }
                    photoTimeline.addPhotos(pp)
                    replaceNextPageRequester(pp.lastOrNull()?.let {
                        photoListNextPageFetchRequesterFactory(it.getInt("timestamp"))
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
        /**
         * response":[
         * {
         * "blog_name":"fileth",
         * "id":52302908168,
         * "post_url":"http:\/\/fileth.tumblr.com\/post\/52302908168\/on-twitpic",
         * "slug":"on-twitpic",
         * "type":"photo",
         * "date":"2013-06-06 15:05:00 GMT",
         * "timestamp":1370531100,
         * "state":"published",
         * "format":"html",
         * "reblog_key":"m3GAoQbz",
         * "tags":["\u3086\u3086\u5f0f","\u677e\u672c\u983c\u5b50","yuyushiki","yoriko matsumoto"],
         * "short_url":"http:\/\/tmblr.co\/Z1qtXymjVmi8",
         * "highlighted":[],
         * "note_count":3,
         * "source_url":"http:\/\/twitpic.com\/cvmf4w\/full",
         * "source_title":"twitpic.com",
         * "caption":"\u003Cp\u003E\u003Ca href=\u0022http:\/\/twitpic.com\/cvmf4w\/full\u0022 target=\u0022_blank\u0022\u003E\u304a\u6bcd\u3055\u3093\u5148\u751f on Twitpic\u003C\/a\u003E\u003C\/p\u003E",
         * "link_url":"http:\/\/twitpic.com\/cvmf4w\/full",
         * "image_permalink":"http:\/\/fileth.tumblr.com\/image\/52302908168",
         * "photos":[
         * {
         * "caption":"",
         * "alt_sizes":[
         * {"width":400,"height":600,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_400.jpg"},
         * {"width":250,"height":375,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_250.jpg"},
         * {"width":100,"height":150,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_100.jpg"},
         * {"width":75,"height":75,"url":"http:\/\/25.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_75sq.jpg"}
         * ],
         * "original_size":{"width":400,"height":600,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_400.jpg"}
         * }
         * ]
         * },
         * {"blog_name":"anisample","id":52300690971,"post_url":"http:\/\/anisample.tumblr.com\/post\/52300690971","slug":"","type":"audio","date":"2013-06-06 14:16:32 GMT","timestamp":1370528192,"state":"published","format":"html","reblog_key":"yIm7xUJf","tags":["\u3086\u3086\u5f0f","\u91ce\u3005\u539f\u3086\u305a\u3053","\u5927\u4e45\u4fdd\u7460\u7f8e"],"short_url":"http:\/\/tmblr.co\/Zj_7osmjNJOR","highlighted":[],"note_count":7,"artist":"\u91ce\u3005\u539f \u3086\u305a\u3053","album":"\u3086\u3086\u5f0f","track_name":"\u3053\u306e\u4e0a\u304c\u3063\u305f\u306e\u3069\u3046\u3057\u3088\u3046","album_art":"http:\/\/25.media.tumblr.com\/tumblr_mnz6bkviyS1s85v8so1_1370528192_cover.jpg","caption":"","player":"\u003Cembed type=\u0022application\/x-shockwave-flash\u0022 src=\u0022http:\/\/assets.tumblr.com\/swf\/audio_player.swf?audio_file=http%3A%2F%2Fwww.tumblr.com%2Faudio_file%2Fanisample%2F52300690971%2Ftumblr_mnz6bkviyS1s85v8s&color=FFFFFF\u0022 height=\u002227\u0022 width=\u0022207\u0022 quality=\u0022best\u0022 wmode=\u0022opaque\u0022\u003E\u003C\/embed\u003E","embed":"\u003Ciframe class=\u0022tumblr_audio_player tumblr_audio_player_52300690971\u0022 src=\u0022http:\/\/anisample.tumblr.com\/post\/52300690971\/audio_player_iframe\/anisample\/tumblr_mnz6bkviyS1s85v8s?audio_file=http%3A%2F
         */
        fun getAppropriateSizePhotoObject(photoInfo: JSONObject): Photo? {
            var photo: Photo? = null
            try {
                val arr = photoInfo.getJSONArray("alt_sizes")
                val len = arr.length()
                for (i in 0 until len) {
                    val o = arr.getJSONObject(i)
                    val w = o.getInt("width")
                    val h = o.getInt("height")
                    val u = o.getString("url")
                    val p = Photo(w, h, u)
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

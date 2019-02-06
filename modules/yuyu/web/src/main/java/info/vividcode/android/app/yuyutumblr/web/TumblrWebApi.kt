package info.vividcode.android.app.yuyutumblr.web

import info.vividcode.android.app.yuyutumblr.usecase.TumblrApi
import info.vividcode.android.app.yuyutumblr.usecase.TumblrPost
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class TumblrWebApi(
        private val apiKey: String,
        private val callFactory: Call.Factory,
        private val callbackExecutor: CallbackExecutor,
        private val tumblrWebApiBaseUrl: String = "https://api.tumblr.com"
) : TumblrApi {

    interface CallbackExecutor {
        fun <T> execute(value: T, execution: (T) -> Unit)
    }

    override fun fetchPosts(lastTimestamp: Int?, callback: (TumblrApi.Result<List<TumblrPost>>) -> Unit) {
        // http://www.tumblr.com/docs/en/api/v2#tagged-method
        var uri = "$tumblrWebApiBaseUrl/v2/tagged?tag=%E3%82%86%E3%82%86%E5%BC%8F" +
                "&api_key=${URLEncoder.encode(apiKey, "UTF-8")}"
        if (lastTimestamp != null) {
            uri += "&before=$lastTimestamp"
        }

        val request = Request.Builder()
                .url(uri)
                .build()
        callFactory.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseContent: TumblrApi.Result<List<TumblrPost>> = try {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val jsonObject = it.string().let(::JSONObject)
                            TumblrApi.Result.Success(TumblrResponseConverter.convertTumblrPostsResponse(jsonObject))
                        } ?: TumblrApi.Result.Failure(ErrorResponseException("Not expected response body", response))
                    } else {
                        TumblrApi.Result.Failure(ErrorResponseException("Not successful response", response))
                    }
                } catch (e: Throwable) {
                    TumblrApi.Result.Failure(e)
                }
                callbackExecutor.execute(responseContent, callback)
            }
            override fun onFailure(call: Call, e: IOException) {
                callbackExecutor.execute(TumblrApi.Result.Failure(e), callback)
            }
        })
    }

    class ErrorResponseException(message: String, val response: okhttp3.Response) : RuntimeException(message)

}

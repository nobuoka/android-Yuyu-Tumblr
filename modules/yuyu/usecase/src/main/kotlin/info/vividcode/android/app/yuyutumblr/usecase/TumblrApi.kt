package info.vividcode.android.app.yuyutumblr.usecase

import org.json.JSONObject

interface TumblrApi {

    fun fetchPosts(lastTimestamp: Int?, callback: (Result<JSONObject>) -> Unit)

    sealed class Result<T> {
        class Success<T>(val responseContent: T) : Result<T>()
        class Failure<T>(val exception: Exception) : Result<T>()
    }

}

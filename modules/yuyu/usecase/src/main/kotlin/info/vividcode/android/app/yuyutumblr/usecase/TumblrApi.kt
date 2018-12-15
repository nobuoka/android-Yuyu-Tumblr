package info.vividcode.android.app.yuyutumblr.usecase

interface TumblrApi {

    fun fetchPosts(lastTimestamp: Int?, callback: (Result<List<TumblrPost>>) -> Unit)

    sealed class Result<T> {
        class Success<T>(val responseContent: T) : Result<T>()
        class Failure<T>(val exception: Throwable) : Result<T>()
    }

}

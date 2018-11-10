package info.vividcode.android.app.yuyutumblr.usecase

import info.vividcode.android.app.yuyu.utils.Observable
import info.vividcode.android.app.yuyu.utils.Subject
import org.json.JSONObject

interface PhotoListNextPageFetchRequester {

    class Response(val result: TumblrApi.Result<JSONObject>)

    enum class State {
        Idle, Progress
    }

    operator fun invoke()

    val state: State

    val responseObservable: Observable<Response>

    companion object {
        fun createFactory(tumblrApi: TumblrApi): (lastTimestamp: Int) -> PhotoListNextPageFetchRequester =
                { lastTimestamp -> Impl(tumblrApi, lastTimestamp) }
    }

    private class Impl(
            private val tumblrApi: TumblrApi, private val lastTimestamp: Int
    ) : PhotoListNextPageFetchRequester {
        override var state: State = State.Idle
        override val responseObservable = Subject<Response>()

        override fun invoke() {
            if (state == State.Progress) return

            state = State.Progress
            tumblrApi.fetchPosts(lastTimestamp) { result ->
                responseObservable(Response(result))
                state = State.Idle
            }
        }
    }

}

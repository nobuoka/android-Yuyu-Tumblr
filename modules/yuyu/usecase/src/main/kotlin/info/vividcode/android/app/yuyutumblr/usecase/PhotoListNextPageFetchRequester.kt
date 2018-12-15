package info.vividcode.android.app.yuyutumblr.usecase

import info.vividcode.android.app.yuyu.utils.BufferedObservable
import info.vividcode.android.app.yuyu.utils.Subject

interface PhotoListNextPageFetchRequester {

    class Response(val result: TumblrApi.Result<List<TumblrPost>>)

    enum class State {
        Idle, Progress
    }

    operator fun invoke()

    val state: State

    val responseObservable: BufferedObservable<Response>

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

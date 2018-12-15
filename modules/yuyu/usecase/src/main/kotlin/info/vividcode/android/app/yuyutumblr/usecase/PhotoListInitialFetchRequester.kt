package info.vividcode.android.app.yuyutumblr.usecase

import info.vividcode.android.app.yuyu.utils.BufferedObservable
import info.vividcode.android.app.yuyu.utils.Subject

interface PhotoListInitialFetchRequester {

    class Response(val result: TumblrApi.Result<List<TumblrPost>>)

    enum class State {
        Idle, Progress
    }

    operator fun invoke()

    val state: State

    val responseObservable: BufferedObservable<Response>

    companion object : (TumblrApi) -> PhotoListInitialFetchRequester {
        override fun invoke(tumblrApi: TumblrApi): PhotoListInitialFetchRequester = Impl(tumblrApi)
    }

    private class Impl(private val tumblrApi: TumblrApi) : PhotoListInitialFetchRequester {
        override var state: State = State.Idle
        override val responseObservable = Subject<Response>()

        override fun invoke() {
            if (state == State.Progress) return

            state = State.Progress
            tumblrApi.fetchPosts(null) { result ->
                responseObservable(Response(result))
                state = State.Idle
            }
        }
    }

}

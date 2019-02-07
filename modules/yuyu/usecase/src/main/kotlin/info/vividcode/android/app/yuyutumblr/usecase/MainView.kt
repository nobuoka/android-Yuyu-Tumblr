package info.vividcode.android.app.yuyutumblr.usecase

interface MainView {
    fun setUserInputEventListener(listener: UserInputEventListener)
    fun unsetUserInputEventListener()
    fun stopRefreshingIndicator()
    fun bindMainApplication(mainApplication: MainApplication)
    fun unbindMainApplication()

    interface UserInputEventListener {
        fun onRefreshRequest()
        fun onNextPageLoadRequest()
    }
}

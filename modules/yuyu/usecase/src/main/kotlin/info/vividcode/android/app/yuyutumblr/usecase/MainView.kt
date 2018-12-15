package info.vividcode.android.app.yuyutumblr.usecase

interface MainView {
    fun setRefreshEventListener(listener: () -> Unit)
    fun unsetRefreshEventListener()
    fun stopRefreshingIndicator()
    fun bindMainApplication(mainApplication: MainApplication)
    fun unbindMainApplication()
}

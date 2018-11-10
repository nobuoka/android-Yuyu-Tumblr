package info.vividcode.android.app.yuyutumblr.usecase

interface MainView {
    fun setRefreshEventListener(listener: () -> Unit)
    fun stopRefreshingIndicator()
    fun bindMainApplication(mainApplication: MainApplication)
}

package info.vividcode.android.app.yuyutumblr.usecase

import org.json.JSONObject

interface MainView {
    fun setRefreshEventListener(listener: () -> Unit)
    fun stopRefreshingIndicator()
    fun addPosts(posts: List<JSONObject>)
    fun getLatestPost(): JSONObject?
}

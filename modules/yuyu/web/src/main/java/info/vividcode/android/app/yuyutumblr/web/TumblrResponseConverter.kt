package info.vividcode.android.app.yuyutumblr.web

import info.vividcode.android.app.yuyutumblr.usecase.TumblrPost
import org.json.JSONObject

object TumblrResponseConverter {

    fun convertTumblrPostsResponse(responseContent: JSONObject): List<TumblrPost> {
        val posts = responseContent.getJSONArray("response")
        val pp = ArrayList<TumblrPost>()
        val length = posts.length()
        for (i in 0 until length) {
            pp.add(TumblrPost(posts.getJSONObject(i)))
        }
        return pp
    }

}

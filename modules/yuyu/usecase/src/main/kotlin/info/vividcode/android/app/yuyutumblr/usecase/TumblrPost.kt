package info.vividcode.android.app.yuyutumblr.usecase

import org.json.JSONArray
import org.json.JSONObject

data class TumblrPost(val postJson: JSONObject) {
    val type: String get() = postJson.getString("type")
    val timestamp: Int get() = postJson.getInt("timestamp")
    /** If [type] is "photo" then this return json array, otherwise this will throw exception. */
    val photos: JSONArray get() = postJson.getJSONArray("photos")
}

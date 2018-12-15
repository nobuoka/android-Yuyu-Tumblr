package info.vividcode.android.app.yuyutumblr.usecase

import org.json.JSONObject

data class TumblrPhotoInfo(val photoJson: JSONObject) {
    val altSizes: List<Photo>
        get() {
            val arr = photoJson.getJSONArray("alt_sizes")
            val len = arr.length()
            val list = mutableListOf<Photo>()
            for (i in 0 until len) {
                val o = arr.getJSONObject(i)
                val w = o.getInt("width")
                val h = o.getInt("height")
                val u = o.getString("url")
                list.add(Photo(w, h, u))
            }
            return list
        }
}

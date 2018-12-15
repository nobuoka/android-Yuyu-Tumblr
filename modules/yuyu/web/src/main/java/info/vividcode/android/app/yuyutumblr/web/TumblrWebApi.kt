package info.vividcode.android.app.yuyutumblr.web

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import info.vividcode.android.app.yuyutumblr.usecase.TumblrApi
import info.vividcode.android.app.yuyutumblr.usecase.TumblrPost
import org.json.JSONException

class TumblrWebApi(private val requestQueue: RequestQueue) : TumblrApi {

    override fun fetchPosts(lastTimestamp: Int?, callback: (TumblrApi.Result<List<TumblrPost>>) -> Unit) {
        // API キーは Tumblr のドキュメントにのってたやつ。 ほんとは各自取得する必要がある?
        // http://www.tumblr.com/docs/en/api/v2#tagged-method
        var uri = "https://api.tumblr.com/v2/tagged?tag=%E3%82%86%E3%82%86%E5%BC%8F" + "&api_key=fuiKNFp9vQFvjLNvx4sUwti4Yb5yGutBN4Xh10LXZhhRKjWlV4"
        if (lastTimestamp != null) {
            try {
                uri += "&before=$lastTimestamp"
            } catch (e: JSONException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }

        // リクエスト生成
        val req = JsonObjectRequest(uri, null, Response.Listener { response ->
            val responseContent = try {
                TumblrApi.Result.Success(TumblrResponseConverter.convertTumblrPostsResponse(response))
            } catch (e: Throwable) {
                TumblrApi.Result.Failure<List<TumblrPost>>(e)
            }
            callback(responseContent)
        }, Response.ErrorListener { error ->
            callback(TumblrApi.Result.Failure(error))
        })
        // リクエストをキューに追加
        requestQueue.add(req)
    }

}

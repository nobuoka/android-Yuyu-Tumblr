package info.vividcode.android.app.yuyutumblr

import org.json.JSONException
import org.json.JSONObject

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.ImageLoader.ImageCache
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import android.graphics.Color
import android.os.Bundle
import android.app.Activity
import android.graphics.Bitmap
import android.support.v4.util.LruCache
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import info.vividcode.android.app.yuyutumblr.ui.PostAdapter

import java.util.ArrayList

class MainActivity : Activity() {

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mRequestQueue: RequestQueue? = null
    private var mImageLoader: ImageLoader? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: PostAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    private var mUpdating = false

    private class BitmapCache : ImageCache {

        private val mCache: LruCache<String, Bitmap>

        init {
            val maxSize = 5 * 1024 * 1024
            mCache = object : LruCache<String, Bitmap>(maxSize) {
                override fun sizeOf(key: String, value: Bitmap): Int {
                    return value.rowBytes * value.height
                }
            }
        }

        override fun getBitmap(url: String): Bitmap? {
            return mCache.get(url)
        }

        override fun putBitmap(url: String, bitmap: Bitmap) {
            mCache.put(url, bitmap)
        }

    }

    class Photo(val width: Int, val height: Int, val url: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRequestQueue = Volley.newRequestQueue(this)
        val imageLoader = ImageLoader(mRequestQueue, BitmapCache())
        mImageLoader = imageLoader

        // スクロール限界までスクロールしてさらに引っ張ると続きを読み込む仕組み
        mRecyclerView = findViewById<View>(R.id.posts_view) as RecyclerView

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        mAdapter = PostAdapter(imageLoader)
        mRecyclerView!!.adapter = mAdapter
        mSwipeRefreshLayout = findViewById<View>(R.id.swipe_refresh_layout) as SwipeRefreshLayout

        mSwipeRefreshLayout!!.setColorSchemeColors(Color.RED)
        mSwipeRefreshLayout!!.setOnRefreshListener { updatePosts() }
    }

    override fun onRestart() {
        super.onRestart()
        mRequestQueue!!.start() // start 時に呼び出さないのは onCreate で start されるため
    }

    override fun onResume() {
        super.onResume()

        updatePosts()

        // 更新ボタンは使えないようにしておく (特に意味はない)
        val updateButton = findViewById<View>(R.id.update_posts_button) as Button
        updateButton.setOnClickListener {
            Log.d("click", "clicked")
            updatePosts()
        }
        updateButton.isEnabled = false
    }

    override fun onStop() {
        mRequestQueue!!.stop()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun updatePosts() {
        // 引っ張って更新時は何回もこのメソッドが呼ばれるので, 更新処理中は何もしない
        if (mUpdating) return
        mUpdating = true
        // API キーは Tumblr のドキュメントにのってたやつ。 ほんとは各自取得する必要がある?
        // http://www.tumblr.com/docs/en/api/v2#tagged-method
        var uri = "http://api.tumblr.com/v2/tagged?tag=%E3%82%86%E3%82%86%E5%BC%8F" + "&api_key=fuiKNFp9vQFvjLNvx4sUwti4Yb5yGutBN4Xh10LXZhhRKjWlV4"

        val post = mAdapter!!.lastItem
        if (post != null) {
            try {
                val lastTimestamp = post.getInt("timestamp")
                uri += "&before=$lastTimestamp"
            } catch (e: JSONException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        }

        // リクエスト生成
        val req = JsonObjectRequest(uri, null, Response.Listener { response ->
            mUpdating = false
            mSwipeRefreshLayout!!.isRefreshing = false
            try {
                val posts = response.getJSONArray("response")
                val pp = ArrayList<JSONObject>()
                val length = posts.length()
                for (i in 0 until length) {
                    pp.add(posts.getJSONObject(i))
                }
                mAdapter!!.add(pp)
            } catch (err: JSONException) {
                Log.d("res", "error", err)
            }
        }, Response.ErrorListener { error ->
            mUpdating = false
            mSwipeRefreshLayout!!.isRefreshing = false
            Log.d("res", "error", error)
        })
        // リクエストをキューに追加
        mRequestQueue!!.add(req)
    }

    companion object {

        /**
         * response":[
         * {
         * "blog_name":"fileth",
         * "id":52302908168,
         * "post_url":"http:\/\/fileth.tumblr.com\/post\/52302908168\/on-twitpic",
         * "slug":"on-twitpic",
         * "type":"photo",
         * "date":"2013-06-06 15:05:00 GMT",
         * "timestamp":1370531100,
         * "state":"published",
         * "format":"html",
         * "reblog_key":"m3GAoQbz",
         * "tags":["\u3086\u3086\u5f0f","\u677e\u672c\u983c\u5b50","yuyushiki","yoriko matsumoto"],
         * "short_url":"http:\/\/tmblr.co\/Z1qtXymjVmi8",
         * "highlighted":[],
         * "note_count":3,
         * "source_url":"http:\/\/twitpic.com\/cvmf4w\/full",
         * "source_title":"twitpic.com",
         * "caption":"\u003Cp\u003E\u003Ca href=\u0022http:\/\/twitpic.com\/cvmf4w\/full\u0022 target=\u0022_blank\u0022\u003E\u304a\u6bcd\u3055\u3093\u5148\u751f on Twitpic\u003C\/a\u003E\u003C\/p\u003E",
         * "link_url":"http:\/\/twitpic.com\/cvmf4w\/full",
         * "image_permalink":"http:\/\/fileth.tumblr.com\/image\/52302908168",
         * "photos":[
         * {
         * "caption":"",
         * "alt_sizes":[
         * {"width":400,"height":600,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_400.jpg"},
         * {"width":250,"height":375,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_250.jpg"},
         * {"width":100,"height":150,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_100.jpg"},
         * {"width":75,"height":75,"url":"http:\/\/25.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_75sq.jpg"}
         * ],
         * "original_size":{"width":400,"height":600,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_400.jpg"}
         * }
         * ]
         * },
         * {"blog_name":"anisample","id":52300690971,"post_url":"http:\/\/anisample.tumblr.com\/post\/52300690971","slug":"","type":"audio","date":"2013-06-06 14:16:32 GMT","timestamp":1370528192,"state":"published","format":"html","reblog_key":"yIm7xUJf","tags":["\u3086\u3086\u5f0f","\u91ce\u3005\u539f\u3086\u305a\u3053","\u5927\u4e45\u4fdd\u7460\u7f8e"],"short_url":"http:\/\/tmblr.co\/Zj_7osmjNJOR","highlighted":[],"note_count":7,"artist":"\u91ce\u3005\u539f \u3086\u305a\u3053","album":"\u3086\u3086\u5f0f","track_name":"\u3053\u306e\u4e0a\u304c\u3063\u305f\u306e\u3069\u3046\u3057\u3088\u3046","album_art":"http:\/\/25.media.tumblr.com\/tumblr_mnz6bkviyS1s85v8so1_1370528192_cover.jpg","caption":"","player":"\u003Cembed type=\u0022application\/x-shockwave-flash\u0022 src=\u0022http:\/\/assets.tumblr.com\/swf\/audio_player.swf?audio_file=http%3A%2F%2Fwww.tumblr.com%2Faudio_file%2Fanisample%2F52300690971%2Ftumblr_mnz6bkviyS1s85v8s&color=FFFFFF\u0022 height=\u002227\u0022 width=\u0022207\u0022 quality=\u0022best\u0022 wmode=\u0022opaque\u0022\u003E\u003C\/embed\u003E","embed":"\u003Ciframe class=\u0022tumblr_audio_player tumblr_audio_player_52300690971\u0022 src=\u0022http:\/\/anisample.tumblr.com\/post\/52300690971\/audio_player_iframe\/anisample\/tumblr_mnz6bkviyS1s85v8s?audio_file=http%3A%2F
         */
        fun getAppropriateSizePhotoObject(photoInfo: JSONObject): Photo? {
            var photo: Photo? = null
            try {
                val arr = photoInfo.getJSONArray("alt_sizes")
                val len = arr.length()
                for (i in 0 until len) {
                    val o = arr.getJSONObject(i)
                    val w = o.getInt("width")
                    val h = o.getInt("height")
                    val u = o.getString("url")
                    val p = Photo(w, h, u)
                    if (photo == null) {
                        photo = p
                    } else {
                        if (p.width <= 400) {
                            if (photo.width < p.width) {
                                photo = p
                            }
                        } else {
                            if (p.width < photo.width) {
                                photo = p
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            return photo
        }
    }

}

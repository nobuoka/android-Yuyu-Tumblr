package info.vividcode.android.app.yuyutumblr

import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import info.vividcode.android.app.yuyutumblr.ui.AndroidMainView
import info.vividcode.android.app.yuyutumblr.ui.PostAdapter
import info.vividcode.android.app.yuyutumblr.ui.BitmapCache
import info.vividcode.android.app.yuyutumblr.usecase.MainApplication
import info.vividcode.android.app.yuyutumblr.usecase.MainView
import info.vividcode.android.app.yuyutumblr.web.TumblrWebApi

class MainActivity : AppCompatActivity() {

    private var mRequestQueue: RequestQueue? = null

    private val mainApplication: MainApplication by lazy {
        MainApplication(createMainView(), TumblrWebApi(requireNotNull(mRequestQueue)))
    }

    private fun createMainView(): MainView {
        // スクロール限界までスクロールしてさらに引っ張ると続きを読み込む仕組み
        val recyclerView = findViewById<View>(R.id.posts_view) as androidx.recyclerview.widget.RecyclerView
        val imageLoader = ImageLoader(mRequestQueue, BitmapCache())
        val postAdapter = PostAdapter(imageLoader)

        val swipeRefreshLayout = findViewById<View>(R.id.swipe_refresh_layout) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout

        return AndroidMainView.create(this, recyclerView, swipeRefreshLayout, postAdapter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRequestQueue = Volley.newRequestQueue(this)
        mainApplication.init()
    }

    override fun onRestart() {
        super.onRestart()
        mRequestQueue!!.start() // start 時に呼び出さないのは onCreate で start されるため
    }

    override fun onResume() {
        super.onResume()

        mainApplication.updatePosts()

        // 更新ボタンは使えないようにしておく (特に意味はない)
        val updateButton = findViewById<View>(R.id.update_posts_button) as Button
        updateButton.setOnClickListener {
            Log.d("click", "clicked")
            mainApplication.updatePosts()
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

}

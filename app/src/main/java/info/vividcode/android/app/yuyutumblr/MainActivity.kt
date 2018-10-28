package info.vividcode.android.app.yuyutumblr

import com.android.volley.toolbox.ImageLoader

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.RequestQueue
import info.vividcode.android.app.yuyutumblr.ui.AndroidMainView
import info.vividcode.android.app.yuyutumblr.ui.PostAdapter
import info.vividcode.android.app.yuyutumblr.ui.BitmapCache
import info.vividcode.android.app.yuyutumblr.usecase.MainApplication
import info.vividcode.android.app.yuyutumblr.usecase.MainView
import info.vividcode.android.app.yuyutumblr.utils.RequestQueueLifecycleAwareContainer
import info.vividcode.android.app.yuyutumblr.web.TumblrWebApi

class MainActivity : AppCompatActivity() {

    private lateinit var mainApplication: MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val requestQueueContainer = RequestQueueLifecycleAwareContainer(this)
        lifecycle.addObserver(requestQueueContainer)

        val requestQueue = requestQueueContainer.requestQueue
        mainApplication = MainApplication(createMainView(requestQueue), TumblrWebApi(requestQueue))
        mainApplication.init()
    }

    override fun onResume() {
        super.onResume()
        mainApplication.updatePosts()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun createMainView(requestQueue: RequestQueue): MainView {
        // スクロール限界までスクロールしてさらに引っ張ると続きを読み込む仕組み
        val recyclerView = findViewById<View>(R.id.posts_view) as RecyclerView
        val imageLoader = ImageLoader(requestQueue, BitmapCache())
        val postAdapter = PostAdapter(imageLoader)

        val swipeRefreshLayout = findViewById<View>(R.id.swipe_refresh_layout) as SwipeRefreshLayout

        return AndroidMainView.create(this, recyclerView, swipeRefreshLayout, postAdapter)
    }

}

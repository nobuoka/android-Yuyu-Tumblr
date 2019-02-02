package info.vividcode.android.app.yuyutumblr.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import info.vividcode.android.app.yuyu.ui.R
import info.vividcode.android.app.yuyutumblr.usecase.MainApplication
import info.vividcode.android.app.yuyutumblr.usecase.MainView

class AndroidMainView private constructor(
        private val recyclerView: androidx.recyclerview.widget.RecyclerView,
        private val swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout,
        private val adapter: PostAdapter
) : MainView {

    private var userInputEventListener: MainView.UserInputEventListener? = null

    companion object {
        fun setupActivity(activity: Activity, requestQueue: RequestQueue): MainView {
            activity.setContentView(R.layout.activity_main)

            // スクロール限界までスクロールしてさらに引っ張ると続きを読み込む仕組み
            val recyclerView = activity.findViewById<View>(R.id.posts_view) as RecyclerView
            val imageLoader = ImageLoader(requestQueue, BitmapCache())
            val postAdapter = PostAdapter(imageLoader)

            val swipeRefreshLayout = activity.findViewById<View>(R.id.swipe_refresh_layout) as SwipeRefreshLayout

            return AndroidMainView(recyclerView, swipeRefreshLayout, postAdapter).also {
                it.initialize(activity)
            }
        }
    }

    private fun initialize(context: Context) {
        // use a linear layout manager
        val layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        // specify an adapter (see also next example)
        recyclerView.adapter = adapter

        swipeRefreshLayout.setColorSchemeColors(Color.RED)
        swipeRefreshLayout.setOnRefreshListener { userInputEventListener?.onRefreshRequest() }
    }

    override fun setUserInputEventListener(listener: MainView.UserInputEventListener) {
        userInputEventListener = listener
    }

    override fun unsetUserInputEventListener() {
        userInputEventListener = null
    }

    override fun stopRefreshingIndicator() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun bindMainApplication(mainApplication: MainApplication) {
        adapter.bindModel(mainApplication.photoTimeline, mainApplication.nextPageLoaderState, mainApplication::requestNextPage)
    }

    override fun unbindMainApplication() {
        adapter.unbindModel()
    }

}

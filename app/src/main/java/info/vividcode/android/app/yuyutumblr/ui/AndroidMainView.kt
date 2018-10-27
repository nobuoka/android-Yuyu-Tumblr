package info.vividcode.android.app.yuyutumblr.ui

import android.content.Context
import android.graphics.Color
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import info.vividcode.android.app.yuyutumblr.MainActivity
import org.json.JSONObject

class AndroidMainView private constructor(
        private val recyclerView: RecyclerView,
        private val swipeRefreshLayout: SwipeRefreshLayout,
        private val adapter: PostAdapter
) : MainActivity.MainView {

    private var internalRefreshEventListener: (() -> Unit)? = null

    companion object {
        fun create(
                context: Context,
                recyclerView: RecyclerView,
                swipeRefreshLayout: SwipeRefreshLayout,
                adapter: PostAdapter
        ) = AndroidMainView(recyclerView, swipeRefreshLayout, adapter).also {
            it.initialize(context)
        }
    }

    private fun initialize(context: Context) {
        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        // specify an adapter (see also next example)
        recyclerView.adapter = adapter

        swipeRefreshLayout.setColorSchemeColors(Color.RED)
        swipeRefreshLayout.setOnRefreshListener { internalRefreshEventListener?.invoke() }
    }

    override fun setRefreshEventListener(listener: () -> Unit) {
        internalRefreshEventListener = listener
    }

    override fun stopRefreshingIndicator() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun addPosts(posts: List<JSONObject>) {
        adapter.add(posts)
    }

    override fun getLatestPost(): JSONObject? = adapter.lastItem

}

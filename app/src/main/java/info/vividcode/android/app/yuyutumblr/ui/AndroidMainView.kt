package info.vividcode.android.app.yuyutumblr.ui

import android.content.Context
import android.graphics.Color
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import info.vividcode.android.app.yuyutumblr.usecase.MainView
import org.json.JSONObject

class AndroidMainView private constructor(
        private val recyclerView: androidx.recyclerview.widget.RecyclerView,
        private val swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout,
        private val adapter: PostAdapter
) : MainView {

    private var internalRefreshEventListener: (() -> Unit)? = null

    companion object {
        fun create(
                context: Context,
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout,
                adapter: PostAdapter
        ) = AndroidMainView(recyclerView, swipeRefreshLayout, adapter).also {
            it.initialize(context)
        }
    }

    private fun initialize(context: Context) {
        // use a linear layout manager
        val layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
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

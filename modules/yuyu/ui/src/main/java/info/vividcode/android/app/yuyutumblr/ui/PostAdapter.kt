package info.vividcode.android.app.yuyutumblr.ui

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import info.vividcode.android.app.yuyu.ui.R
import info.vividcode.android.app.yuyutumblr.usecase.MainApplication
import info.vividcode.android.app.yuyutumblr.usecase.NextPageLoaderStateHolder
import info.vividcode.android.app.yuyutumblr.usecase.PhotoTimeline
import info.vividcode.android.app.yuyutumblr.usecase.TumblrPost

class PostAdapter(
        private val mImageLoader: ImageLoader
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private val mainHandler = Handler(Looper.getMainLooper())

    private var photoTimeline: PhotoTimeline? = null
    private var nextPageLoaderStateHolder: NextPageLoaderStateHolder? = null
    private var nextPageLoadRequester: (() -> Unit)? = null

    private val photoTimelineEventListener: (PhotoTimeline.ChangeEvent) -> Unit = {
        when (it) {
            is PhotoTimeline.ChangeEvent.ItemsAdded -> {
                this.notifyItemRangeInserted(it.startPosition, it.itemCount)
            }
        } as? Unit?
    }

    private val nextPageLoaderStateEventListener: (NextPageLoaderStateHolder.ChangeEvent) -> Unit = {
        when (it) {
            is NextPageLoaderStateHolder.ChangeEvent.Update -> {
                val currentVisible = hasNextPageLoader(it.currentState)
                val visibilityChanged = hasNextPageLoader(it.previousState) != currentVisible
                if (currentVisible) {
                    val currentPosition = itemCount - 1
                    if (visibilityChanged) {
                        this.notifyItemInserted(currentPosition)
                    } else {
                        this.notifyItemChanged(currentPosition)
                    }
                } else {
                    val previousPosition = itemCount
                    if (visibilityChanged) {
                        this.notifyItemRemoved(previousPosition)
                    }
                    Unit
                }
            }
        } as? Unit?
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        interface Factory<T : ViewHolder> {
            fun create(parent: ViewGroup): T
        }

        private class FactoryImpl<T : ViewHolder>(
                @LayoutRes private val layoutId: Int,
                private val constructor: (View) -> T
        ) : Factory<T> {
            override fun create(parent: ViewGroup): T =
                    LayoutInflater.from(parent.context).inflate(layoutId, parent, false).let(constructor)
        }

        class PhotoPost(view: View) : ViewHolder(view)
        class NextPageIndicator(view: View) : ViewHolder(view) {
            companion object : Factory<NextPageIndicator>
            by FactoryImpl(R.layout.next_page_indicator, ::NextPageIndicator)

            fun setState(state: NextPageLoaderStateHolder.State) {
                listOf(
                        itemView.findViewById<View>(R.id.nextPageIndicatorIdle),
                        itemView.findViewById<View>(R.id.nextPageIndicatorProgress),
                        itemView.findViewById<View>(R.id.nextPageIndicatorNoNextPage),
                        itemView.findViewById<View>(R.id.nextPageIndicatorError)
                ).forEach { it.visibility = View.INVISIBLE }
                val id = when (state) {
                    NextPageLoaderStateHolder.State.NoNextPage -> R.id.nextPageIndicatorNoNextPage
                    NextPageLoaderStateHolder.State.Idle -> R.id.nextPageIndicatorIdle
                    NextPageLoaderStateHolder.State.Progress -> R.id.nextPageIndicatorProgress
                    is NextPageLoaderStateHolder.State.Error -> R.id.nextPageIndicatorError
                }
                itemView.findViewById<View>(id).visibility = View.VISIBLE
            }
        }
    }

    fun bindModel(photoTimeline: PhotoTimeline, nextPageLoaderStateHolder: NextPageLoaderStateHolder, nextPageLoadRequester: () -> Unit) {
        this.photoTimeline = photoTimeline
        this.nextPageLoaderStateHolder = nextPageLoaderStateHolder
        this.nextPageLoadRequester = nextPageLoadRequester
        photoTimeline.addChangeEventListener(photoTimelineEventListener)
        nextPageLoaderStateHolder.addChangeEventListener(nextPageLoaderStateEventListener)
    }

    fun unbindModel() {
        nextPageLoaderStateHolder?.removeChangeEventListener(nextPageLoaderStateEventListener)
        photoTimeline?.removeChangeEventListener(photoTimelineEventListener)
        this.nextPageLoadRequester = null
        this.nextPageLoaderStateHolder = null
        this.photoTimeline = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            when (viewType) {
                viewTypePhotoPost -> {
                    val v = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
                    ViewHolder.PhotoPost(v)
                }
                viewTypeNextPageLoader -> {
                    ViewHolder.NextPageIndicator.create(parent)
                }
                else -> throw RuntimeException("Unknown view type ($viewType)")
            }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.PhotoPost -> {
                // - get element from your dataset at this position
                // - replace the contents of the view with that element
                val post = photoTimeline?.getPhoto(position) ?: return
                if (post is TumblrPost.Photo) {
                    val p = MainApplication.getAppropriateSizePhotoObject(post.photos.first())
                    //String url = .getJSONObject("original_size").getString("url");

                    val v = holder.itemView.findViewById<View>(R.id.image) as NetworkImageView
                    v.setImageUrl(p!!.url, mImageLoader)
                    v.minimumHeight = p.height
                    v.minimumWidth = 36
                    v.maxHeight = p.height
                    v.maxWidth = p.width

                    v.setDefaultImageResId(android.R.drawable.ic_menu_rotate)
                    v.setErrorImageResId(android.R.drawable.ic_delete)

                    //FrameLayout fl = (FrameLayout) convertView.findViewById(R.id.image_container);
                    //fl.removeAllViews();
                    //fl.addView(v);
                }
                Unit
            }
            is ViewHolder.NextPageIndicator -> {
                nextPageLoaderStateHolder?.let {
                    holder.setState(it.state)
                    if (it.state is NextPageLoaderStateHolder.State.Idle) {
                        mainHandler.post { nextPageLoadRequester?.invoke() }
                    }
                }
                Unit
            }
        } as? Unit?
    }

    override fun getItemViewType(position: Int): Int =
            if (hasNextPageLoader() && position == itemCount - 1) {
                viewTypeNextPageLoader
            } else {
                viewTypePhotoPost
            }

    override fun getItemCount(): Int {
        return (photoTimeline?.size ?: 0) + (if (hasNextPageLoader()) 1 else 0)
    }

    private fun hasNextPageLoader(): Boolean = hasNextPageLoader(nextPageLoaderStateHolder?.state)

    companion object {
        private const val viewTypePhotoPost = 1
        private const val viewTypeNextPageLoader = 2

        private fun hasNextPageLoader(state: NextPageLoaderStateHolder.State?): Boolean = when (state) {
            NextPageLoaderStateHolder.State.NoNextPage -> false
            NextPageLoaderStateHolder.State.Idle -> true
            NextPageLoaderStateHolder.State.Progress -> true
            is NextPageLoaderStateHolder.State.Error -> true
            null -> false
        }
    }

}

package info.vividcode.android.app.yuyutumblr.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import info.vividcode.android.app.yuyu.ui.R
import info.vividcode.android.app.yuyutumblr.usecase.MainApplication
import info.vividcode.android.app.yuyutumblr.usecase.PhotoTimeline
import org.json.JSONException

class PostAdapter(
        private val mImageLoader: ImageLoader
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var photoTimeline: PhotoTimeline? = null

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun bindPhotoTimeline(photoTimeline: PhotoTimeline) {
        this.photoTimeline = photoTimeline
        photoTimeline.addChangeEventListener {
            when (it) {
                is PhotoTimeline.ChangeEvent.ItemsAdded -> {
                    this.notifyItemRangeInserted(it.startPosition, it.itemCount)
                }
            } as? Unit?
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        // set the view's size, margins, paddings and layout parameters...
        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        try {
            val post = photoTimeline?.getPhoto(position) ?: return
            if (post.type == "photo") {
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
        } catch (e: JSONException) {
            Log.d("error", "error", e)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return photoTimeline?.size ?: 0
    }

}

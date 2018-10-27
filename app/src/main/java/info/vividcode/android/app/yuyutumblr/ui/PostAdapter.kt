package info.vividcode.android.app.yuyutumblr.ui

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import info.vividcode.android.app.yuyutumblr.R
import info.vividcode.android.app.yuyutumblr.usecase.MainApplication
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class PostAdapter// Provide a suitable constructor (depends on the kind of dataset)
(private val mImageLoader: ImageLoader) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private val mList: MutableList<JSONObject>

    val lastItem: JSONObject?
        get() = if (mList.size == 0) null else mList[mList.size - 1]

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(// each data item is just a string in this case
            var mView: View) : RecyclerView.ViewHolder(mView)

    init {
        mList = ArrayList()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): PostAdapter.ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.post, parent, false)
        // set the view's size, margins, paddings and layout parameters...
        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        try {
            val post = mList[position]
            if (post.getString("type") == "photo") {
                val p = MainApplication.getAppropriateSizePhotoObject(post.getJSONArray("photos").getJSONObject(0))
                //String url = .getJSONObject("original_size").getString("url");

                val v = holder.mView.findViewById<View>(R.id.image) as NetworkImageView
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
        return mList.size
    }

    fun add(items: List<JSONObject>) {
        mList.addAll(items)
        notifyDataSetChanged()
    }

}

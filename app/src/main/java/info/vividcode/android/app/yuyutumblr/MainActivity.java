package info.vividcode.android.app.yuyutumblr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static class BitmapCache implements ImageCache {

        private LruCache<String, Bitmap> mCache;

        public BitmapCache() {
            int maxSize = 5 * 1024 * 1024;
            mCache = new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            mCache.put(url, bitmap);
        }

    }

    private static class Photo {
        public final int width;
        public final int height;
        public final String url;
        public Photo(int w, int h, String u) {
            width = w;
            height = h;
            url = u;
        }
    }

    /**
     * response":[
     *   {
     *     "blog_name":"fileth",
     *     "id":52302908168,
     *     "post_url":"http:\/\/fileth.tumblr.com\/post\/52302908168\/on-twitpic",
     *     "slug":"on-twitpic",
     *     "type":"photo",
     *     "date":"2013-06-06 15:05:00 GMT",
     *     "timestamp":1370531100,
     *     "state":"published",
     *     "format":"html",
     *     "reblog_key":"m3GAoQbz",
     *     "tags":["\u3086\u3086\u5f0f","\u677e\u672c\u983c\u5b50","yuyushiki","yoriko matsumoto"],
     *     "short_url":"http:\/\/tmblr.co\/Z1qtXymjVmi8",
     *     "highlighted":[],
     *     "note_count":3,
     *     "source_url":"http:\/\/twitpic.com\/cvmf4w\/full",
     *     "source_title":"twitpic.com",
     *     "caption":"\u003Cp\u003E\u003Ca href=\u0022http:\/\/twitpic.com\/cvmf4w\/full\u0022 target=\u0022_blank\u0022\u003E\u304a\u6bcd\u3055\u3093\u5148\u751f on Twitpic\u003C\/a\u003E\u003C\/p\u003E",
     *     "link_url":"http:\/\/twitpic.com\/cvmf4w\/full",
     *     "image_permalink":"http:\/\/fileth.tumblr.com\/image\/52302908168",
     *     "photos":[
     *       {
     *         "caption":"",
     *         "alt_sizes":[
     *           {"width":400,"height":600,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_400.jpg"},
     *           {"width":250,"height":375,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_250.jpg"},
     *           {"width":100,"height":150,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_100.jpg"},
     *           {"width":75,"height":75,"url":"http:\/\/25.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_75sq.jpg"}
     *         ],
     *         "original_size":{"width":400,"height":600,"url":"http:\/\/24.media.tumblr.com\/af8c96c9b5fe5c98df62587c2aacdef9\/tumblr_mnz8leENUq1qze9qao1_400.jpg"}
     *       }
     *     ]
     *   },
     *   {"blog_name":"anisample","id":52300690971,"post_url":"http:\/\/anisample.tumblr.com\/post\/52300690971","slug":"","type":"audio","date":"2013-06-06 14:16:32 GMT","timestamp":1370528192,"state":"published","format":"html","reblog_key":"yIm7xUJf","tags":["\u3086\u3086\u5f0f","\u91ce\u3005\u539f\u3086\u305a\u3053","\u5927\u4e45\u4fdd\u7460\u7f8e"],"short_url":"http:\/\/tmblr.co\/Zj_7osmjNJOR","highlighted":[],"note_count":7,"artist":"\u91ce\u3005\u539f \u3086\u305a\u3053","album":"\u3086\u3086\u5f0f","track_name":"\u3053\u306e\u4e0a\u304c\u3063\u305f\u306e\u3069\u3046\u3057\u3088\u3046","album_art":"http:\/\/25.media.tumblr.com\/tumblr_mnz6bkviyS1s85v8so1_1370528192_cover.jpg","caption":"","player":"\u003Cembed type=\u0022application\/x-shockwave-flash\u0022 src=\u0022http:\/\/assets.tumblr.com\/swf\/audio_player.swf?audio_file=http%3A%2F%2Fwww.tumblr.com%2Faudio_file%2Fanisample%2F52300690971%2Ftumblr_mnz6bkviyS1s85v8s&color=FFFFFF\u0022 height=\u002227\u0022 width=\u0022207\u0022 quality=\u0022best\u0022 wmode=\u0022opaque\u0022\u003E\u003C\/embed\u003E","embed":"\u003Ciframe class=\u0022tumblr_audio_player tumblr_audio_player_52300690971\u0022 src=\u0022http:\/\/anisample.tumblr.com\/post\/52300690971\/audio_player_iframe\/anisample\/tumblr_mnz6bkviyS1s85v8s?audio_file=http%3A%2F
     */
    private static Photo getAppropriateSizePhotoObject(JSONObject photoInfo) {
        Photo photo = null;
        try {
            JSONArray arr = photoInfo.getJSONArray("alt_sizes");
            int len = arr.length();
            for (int i = 0; i < len; ++i) {
                JSONObject o = arr.getJSONObject(i);
                int w = o.getInt("width");
                int h = o.getInt("height");
                String u = o.getString("url");
                Photo p = new Photo(w, h, u);
                if (photo == null) {
                    photo = p;
                } else {
                    if (p.width <= 400) {
                        if (photo.width < p.width) {
                            photo = p;
                        }
                    } else {
                        if (p.width < photo.width) {
                            photo = p;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return photo;
    }

    public static class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
        private List<JSONObject> mList;
        private final ImageLoader mImageLoader;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public View mView;
            public ViewHolder(View v) {
                super(v);
                mView = v;
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public PostAdapter(ImageLoader imageLoader) {
            mList = new ArrayList<JSONObject>();
            mImageLoader = imageLoader;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post, parent, false);
            // set the view's size, margins, paddings and layout parameters...
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            try {
                JSONObject post = mList.get(position);
                if (post.getString("type").equals("photo")) {
                    Photo p = getAppropriateSizePhotoObject(post.getJSONArray("photos").getJSONObject(0));
                    //String url = .getJSONObject("original_size").getString("url");

                    NetworkImageView v = (NetworkImageView) holder.mView.findViewById(R.id.image);
                    v.setImageUrl(p.url, mImageLoader);
                    v.setMinimumHeight(p.height);
                    v.setMinimumWidth(36);
                    v.setMaxHeight(p.height);
                    v.setMaxWidth(p.width);

                    v.setDefaultImageResId(android.R.drawable.ic_menu_rotate);
                    v.setErrorImageResId(android.R.drawable.ic_delete);

                    //FrameLayout fl = (FrameLayout) convertView.findViewById(R.id.image_container);
                    //fl.removeAllViews();
                    //fl.addView(v);
                }
            } catch (JSONException e) {
                Log.d("error", "error", e);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mList.size();
        }

        public void add(List<JSONObject> items) {
            mList.addAll(items);
            notifyDataSetChanged();
        }

        public JSONObject getLastItem() {
            if (mList.size() == 0) return null;
            return mList.get(mList.size() - 1);
        }
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapCache());

        // スクロール限界までスクロールしてさらに引っ張ると続きを読み込む仕組み
        mRecyclerView = (RecyclerView) findViewById(R.id.posts_view);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new PostAdapter(mImageLoader);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeColors(Color.RED);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updatePosts();
            }
        });
    }

    @Override
    protected void onRestart() {
        mRequestQueue.start(); // start 時に呼び出さないのは onCreate で start されるため
    }

    @Override
    protected void onResume() {
        super.onResume();

        updatePosts();

        // 更新ボタンは使えないようにしておく (特に意味はない)
        Button updateButton = (Button) findViewById(R.id.update_posts_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.d("click", "clicked");
                updatePosts();
            }
        });
        updateButton.setEnabled(false);
    }

    @Override
    protected void onStop() {
        mRequestQueue.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private boolean mUpdating = false;
    private void updatePosts() {
        // 引っ張って更新時は何回もこのメソッドが呼ばれるので, 更新処理中は何もしない
        if (mUpdating) return;
        mUpdating = true;
        // API キーは Tumblr のドキュメントにのってたやつ。 ほんとは各自取得する必要がある?
        // http://www.tumblr.com/docs/en/api/v2#tagged-method
        String uri = "http://api.tumblr.com/v2/tagged?tag=%E3%82%86%E3%82%86%E5%BC%8F" +
                "&api_key=fuiKNFp9vQFvjLNvx4sUwti4Yb5yGutBN4Xh10LXZhhRKjWlV4";

        JSONObject post = mAdapter.getLastItem();
        if (post != null) {
            try {
                int lastTimestamp = post.getInt("timestamp");
                uri += "&before=" + lastTimestamp;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // リクエスト生成
        JsonObjectRequest req = new JsonObjectRequest(uri, null, new Response.Listener<JSONObject>() {
            @Override public void onResponse(JSONObject response) {
                mUpdating = false;
                mSwipeRefreshLayout.setRefreshing(false);
                try {
                    JSONArray posts = response.getJSONArray("response");
                    List<JSONObject> pp = new ArrayList<JSONObject>();
                    int length = posts.length();
                    for (int i = 0; i < length; ++i) {
                        pp.add(posts.getJSONObject(i));
                    }
                    mAdapter.add(pp);
                } catch (JSONException err) {
                    Log.d("res", "error", err);
                }
            }
        }, new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
                mUpdating = false;
                mSwipeRefreshLayout.setRefreshing(false);
                Log.d("res", "error", error);
            }
        });
        // リクエストをキューに追加
        mRequestQueue.add(req);
    }

}

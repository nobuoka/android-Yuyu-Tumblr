package info.vividcode.android.app.yuyutumblr.ui

import android.graphics.Bitmap
import android.support.v4.util.LruCache
import com.android.volley.toolbox.ImageLoader

class BitmapCache : ImageLoader.ImageCache {

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

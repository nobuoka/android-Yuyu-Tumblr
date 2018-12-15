package info.vividcode.android.app.yuyutumblr.usecase

sealed class TumblrPost {

    abstract val timestamp: Int

    data class Photo(
            override val timestamp: Int,
            val photos: List<TumblrPhotoInfo>
    ) : TumblrPost()

    data class Common(
            val type: String,
            override val timestamp: Int
    ) : TumblrPost()

}

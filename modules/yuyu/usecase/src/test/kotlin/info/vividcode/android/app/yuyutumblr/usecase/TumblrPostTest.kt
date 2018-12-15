package info.vividcode.android.app.yuyutumblr.usecase

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TumblrPostTest {

    @Nested
    internal inner class PhotoTest {
        @Test
        fun properties() {
            val timestamp = 100
            val photos = listOf(TumblrPhotoInfo(listOf(
                    Photo(width = 100, height = 200, url = "http://example.com/image-1.jpg")
            )))

            val post = TumblrPost.Photo(timestamp, photos)

            assertEquals(timestamp, post.timestamp)
            assertEquals(photos, post.photos)
        }
    }

    @Nested
    internal inner class CommonTest {
        @Test
        fun properties() {
            val type = "text"
            val timestamp = 100

            val post = TumblrPost.Common(type, timestamp)

            assertEquals(type, post.type)
            assertEquals(timestamp, post.timestamp)
        }
    }

}

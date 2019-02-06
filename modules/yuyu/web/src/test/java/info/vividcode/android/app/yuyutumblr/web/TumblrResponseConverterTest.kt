package info.vividcode.android.app.yuyutumblr.web

import info.vividcode.android.app.yuyutumblr.usecase.Photo
import info.vividcode.android.app.yuyutumblr.usecase.TumblrPost
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test

internal class TumblrResponseConverterTest {

    @Test
    internal fun convertTumblrPostsResponse() {
        val inputJsonString = """{ "response": [
                            |  {
                            |    "timestamp": 1370531100,
                            |    "type": "photo",
                            |    "photos": [
                            |      {
                            |        "caption": "",
                            |        "alt_sizes": [
                            |          {"width":400,"height":600,"url":"http://example.com/af8c96/tumblr_mnz8le_400.jpg"},
                            |          {"width":250,"height":375,"url":"http://example.com/af8c96/tumblr_mnz8le_250.jpg"}
                            |        ],
                            |        "original_size": {"width":400,"height":600,"url":"http://example.com/af8c96/tumblr_mnz8le_400.jpg"}
                            |      }
                            |    ]
                            |  }
                            |] }""".trimMargin()

        val tumblrPosts = TumblrResponseConverter.convertTumblrPostsResponse(JSONObject(inputJsonString))

        assertThat(tumblrPosts.size, equalTo(1))

        val post = tumblrPosts[0]
        assertThat(post, instanceOf(TumblrPost.Photo::class.java))
        assertThat(post.timestamp, equalTo(1370531100))

        val photos = (post as TumblrPost.Photo).photos
        assertThat(photos.size, equalTo(1))

        val photoAltSizes = photos[0].altSizes
        assertThat(photoAltSizes, equalTo(listOf(
                Photo(400, 600, "http://example.com/af8c96/tumblr_mnz8le_400.jpg"),
                Photo(250, 375, "http://example.com/af8c96/tumblr_mnz8le_250.jpg")
        )))
    }

}

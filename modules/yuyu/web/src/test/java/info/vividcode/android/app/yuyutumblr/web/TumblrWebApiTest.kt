package info.vividcode.android.app.yuyutumblr.web

import info.vividcode.android.app.yuyutumblr.usecase.TumblrApi
import info.vividcode.android.app.yuyutumblr.usecase.TumblrPost
import info.vividcode.android.app.yuyutumblr.web.test.WebServerExtension
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TumblrWebApiTest {

    @JvmField
    @RegisterExtension
    val webServerExtension = WebServerExtension()

    object SimpleCallbackExecutor : TumblrWebApi.CallbackExecutor {
        override fun <T> execute(value: T, execution: (T) -> Unit) {
            execution(value)
        }
    }

    @Test
    internal fun normal() {
        val client = OkHttpClient()
        val tumblrApi = TumblrWebApi(
                "test-api-key", client, SimpleCallbackExecutor,
                webServerExtension.server.url("/").toString().removeSuffix("/")
        )

        webServerExtension.server.enqueue(
                MockResponse().setResponseCode(200)
                        .setHeader("Content-Type", "application/json; charset=utf-8")
                        .setBody(this::class.java.getResource("/tumblr-tagged-success-response.json").readText())
        )

        val actualResultFuture = CompletableFuture<TumblrApi.Result<List<TumblrPost>>>()
        tumblrApi.fetchPosts(null) { actualResultFuture.complete(it) }

        val actualResult = actualResultFuture.get(1, TimeUnit.SECONDS)
        assertTrue(actualResult is TumblrApi.Result.Success, "Not success result : $actualResult")
        assertEquals(3, actualResult.responseContent.size)

        val actualRequest = webServerExtension.server.takeRequest()
        assertEquals(
                "GET /v2/tagged?tag=%E3%82%86%E3%82%86%E5%BC%8F&api_key=test-api-key HTTP/1.1",
                actualRequest.requestLine
        )
    }

}

package info.vividcode.android.app.yuyutumblr.web.test

import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class WebServerExtension : BeforeEachCallback, AfterEachCallback {

    val server = MockWebServer()

    override fun beforeEach(context: ExtensionContext?) {
        server.start()
    }

    override fun afterEach(context: ExtensionContext?) {
        server.shutdown()
    }

}

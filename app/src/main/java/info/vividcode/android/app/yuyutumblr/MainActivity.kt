package info.vividcode.android.app.yuyutumblr

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import info.vividcode.android.app.yuyutumblr.ui.AndroidMainView
import info.vividcode.android.app.yuyutumblr.usecase.MainApplication
import info.vividcode.android.app.yuyutumblr.utils.RequestQueueLifecycleAwareContainer
import info.vividcode.android.app.yuyutumblr.utils.getRetainContainer
import info.vividcode.android.app.yuyutumblr.web.TumblrWebApi
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    private lateinit var mainApplication: MainApplication

    private val callbackExecutor: TumblrWebApi.CallbackExecutor = object : TumblrWebApi.CallbackExecutor {
        override fun <T> execute(value: T, execution: (T) -> Unit) {
            runOnUiThread { execution(value) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val retainContainer = supportFragmentManager.getRetainContainer("test")
        val mainApplicationRetainLifecycleContainer = retainContainer.getOrCreate {
            val tumblrApi = TumblrWebApi(OkHttpClient.Builder().build(), callbackExecutor)
            MainApplication.RetainLifecycleScope(tumblrApi)
        }

        val requestQueueContainer = RequestQueueLifecycleAwareContainer(this)
        lifecycle.addObserver(requestQueueContainer)

        val requestQueue = requestQueueContainer.requestQueue
        val mainView = AndroidMainView.setupActivity(this, requestQueue)

        mainApplication = MainApplication(mainView, mainApplicationRetainLifecycleContainer)
    }

    override fun onStart() {
        super.onStart()
        mainApplication.activate()
        mainApplication.requestInitialLoadIfNeeded()
    }

    override fun onStop() {
        mainApplication.deactivate()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

}

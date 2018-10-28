package info.vividcode.android.app.yuyutumblr

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import info.vividcode.android.app.yuyutumblr.ui.AndroidMainView
import info.vividcode.android.app.yuyutumblr.usecase.MainApplication
import info.vividcode.android.app.yuyutumblr.utils.RequestQueueLifecycleAwareContainer
import info.vividcode.android.app.yuyutumblr.web.TumblrWebApi

class MainActivity : AppCompatActivity() {

    private lateinit var mainApplication: MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestQueueContainer = RequestQueueLifecycleAwareContainer(this)
        lifecycle.addObserver(requestQueueContainer)

        val requestQueue = requestQueueContainer.requestQueue
        val mainView = AndroidMainView.setupActivity(this, requestQueue)
        val tumblrApi = TumblrWebApi(requestQueue)

        mainApplication = MainApplication(mainView, tumblrApi)
        mainApplication.init()
    }

    override fun onResume() {
        super.onResume()
        mainApplication.updatePosts()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

}

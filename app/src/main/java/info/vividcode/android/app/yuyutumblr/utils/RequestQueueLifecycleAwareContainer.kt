package info.vividcode.android.app.yuyutumblr.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class RequestQueueLifecycleAwareContainer(activity: AppCompatActivity) : LifecycleObserver {

    val requestQueue: RequestQueue = Volley.newRequestQueue(activity)

    private var stopped = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (stopped) {
            requestQueue.start()
            stopped = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        requestQueue.stop()
        stopped = true
    }

}

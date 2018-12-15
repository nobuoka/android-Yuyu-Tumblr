package info.vividcode.android.app.yuyutumblr.utils

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import android.os.Bundle
import androidx.fragment.app.FragmentManager


class RetainContainer : Fragment() {

    private val mutableMap = mutableMapOf<String, Any>()

    @MainThread
    fun <T : Any> getOrCreate(key: String, klass: Class<T>, generator: () -> T): T =
            mutableMap[key]?.let { if (klass.isInstance(it)) klass.cast(it) else null } ?:
            generator().also { mutableMap[key] = it }

    @MainThread
    inline fun <reified T : Any> getOrCreate(noinline generator: () -> T): T =
            getOrCreate(T::class.java.simpleName, T::class.java, generator)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    companion object {
        const val tagPrefix = "retain-container-"
    }

}

fun FragmentManager.getRetainContainer(retainContainerTag: String): RetainContainer =
        findFragmentByTag(RetainContainer.tagPrefix + retainContainerTag) as RetainContainer? ?:
        RetainContainer().also { retainContainer ->
            beginTransaction().add(retainContainer, RetainContainer.tagPrefix + retainContainerTag).commit()
        }

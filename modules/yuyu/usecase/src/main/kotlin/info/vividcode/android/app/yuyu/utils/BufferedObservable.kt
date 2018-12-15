package info.vividcode.android.app.yuyu.utils

interface BufferedObservable<T> {

    val isActive: Boolean

    fun setObserver(observer: Observer<T>)

    fun unsetObserver()

}

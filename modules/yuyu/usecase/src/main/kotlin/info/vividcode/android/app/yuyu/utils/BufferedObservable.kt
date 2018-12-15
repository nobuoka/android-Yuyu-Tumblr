package info.vividcode.android.app.yuyu.utils

interface BufferedObservable<T> {

    fun setObserver(observer: Observer<T>)

    fun unsetObserver()

}

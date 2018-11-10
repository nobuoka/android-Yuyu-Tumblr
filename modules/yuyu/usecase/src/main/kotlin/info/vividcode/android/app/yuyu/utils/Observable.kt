package info.vividcode.android.app.yuyu.utils

interface Observable<T> {

    fun connect(observer: Observer<T>)

    fun disconnectAll()

}

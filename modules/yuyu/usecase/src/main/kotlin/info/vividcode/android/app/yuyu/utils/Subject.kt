package info.vividcode.android.app.yuyu.utils

class Subject<T> : BufferedObservable<T>, Observer<T> {

    private var observer: Observer<T>? = null

    private val buffer: MutableList<T> = mutableListOf()

    override operator fun invoke(response: T): Unit = synchronized(this) {
        observer?.also { it.invoke(response) } ?: buffer.add(response)
    }

    override fun setObserver(observer: Observer<T>): Unit = synchronized(this) {
        this.observer = observer
        buffer.forEach(observer)
        buffer.clear()
    }

    override fun unsetObserver(): Unit = synchronized(this) {
        this.observer = null
    }

}

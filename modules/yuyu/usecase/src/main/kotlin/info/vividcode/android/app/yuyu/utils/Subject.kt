package info.vividcode.android.app.yuyu.utils

class Subject<T> : Observable<T>, Observer<T> {

    private val observers = mutableListOf<Observer<T>>()

    private val observersSnapshot get() = observers.toList()

    override operator fun invoke(response: T) {
        observersSnapshot.forEach { it(response) }
    }

    override fun connect(observer: Observer<T>) {
        observers.add(observer)
    }

    override fun disconnectAll() {
        observers.clear()
    }

}

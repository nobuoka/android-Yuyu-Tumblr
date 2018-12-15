package info.vividcode.android.app.yuyutumblr.usecase

class PhotoTimeline {

    private val list = mutableListOf<TumblrPost>()

    private val changeEventListeners = mutableListOf<(ChangeEvent) -> Unit>()

    val size: Int get() = list.size

    val lastItem: TumblrPost? get() = list.lastOrNull()

    fun getPhoto(index: Int): TumblrPost = list[index]

    fun addPhotos(photos: List<TumblrPost>) {
        val startPosition = list.size
        val count = photos.size
        list.addAll(photos)
        changeEventListeners.forEach {
            it(ChangeEvent.ItemsAdded(startPosition, count))
        }
    }

    fun addChangeEventListener(listener: (ChangeEvent) -> Unit) {
        changeEventListeners.add(listener)
    }

    fun removeChangeEventListener(listener: (ChangeEvent) -> Unit) {
        changeEventListeners.removeAll { it === listener }
    }

    sealed class ChangeEvent {
        data class ItemsAdded(val startPosition: Int, val itemCount: Int) : ChangeEvent()
    }

}

package info.vividcode.android.app.yuyutumblr.usecase

import org.json.JSONObject

class PhotoTimeline {

    private val list = mutableListOf<JSONObject>()

    private val changeEventListeners = mutableListOf<(ChangeEvent) -> Unit>()

    val size: Int get() = list.size

    val lastItem: JSONObject? get() = list.lastOrNull()

    fun getPhoto(index: Int): JSONObject = list[index]

    fun addPhotos(photos: List<JSONObject>) {
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

    sealed class ChangeEvent {
        data class ItemsAdded(val startPosition: Int, val itemCount: Int) : ChangeEvent()
    }

}

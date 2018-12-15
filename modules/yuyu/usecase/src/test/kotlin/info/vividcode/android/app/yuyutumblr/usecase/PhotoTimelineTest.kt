package info.vividcode.android.app.yuyutumblr.usecase

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PhotoTimelineTest {

    @Test
    internal fun initialState() {
        val photoTimeline = PhotoTimeline()

        Assertions.assertEquals(0, photoTimeline.size)
        Assertions.assertNull(photoTimeline.lastItem)
    }

    @Nested
    internal inner class AddPhotosTest {
        @Test
        internal fun addEmptyList() {
            val photoTimeline = PhotoTimeline()

            photoTimeline.addPhotos(emptyList())

            Assertions.assertEquals(0, photoTimeline.size)
            Assertions.assertNull(photoTimeline.lastItem)
        }

        @Test
        internal fun addPhotos() {
            val photoTimeline = PhotoTimeline()

            val photo1 = TumblrPost.Photo(100, listOf(TumblrPhotoInfo(listOf(Photo(200, 200, "http://example.com/image-1.jpg")))))
            val photo2 = TumblrPost.Photo(100, listOf(TumblrPhotoInfo(listOf(Photo(200, 200, "http://example.com/image-2.jpg")))))

            photoTimeline.addPhotos(listOf(photo2, photo1))

            Assertions.assertEquals(2, photoTimeline.size)
            Assertions.assertSame(photo1, photoTimeline.lastItem)
        }
    }

    @Nested
    internal inner class GetPhotoTest {
        @Test
        internal fun getFromEmpty() {
            val photoTimeline = PhotoTimeline()

            Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
                photoTimeline.getPhoto(0)
            }
        }

        @Test
        internal fun getPhoto() {
            val photoTimeline = PhotoTimeline()
            val photo1 = TumblrPost.Photo(100, listOf(TumblrPhotoInfo(listOf(Photo(200, 200, "http://example.com/image-1.jpg")))))
            val photo2 = TumblrPost.Photo(100, listOf(TumblrPhotoInfo(listOf(Photo(200, 200, "http://example.com/image-2.jpg")))))
            photoTimeline.addPhotos(listOf(photo2, photo1))

            // Act
            val photo = photoTimeline.getPhoto(1)

            Assertions.assertSame(photo1, photo)
        }
    }

    @Nested
    internal inner class EventListenerTest {
        @Test
        internal fun getFromEmpty() {
            val photoTimeline = PhotoTimeline()

            val eventListener = object : (PhotoTimeline.ChangeEvent) -> Unit {
                val receivedEvents = mutableListOf<PhotoTimeline.ChangeEvent>()

                override fun invoke(event: PhotoTimeline.ChangeEvent) {
                    receivedEvents.add(event)
                }
            }
            photoTimeline.addChangeEventListener(eventListener)

            val photo1 = TumblrPost.Photo(100, listOf(TumblrPhotoInfo(listOf(Photo(200, 200, "http://example.com/image-1.jpg")))))
            val photo2 = TumblrPost.Photo(100, listOf(TumblrPhotoInfo(listOf(Photo(200, 200, "http://example.com/image-2.jpg")))))
            val photo3 = TumblrPost.Photo(100, listOf(TumblrPhotoInfo(listOf(Photo(200, 200, "http://example.com/image-3.jpg")))))

            photoTimeline.addPhotos(emptyList())
            photoTimeline.addPhotos(listOf(photo1))
            photoTimeline.addPhotos(listOf(photo2, photo3))

            Assertions.assertEquals(
                    listOf(
                            PhotoTimeline.ChangeEvent.ItemsAdded(0, 0),
                            PhotoTimeline.ChangeEvent.ItemsAdded(0, 1),
                            PhotoTimeline.ChangeEvent.ItemsAdded(1, 2)
                    ),
                    eventListener.receivedEvents
            )
        }
    }

}

package com.hide.videophoto.common.util

import com.hide.videophoto.data.model.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

// Use object so we have a singleton instance
object RxBus {

    private val publisher = PublishSubject.create<Any?>()

    private fun publish(event: Any?) {
        event?.let {
            publisher.onNext(it)
        }
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    private fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

    fun publishFolderChanged(model: EventFolderModel = EventFolderModel()) {
        publish(model)
    }

    fun listenFolderChanged(): Observable<EventFolderModel> {
        return listen(EventFolderModel::class.java)
    }

    fun publishNoteChanged(model: EventNoteModel) {
        publish(model)
    }

    fun listenNoteChanged(): Observable<EventNoteModel> {
        return listen(EventNoteModel::class.java)
    }

    fun publishFileChanged(model: EventFileModel) {
        publish(model)
    }

    fun listenFileChanged(): Observable<EventFileModel> {
        return listen(EventFileModel::class.java)
    }

    fun publishUserActivities(model: ActivityModel) {
        publish(model)
    }

    fun listenUserActivities(): Observable<ActivityModel> {
        return listen(ActivityModel::class.java)
    }

    fun publishAppSettingsChanged(model: EventAppSettingsModel) {
        publish(model)
    }

    fun listenAppSettingsChanged(): Observable<EventAppSettingsModel> {
        return listen(EventAppSettingsModel::class.java)
    }

    fun publishAppStateChanged(model: EventAppStateChangeModel) {
        publish(model)
    }

    fun listenAppStateChanged(): Observable<EventAppStateChangeModel> {
        return listen(EventAppStateChangeModel::class.java)
    }
}
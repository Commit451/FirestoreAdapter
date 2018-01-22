package com.commit451.firestoreadapter

import android.support.v7.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query


/**
 * [RecyclerView.Adapter] for displaying the results of a Firestore [Query]. If you need to use [DocumentSnapshot]s directly, use [DocumentSnapshotFirestoreAdapter]
 */
abstract class FirestoreAdapter<out T, VH : RecyclerView.ViewHolder>(private val clazz: Class<T>, queryCreator: QueryCreator) : DocumentSnapshotFirestoreAdapter<VH>(queryCreator) {

    private val items = mutableMapOf<String, T>()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onDocumentAdded(change: DocumentChange) {
        items[change.document.id] = change.document.toObject(clazz)
        super.onDocumentAdded(change)
    }

    override fun onDocumentModified(change: DocumentChange) {
        items[change.document.id] = change.document.toObject(clazz)
        super.onDocumentModified(change)
    }

    override fun onDocumentRemoved(change: DocumentChange) {
        items.remove(change.document.id)
        super.onDocumentRemoved(change)
    }

    override fun clear() {
        items.clear()
        super.clear()
    }

    fun get(index: Int): T {
        return items.values.elementAt(index)
    }
}
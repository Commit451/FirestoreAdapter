package com.commit451.firestoreadapter

import android.support.v7.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query


/**
 * [RecyclerView.Adapter] for displaying the results of a Firestore [Query]. If you need to use [DocumentSnapshot]s directly, use [DocumentSnapshotFirestoreAdapter]
 */
abstract class FirestoreAdapter<out T, VH : RecyclerView.ViewHolder>(private val clazz: Class<T>, queryCreator: QueryCreator) : DocumentSnapshotFirestoreAdapter<VH>(queryCreator) {

    private val items = mutableListOf<T>()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onDocumentAdded(change: DocumentChange, newIndex: Int) {
        items.add(newIndex, change.document.toObject(clazz))
        super.onDocumentAdded(change, newIndex)
    }

    override fun onDocumentModified(change: DocumentChange, oldIndex: Int, newIndex: Int) {
        if (oldIndex == newIndex) {
            // Item changed but remained in same position
            items[oldIndex] = change.document.toObject(clazz)
        } else {
            // Item changed and changed position
            items.removeAt(oldIndex)
            items.add(newIndex, change.document.toObject(clazz))
        }
        super.onDocumentModified(change, oldIndex, newIndex)
    }

    override fun onDocumentRemoved(change: DocumentChange, oldIndex: Int) {
        items.removeAt(oldIndex)
        super.onDocumentRemoved(change, oldIndex)
    }

    override fun clear() {
        items.clear()
        super.clear()
    }

    fun get(index: Int): T {
        return items[index]
    }
}
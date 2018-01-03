package com.commit451.firestoreadapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.firestore.*

/**
 * RecyclerView adapter for displaying the results of a Firestore [Query]. Holds [DocumentSnapshot]s.
 * You should favor [FirestoreAdapter] unless you have a good reason to use this one, since [FirestoreAdapter]
 * prevents conversion of [DocumentSnapshot] to objects as a user scrolls
 */
abstract class DocumentSnapshotFirestoreAdapter<VH : RecyclerView.ViewHolder>(private var queryCreator: QueryCreator) : RecyclerView.Adapter<VH>(), EventListener<QuerySnapshot> {

    var onLoadingMore: OnLoadingMore? = null
    var onLoadingMoreComplete: OnLoadingMoreComplete? = null
    var onHasLoadedAll: OnHasLoadedAll? = null

    private val snapshots = mutableListOf<DocumentSnapshot>()

    private var loadingMore = false

    private var hasLoadedAll = false

    private var limit = -1

    private var queries = mutableListOf<Query>()
    private var registrations = mutableListOf<ListenerRegistration>()

    init {
        queries.add(queryCreator.invoke())
    }

    override fun onEvent(documentSnapshots: QuerySnapshot, e: FirebaseFirestoreException?) {
        if (e != null) {
            onError(e)
            return
        }

        //since we can't get the initial limit count from the query
        if (limit == -1) {
            limit = documentSnapshots.documentChanges.size
        }

        if (loadingMore) {
            if (documentSnapshots.documentChanges.size < limit) {
                hasLoadedAll = true
            }
            loadingMore = false
            onLoadingMoreComplete?.invoke()
        }
        // Dispatch the event
        for (change in documentSnapshots.documentChanges) {
            val newIndex = change.newIndex + (queries.size - 1) * limit
            val oldIndex = change.oldIndex + (queries.size - 1) * limit
            when (change.type) {
                DocumentChange.Type.ADDED -> onDocumentAdded(change, newIndex)
                DocumentChange.Type.MODIFIED -> onDocumentModified(change, oldIndex, newIndex)
                DocumentChange.Type.REMOVED -> onDocumentRemoved(change, oldIndex)
            }
        }

        onDataChanged()
    }

    fun startListening() {
        if (registrations.isEmpty()) {
            queries
                    .forEach {
                        listenToQuery(it)
                    }
        }
    }

    fun stopListening() {
        registrations.forEach {
            it.remove()
        }
        registrations.clear()
    }

    open fun clear() {
        snapshots.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return snapshots.size
    }

    protected fun getSnapshot(index: Int): DocumentSnapshot {
        return snapshots[index]
    }

    protected open fun onDocumentAdded(change: DocumentChange, newIndex: Int) {
        snapshots.add(newIndex, change.document)
        notifyItemInserted(newIndex)
    }

    protected open fun onDocumentModified(change: DocumentChange, oldIndex: Int, newIndex: Int) {
        if (oldIndex == newIndex) {
            // Item changed but remained in same position
            snapshots.set(oldIndex, change.document)
            notifyItemChanged(oldIndex)
        } else {
            // Item changed and changed position
            snapshots.removeAt(change.oldIndex)
            snapshots.add(newIndex, change.document)
            notifyItemMoved(oldIndex, newIndex)
        }
    }

    protected open fun onDocumentRemoved(change: DocumentChange, oldIndex: Int) {
        snapshots.removeAt(oldIndex)
        notifyItemRemoved(oldIndex)
    }

    protected open fun onError(e: FirebaseFirestoreException) {}

    protected open fun onDataChanged() {}

    fun setupOnScrollListener(recyclerView: RecyclerView, layoutManager: LinearLayoutManager): RecyclerView.OnScrollListener {
        val onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItem + visibleItemCount >= totalItemCount && !loadingMore && !hasLoadedAll) {
                    val lastVisible = snapshots.last()
                    val query = queryCreator.invoke()
                            .startAfter(lastVisible)
                    queries.add(query)
                    loadingMore = true
                    onLoadingMore?.invoke()
                    listenToQuery(query)
                }
            }
        }
        recyclerView.addOnScrollListener(onScrollListener)
        return onScrollListener
    }

    private fun listenToQuery(query: Query) {
        val registration = query.addSnapshotListener(this)
        registrations.add(registration)
    }
}

typealias QueryCreator = () -> Query
typealias OnLoadingMore = () -> Unit
typealias OnLoadingMoreComplete = () -> Unit
typealias OnHasLoadedAll = () -> Unit
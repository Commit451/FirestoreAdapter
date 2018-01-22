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

    private var queries = mutableListOf<Query>()
    private var registrations = mutableListOf<ListenerRegistration>()

    override fun onEvent(documentSnapshots: QuerySnapshot, e: FirebaseFirestoreException?) {
        if (e != null) {
            onError(e)
            return
        }

        // Dispatch the event
        for (change in documentSnapshots.documentChanges) {
            when (change.type) {
                DocumentChange.Type.ADDED -> onDocumentAdded(change)
                DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
            }
        }

        onDataChanged()

        hasLoadedAll = documentSnapshots.isEmpty

        if (loadingMore) {
            loadingMore = false
            onLoadingMoreComplete?.invoke()
        }
    }

    fun startListening() {
        if (queries.isEmpty()) {
            queries.add(queryCreator.invoke())
        }
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
        queries.clear()
        stopListening()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return snapshots.size
    }

    fun getSnapshot(index: Int): DocumentSnapshot {
        return snapshots[index]
    }

    protected open fun onDocumentAdded(change: DocumentChange) {
        val index = snapshots.size
        snapshots.add(index, change.document)
        notifyItemInserted(index)
        //sadly, we always have to add documents to the end of the list, since we cannot attempt to
        //add to the correct index since we have multiple queries
    }

    protected open fun onDocumentModified(change: DocumentChange) {
        val index = snapshots.indexOfFirst { it.id == change.document.id }
        snapshots[index] = change.document
        notifyItemChanged(index)
    }

    protected open fun onDocumentRemoved(change: DocumentChange) {
        val index = snapshots.indexOfFirst { it.id == change.document.id }
        snapshots.removeAt(index)
        notifyItemRemoved(index)
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
package com.commit451.firestoreadapter.sample

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class MainActivity : AppCompatActivity() {

    companion object {
        const val SORT_NAME = "name"
        const val SORT_POPULATION = "population"
    }

    private lateinit var root: ViewGroup
    private lateinit var adapter: ItemAdapter

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val refStates by lazy {
        firestore.collection("states")
    }

    private var sort = SORT_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        root = findViewById(R.id.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.app_name)
        toolbar.inflateMenu(R.menu.refresh)
        toolbar.inflateMenu(R.menu.sort)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_refresh -> {
                    adapter.clear()
                    adapter.startListening()
                    return@setOnMenuItemClickListener true
                }
                R.id.action_sort -> {
                    if (sort == SORT_NAME) sort = SORT_POPULATION else sort = SORT_NAME
                    snackbar("Sorting by $sort")
                    adapter.clear()
                    adapter.startListening()
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }

        adapter = ItemAdapter({
            refStates.limit(10)
                .orderBy(sort, Query.Direction.ASCENDING)
        })

        adapter.onDeleteListener = { position ->
            //assume success, otherwise it will be updated in the next query
            val state = adapter.get(position)
            val snapshot = adapter.getSnapshot(position)
            delete(state, snapshot.reference)
        }

        adapter.onUpListener = { position ->
            val state = adapter.get(position)
            val snapshot = adapter.getSnapshot(position)
            incrementPopulation(state, snapshot.reference)
            //shows us waiting for the update
        }
        adapter.onClickListener = { position ->
            Snackbar.make(root, "$position clicked", Snackbar.LENGTH_SHORT)
                .show()
        }

        val list = findViewById<RecyclerView>(R.id.list)
        val layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        list.layoutManager = layoutManager
        adapter.setupOnScrollListener(list, layoutManager)

        adapter.onLoadingMore = {
            log("onLoadingMore")
        }
        adapter.onLoadingMoreComplete = {
            log("onLoadingMoreComplete")
        }
        adapter.onHasLoadedAll = {
            log("onHasLoadedAll")
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.clear()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    fun log(string: String) {
        Log.d("TEST", string)
    }

    fun snackbar(string: String) {
        Snackbar.make(root, string, Snackbar.LENGTH_SHORT)
            .show()
    }

    fun incrementPopulation(state: State, docRef: DocumentReference) {
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val newPopulation = snapshot.getDouble("population")!! + 1
            transaction.update(docRef, "population", newPopulation)

            // Success
            null
        }.addOnSuccessListener {
            log("Transaction success!")
        }.addOnFailureListener { e ->
            e.printStackTrace()
            snackbar("Failed to increment ${state.name}")
        }
    }

    fun delete(state: State, docRef: DocumentReference) {
        docRef.delete()
            .addOnSuccessListener {
                log("Transaction success!")
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                snackbar("Failed to delete ${state.name}")
            }
    }
}

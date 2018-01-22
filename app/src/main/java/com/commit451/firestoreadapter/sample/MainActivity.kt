package com.commit451.firestoreadapter.sample

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private lateinit var root: ViewGroup
    private lateinit var adapter: ItemAdapter

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val refStates by lazy {
        firestore.collection("states")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        root = findViewById(R.id.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.app_name)
        toolbar.inflateMenu(R.menu.refresh)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_refresh -> {
                    adapter.clear()
                    adapter.stopListening()
                    adapter.startListening()
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }

        adapter = ItemAdapter({
            refStates.limit(20)
                    .orderBy("name")
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
        firestore.runTransaction({ transaction ->
            val snapshot = transaction.get(docRef)
            val newPopulation = snapshot.getDouble("population")!! + 1
            transaction.update(docRef, "population", newPopulation)

            // Success
            null
        }).addOnSuccessListener({
            log("Transaction success!")
        }).addOnFailureListener({ e ->
            e.printStackTrace()
            snackbar("Failed to increment ${state.name}")
        })
    }

    fun delete(state: State, docRef: DocumentReference) {
        docRef.delete()
                .addOnSuccessListener({
                    log("Transaction success!")
                })
                .addOnFailureListener({ e ->
                    e.printStackTrace()
                    snackbar("Failed to delete ${state.name}")
                })
    }
}

package com.commit451.firestoreadapter.sample

import android.view.ViewGroup
import com.commit451.firestoreadapter.FirestoreAdapter
import com.commit451.firestoreadapter.QueryCreator

class ItemAdapter(query: QueryCreator) : FirestoreAdapter<State, ItemViewHolder>(State::class.java, query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(get(position))
    }
}
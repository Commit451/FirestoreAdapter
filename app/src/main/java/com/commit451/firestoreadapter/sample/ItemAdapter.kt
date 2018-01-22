package com.commit451.firestoreadapter.sample

import android.view.ViewGroup
import com.commit451.firestoreadapter.FirestoreAdapter
import com.commit451.firestoreadapter.QueryCreator

class ItemAdapter(query: QueryCreator) : FirestoreAdapter<State, ItemViewHolder>(State::class.java, query) {

    var onDeleteListener: ((position: Int) -> Unit)? = null
    var onUpListener: ((position: Int) -> Unit)? = null
    var onClickListener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val holder = ItemViewHolder.inflate(parent)
        holder.buttonDelete.setOnClickListener {
            val position = holder.adapterPosition
            onDeleteListener?.invoke(position)
        }
        holder.buttonUp.setOnClickListener {
            val position = holder.adapterPosition
            onUpListener?.invoke(position)
        }
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            onClickListener?.invoke(position)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(get(position))
    }
}
package com.commit451.firestoreadapter.sample

import android.view.ViewGroup
import com.commit451.firestoreadapter.FirestoreAdapter
import com.commit451.firestoreadapter.QueryCreator

class BookAdapter(query: QueryCreator) : FirestoreAdapter<Book, BookViewHolder>(Book::class.java, query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(get(position))
    }
}
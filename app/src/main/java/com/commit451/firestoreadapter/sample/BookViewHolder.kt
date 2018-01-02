package com.commit451.firestoreadapter.sample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun inflate(parent: ViewGroup): BookViewHolder {
            return BookViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_book, parent, false))
        }
    }

    var textTitle: TextView = itemView.findViewById(R.id.text_title)
    var textAuthor: TextView = itemView.findViewById(R.id.text_author)

    fun bind(book: Book) {
        textTitle.text = book.title
        textAuthor.text = book.author
    }
}
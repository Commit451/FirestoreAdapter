# FirestoreAdapter

[![Build Status](https://travis-ci.org/Commit451/FirestoreAdapter.svg?branch=master)](https://travis-ci.org/Commit451/FirestoreAdapter) [![](https://jitpack.io/v/Commit451/FirestoreAdapter.svg)](https://jitpack.io/#Commit451/FirestoreAdapter)

RecyclerView Adapter for Firebase Firestore

## Usage
Start by subclassing FirestoreAdapter:
```kotlin
class ItemAdapter(creator: QueryCreator) : FirestoreAdapter<State, ItemViewHolder>(State::class.java, creator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(get(position))
    }
}
```
Usage will look like so:
```kotlin
val firestore = FirebaseFirestore.getInstance()
val ref = firestore.collection("states")
adapter = ItemAdapter({
    ref.limit(10)
            .orderBy("name")
})

val list = findViewById<RecyclerView>(R.id.list)
val layoutManager = LinearLayoutManager(this)
list.adapter = adapter
list.layoutManager = layoutManager
//this allows for endless scrolling
adapter.setupOnScrollListener(list, layoutManager)
```

License
--------

    Copyright 2018 Commit 451

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

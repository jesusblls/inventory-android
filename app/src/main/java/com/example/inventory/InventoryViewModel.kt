/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {
    private val db = Firebase.firestore

    private val _allItems = MutableLiveData<List<Item>>()
    val allItems: LiveData<List<Item>> = _allItems

    init {
        loadItemsFromFirestore()
    }

    // Cache all items form the database using LiveData.
    //val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()

    /**
     * Obtener items que no tengan salida
     */
    fun loadItemsFromFirestore() {
        db.collection("items")
            .whereEqualTo("salida", "")
            .get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<Item>()
                for (document in result) {
                    val item = Item(
                        id = document.id.toString(),
                        itemModelo = document.data["modelo"].toString(),
                        itemNumeroSerie = document.data["numeroSerie"].toString(),
                        itemMarca = document.data["marca"].toString()
                    )
                    items.add(item)
                }
                _allItems.value = items
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }
    /**
     * Returns true if stock is available to sell, false otherwise.
     */
    fun isStockAvailable(item: Item): Boolean {
        return true;
    }

    /**
     * Updates an existing Item in the database.
     */
    fun updateItem(
        itemId: String,
        itemModelo: String,
        itemNumeroSerie: String,
        itemMarca: String
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, itemModelo, itemNumeroSerie, itemMarca)
        updateItem(updatedItem)
    }


    /**
     * Launching a new coroutine to update an item in a non-blocking way
     */
    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    /**
     * Decreases the stock by one unit and updates the database.
     */
    fun sellItem(item: Item) {
        //if (item.quantityInStock > 0) {
            // Decrease the quantity by 1
          //  val newItem = item.copy(quantityInStock = item.quantityInStock - 1)
           // updateItem(newItem)
        //}
    }
    /**
     * Filters items with firestore
     */
    fun filterItems(query: String): LiveData<List<Item>> {
        val filteredItems = MutableLiveData<List<Item>>()
        val queryRef = if (query.isEmpty()) {
            db.collection("items").whereEqualTo("salida", "")
        } else {
            // Ajusta esta consulta según tus necesidades específicas y los índices disponibles en Firestore
            db.collection("items")
                .orderBy("modelo") // Asegúrate de tener un índice para este campo si usas orderBy
                .startAt(query)
                .endAt(query + '\uf8ff')
                .whereEqualTo("salida", "")
        }

        queryRef.get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<Item>()
                for (document in result) {
                    val item = Item(
                        id = document.id.toString(), // Asegúrate de que este mapeo es correcto
                        itemModelo = document.data["modelo"].toString(),
                        itemNumeroSerie = document.data["numeroSerie"].toString(),
                        itemMarca = document.data["marca"].toString()
                    )
                    items.add(item)
                }
                filteredItems.postValue(items)
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
        return filteredItems
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun marcarSalidaItemsSeleccionados(selectedItemIds: Set<String>) {
        selectedItemIds.forEach { itemId ->
            marcarSalidaConId(itemId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun marcarSalidaConId(itemId: String) {
        val itemRef = db.collection("items").document(itemId)
        itemRef.update("salida", java.time.LocalDateTime.now().toString())
            .addOnSuccessListener { println("DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> println("Error updating document $e") }
    }

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(itemModelo: String, itemNumeroSerie: String, itemMarca: String) {
        //val newItem = getNewItemEntry(itemModelo, itemNumeroSerie, itemMarca)
        //insertItem(newItem)

        //usando firestore
        val db = Firebase.firestore
        val item = hashMapOf(
            "modelo" to itemModelo,
            "numeroSerie" to itemNumeroSerie,
            "marca" to itemMarca,
            "salida" to "",
        )

        db.collection("items")
            .add(item)
            .addOnSuccessListener { documentReference ->
                println("DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    /**
     * Marca la salida de un item en firebase con hora y fecha en dd/MM/yyyy HH:mm:ss
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun marcarSalida(item: Item) {
        val db = Firebase.firestore
        val itemRef = db.collection("items").document(item.id)

        itemRef
            .update("salida", java.time.LocalDateTime.now().toString())
            .addOnSuccessListener { println("DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> println("Error updating document $e") }
    }

    /**
     * Retrieve an item from the repository.
     */
    fun retrieveItem(id: String): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }

    fun getItemFromFirestore(id: String): LiveData<Item> {
        val itemLiveData = MutableLiveData<Item>()
        db.collection("items").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val item = Item(
                        id = document.id.toString(),
                        itemModelo = document.data?.get("modelo").toString(),
                        itemNumeroSerie = document.data?.get("numeroSerie").toString(),
                        itemMarca = document.data?.get("marca").toString()
                    )
                    itemLiveData.postValue(item)
                } else {
                    println("No such document")
                }
            }
            .addOnFailureListener { exception ->
                println("get failed with $exception")
            }
        return itemLiveData
    }
    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(itemModelo: String, itemNumeroSerie: String, itemMarca: String): Boolean {
        if (itemModelo.isBlank() || itemNumeroSerie.isBlank() || itemMarca.isBlank()) {
            return false
        }
        return true
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(itemModelo: String, itemNumeroSerie: String, itemMarca: String): Item {
        return Item(
            id = "",
            itemModelo = itemModelo,
            itemNumeroSerie = itemNumeroSerie,
            itemMarca = itemMarca
        )
    }

    /**
     * Called to update an existing entry in the Inventory database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        itemId: String,
        itemModelo: String,
        itemNumeroSerie: String,
        itemMarca: String
    ): Item {
        return Item(
            id = itemId,
            itemModelo = itemModelo,
            itemNumeroSerie = itemNumeroSerie,
            itemMarca = itemMarca
        )
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


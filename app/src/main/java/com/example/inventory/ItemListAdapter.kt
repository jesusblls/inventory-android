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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.Item
import com.example.inventory.databinding.ItemListItemBinding

/**
 * [ListAdapter] implementation for the recyclerview.
 */

class ItemListAdapter(private val onItemClicked: (Item) -> Unit, private val isCheckboxVisible: Boolean = true) :
    ListAdapter<Item, ItemListAdapter.ItemViewHolder>(DiffCallback) {

    private var isAllSelected = false
    var selectedItems = mutableListOf<String>()

    fun selectAll(isSelected: Boolean) {
        isAllSelected = isSelected

        if(isSelected){
            currentList.forEach {
                selectedItems.add(it.id)
            }
        } else {
            selectedItems.clear()
        }

        notifyDataSetChanged() // Notifica que los datos han cambiado para refrescar la lista

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current, isAllSelected, isCheckboxVisible) { item, isChecked ->
            if (isChecked) {
                selectedItems.add(item.id)
            } else {
                selectedItems.remove(item.id)
            }
        }
    }

    class ItemViewHolder(var binding: ItemListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, isAllSelected: Boolean, isCheckboxVisible: Boolean, onCheckboxClicked: (Item, Boolean) -> Unit) {
            binding.itemName.text = item.itemModelo
            binding.itemPrice.text = item.itemNumeroSerie
            binding.itemQuantity.text = if (isCheckboxVisible) {
                item.itemMarca
            } else {
                item.itemSalida
            }
            binding.itemCheckbox.isChecked = isAllSelected
            binding.itemCheckbox.visibility = if (isCheckboxVisible) {
                RecyclerView.VISIBLE
            } else {
                RecyclerView.GONE
            }
            binding.itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxClicked(item, isChecked)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.itemModelo == newItem.itemModelo
            }
        }
    }
}

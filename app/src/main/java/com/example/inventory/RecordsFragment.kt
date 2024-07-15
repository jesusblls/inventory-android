package com.example.inventory

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.inventory.databinding.FragmentRecordsListBinding

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRecordsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Crear una instancia de ItemListAdapter
        val adapter = ItemListAdapter ({ item ->
            // Manejar el clic en un elemento, si es necesario
            // Por ejemplo, navegar a un detalle del item
        }, isCheckboxVisible = false)

        binding.list.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.loadItemsFromFirestore(onlyWithExitDate = true)

        // Observar allItems y actualizar la lista cuando cambien los datos
        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
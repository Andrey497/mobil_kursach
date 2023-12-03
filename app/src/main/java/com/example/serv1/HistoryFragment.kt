package com.example.serv1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

import com.example.serv1.databinding.FragmentHistoryBinding


class HistoryFragment : Fragment(R.layout.fragment_history) {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val records = HistoryRecords()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext())
        records.load(preferences)

        val rvHistory = binding.rvHistory
        val adapterRv = CustomAdapterHistoryRecord(this.requireContext(), records.list)
        rvHistory.adapter = adapterRv

    }



}
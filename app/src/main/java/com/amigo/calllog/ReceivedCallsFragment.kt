package com.amigo.calllog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReceivedCallsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CallLogAdapter

    companion object {
        fun newInstance(calls: List<CallLogItem>): ReceivedCallsFragment {
            return ReceivedCallsFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("calls", ArrayList(calls))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_received_calls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        
        val calls = arguments?.getParcelableArrayList<CallLogItem>("calls") ?: listOf()
        val timeFilters = TimeFilter.groupCallsByDate(calls)

        val items = mutableListOf<CallLogAdapter.ListItem>()
        timeFilters.forEach { filter ->
            items.add(CallLogAdapter.ListItem.HeaderItem(filter.title))
            items.addAll(filter.calls.map { CallLogAdapter.ListItem.CallItem(it) })
        }

        adapter = CallLogAdapter(items)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
}
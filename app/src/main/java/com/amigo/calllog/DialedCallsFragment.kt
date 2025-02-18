package com.amigo.calllog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DialedCallsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: CallLogAdapter
    private var calls: List<CallLogItem> = listOf()

    companion object {
        fun newInstance(calls: List<CallLogItem>): DialedCallsFragment {
            return DialedCallsFragment().apply {
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
        return inflater.inflate(R.layout.fragment_dialed_calls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        
        calls = arguments?.getParcelableArrayList("calls") ?: listOf()
        setupRecyclerView()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
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

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshCallLogs()
        }
    }

    private fun refreshCallLogs() {
        CoroutineScope(Dispatchers.IO).launch {
            // Fetch call logs again
            val context = requireContext()
            val (_, _, dialed) = CallLogHelper.getCallLogs(context)
            
            withContext(Dispatchers.Main) {
                // Update calls and refresh RecyclerView
                calls = dialed.flatMap { it.calls }
                setupRecyclerView()
                
                // Stop the refresh animation
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}
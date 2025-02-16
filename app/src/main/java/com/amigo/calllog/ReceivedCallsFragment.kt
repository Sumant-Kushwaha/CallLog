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
        fun newInstance(callLogs: ArrayList<CallLogItem>) = ReceivedCallsFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("callLogs", callLogs)
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
        val callLogs = arguments?.getParcelableArrayList<CallLogItem>("callLogs") ?: ArrayList()
        adapter = CallLogAdapter(callLogs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
}
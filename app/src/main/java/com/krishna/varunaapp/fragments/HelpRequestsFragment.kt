package com.krishna.varunaapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.krishna.varunaapp.databinding.FragmentHelpRequestsBinding
import com.krishna.varunaapp.models.HelpRequest
import com.krishna.varunaapp.adapters.HelpRequestAdapter

class HelpRequestsFragment : Fragment() {

    private var _binding: FragmentHelpRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: HelpRequestAdapter
    private val helpRequests = mutableListOf<HelpRequest>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpRequestsBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchHelpRequests()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = HelpRequestAdapter(helpRequests,
            onApprove = { request -> approveRequest(request) },
            onDelete = { request -> deleteRequest(request) }
        )
        binding.recyclerHelpRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHelpRequests.adapter = adapter
    }

    private fun fetchHelpRequests() {
        db.collection("helpRequests")
            .get()
            .addOnSuccessListener { snapshot ->
                helpRequests.clear()
                for (doc in snapshot.documents) {
                    val item = doc.toObject(HelpRequest::class.java)
                    if (item != null) item.id = doc.id
                    item?.let { helpRequests.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load requests", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approveRequest(request: HelpRequest) {
        db.collection("helpRequests").document(request.id!!)
            .update("status", "Approved")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Request approved", Toast.LENGTH_SHORT).show()
                fetchHelpRequests()
            }
    }

    private fun deleteRequest(request: HelpRequest) {
        db.collection("helpRequests").document(request.id!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Request deleted", Toast.LENGTH_SHORT).show()
                fetchHelpRequests()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

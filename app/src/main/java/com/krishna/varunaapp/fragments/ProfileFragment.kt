package com.krishna.varunaapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krishna.varunaapp.activities.LoginActivity
import com.krishna.varunaapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserProfile()

        // Always update directly (NO DIALOG)
        binding.btnUpdate.setOnClickListener {
            updateUserProfile()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }

        return binding.root
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.etUsername.setText(doc.getString("username") ?: "")
                    binding.etMobile.setText(doc.getString("mobile") ?: "")
                    binding.etEmail.setText(doc.getString("email") ?: "")
                    binding.etState.setText(doc.getString("state") ?: "")
                    binding.etVillage.setText(doc.getString("village") ?: "")
                    binding.etPincode.setText(doc.getString("pincode") ?: "")
                }
            }
    }

    private fun updateUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        val updatedData = mapOf(
            "username" to binding.etUsername.text.toString(),
            "mobile" to binding.etMobile.text.toString(),
            "state" to binding.etState.text.toString(),
            "village" to binding.etVillage.text.toString(),
            "pincode" to binding.etPincode.text.toString()
        )

        db.collection("users").document(uid)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//package com.krishna.varunaapp.fragments
//
//import android.app.AlertDialog
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.activities.LoginActivity
//import com.krishna.varunaapp.databinding.FragmentProfileBinding
//import java.util.*
//
//class ProfileFragment : Fragment() {
//
//    private var _binding: FragmentProfileBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var db: FirebaseFirestore
//    private var userRole: String = "GeneralUser"
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentProfileBinding.inflate(inflater, container, false)
//        auth = FirebaseAuth.getInstance()
//        db = FirebaseFirestore.getInstance()
//
//        loadUserProfile()
//
//        binding.btnUpdate.setOnClickListener {
//            if (userRole == "GeneralUser") showHelpRequestDialog()
//            else updateUserProfile()
//        }
//
//        binding.btnLogout.setOnClickListener {
//            auth.signOut()
//            startActivity(Intent(requireContext(), LoginActivity::class.java))
//            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
//            requireActivity().finish()
//        }
//
//        return binding.root
//    }
//
//    private fun loadUserProfile() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    binding.etUsername.setText(document.getString("username") ?: "")
//                    binding.etMobile.setText(document.getString("mobile") ?: "")
//                    binding.etEmail.setText(document.getString("email") ?: "")
//                    binding.etState.setText(document.getString("state") ?: "")
//                    binding.etVillage.setText(document.getString("village") ?: "")
//                    binding.etPincode.setText(document.getString("pincode") ?: "")
//                    userRole = document.getString("role") ?: "GeneralUser"
//                }
//            }
//    }
//
//    private fun updateUserProfile() {
//        val uid = auth.currentUser?.uid ?: return
//        val updated = mapOf(
//            "username" to binding.etUsername.text.toString(),
//            "mobile" to binding.etMobile.text.toString(),
//            "state" to binding.etState.text.toString(),
//            "village" to binding.etVillage.text.toString(),
//            "pincode" to binding.etPincode.text.toString()
//        )
//
//        db.collection("users").document(uid).update(updated)
//            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun showHelpRequestDialog() {
//        val dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.simple_list_item_1, null)
//        val input = EditText(requireContext())
//        input.hint = "Describe what you want to update"
//        val dialog = AlertDialog.Builder(requireContext())
//            .setTitle("Submit Help Request")
//            .setView(input)
//            .setPositiveButton("Submit") { _, _ ->
//                val request = mapOf(
//                    "userId" to auth.currentUser?.uid,
//                    "message" to input.text.toString(),
//                    "timestamp" to Date(),
//                    "status" to "Pending"
//                )
//                db.collection("helpRequests").add(request)
//                    .addOnSuccessListener {
//                        Toast.makeText(requireContext(), "Request submitted", Toast.LENGTH_SHORT).show()
//                    }
//                    .addOnFailureListener {
//                        Toast.makeText(requireContext(), "Failed to submit", Toast.LENGTH_SHORT).show()
//                    }
//            }
//            .setNegativeButton("Cancel", null)
//            .create()
//        dialog.show()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
//
//

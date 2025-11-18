package com.krishna.varunaapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun currentUser() = auth.currentUser

    fun createUserDocument(
        uid: String,
        data: Map<String, Any>,
        callback: (Boolean, String?) -> Unit
    ) {
        db.collection("users").document(uid)
            .set(data)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.message) }
    }
}


//package com.krishna.varunaapp.utils
//
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.FirebaseStorage
//
//
//object FirebaseUtils {
//    val auth: FirebaseAuth = FirebaseAuth.getInstance()
//    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
//    val storage: FirebaseStorage = FirebaseStorage.getInstance()
//
//
//    fun currentUser(): FirebaseUser? = auth.currentUser
//
//
//    // Create user document after signup
//    fun createUserDocument(uid: String, userMap: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
//        firestore.collection("users").document(uid)
//            .set(userMap)
//            .addOnSuccessListener { onComplete(true, null) }
//            .addOnFailureListener { e -> onComplete(false, e.message) }
//    }
//
//
//    // Simple role check helper by reading user's doc
//    fun getUserRole(uid: String, onResult: (String?) -> Unit) {
//        firestore.collection("users").document(uid).get()
//            .addOnSuccessListener { snap -> onResult(snap.getString("role")) }
//            .addOnFailureListener { _ -> onResult(null) }
//    }
//}
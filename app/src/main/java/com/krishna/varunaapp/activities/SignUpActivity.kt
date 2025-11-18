package com.krishna.varunaapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.krishna.varunaapp.databinding.ActivitySignUpBinding
import com.krishna.varunaapp.models.User
import com.krishna.varunaapp.utils.FirebaseUtils

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSignup.setOnClickListener {

            val username = binding.etUsername.text.toString().trim()
            val mobile = binding.etMobile.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val state = binding.etState.text.toString().trim()
            val village = binding.etVillage.text.toString().trim()
            val pincode = binding.etPincode.text.toString().trim()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->

                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    val user = User(
                        username = username,
                        mobile = mobile,
                        email = email,
                        role = "GeneralUser",
                        state = state,
                        village = village,
                        pincode = pincode
                    )

                    FirebaseUtils.createUserDocument(uid, user.toMap()) { success, err ->
                        if (success) {
                            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                            startActivity(android.content.Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error: $err", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }
}


//package com.krishna.varunaapp.activities
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.auth.FirebaseAuth
//import com.krishna.varunaapp.databinding.ActivitySignUpBinding
//import com.krishna.varunaapp.models.User
//import com.krishna.varunaapp.utils.FirebaseUtils
//
//class SignUpActivity : AppCompatActivity() {
//    private lateinit var binding: ActivitySignUpBinding
//    private lateinit var auth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySignUpBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
//
//        binding.btnSignup.setOnClickListener {
//            val username = binding.etUsername.text.toString().trim()
//            val mobile = binding.etMobile.text.toString().trim()
//            val email = binding.etEmail.text.toString().trim()
//            val password = binding.etPassword.text.toString().trim()
////            val confirmPassword = binding.etVerifyPassword.text.toString().trim()
//            val state = binding.etState.text.toString().trim()
//            val village = binding.etVillage.text.toString().trim()
//            val pincode = binding.etPincode.text.toString().trim()
//
////            if (password != confirmPassword) {
////                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
////                return@setOnClickListener
////            }
//
//            auth.createUserWithEmailAndPassword(email, password)
//                .addOnSuccessListener { result ->
//                    val uid = result.user?.uid ?: return@addOnSuccessListener
//                    val user = User(username, mobile, email, "GeneralUser", state, village, pincode)
//
//                    FirebaseUtils.createUserDocument(uid, user.toMap()) { success, err ->
//                        if (success) {
//                            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
//                            startActivity(android.content.Intent(this, LoginActivity::class.java))
//                            finish()
//                        } else {
//                            Toast.makeText(this, "Error: $err", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//                .addOnFailureListener {
//                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//}
//

//package com.krishna.varunaapp.activities
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.auth.FirebaseAuth
//import com.krishna.varunaapp.databinding.ActivitySignUpBinding
//import com.krishna.varunaapp.models.User
//import com.krishna.varunaapp.utils.FirebaseUtils
//
//
//class SignUpActivity : AppCompatActivity() {
//    private lateinit var binding: ActivitySignUpBinding
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySignUpBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//        binding.btnSignup.setOnClickListener {
//            val email = binding.etEmail.text.toString().trim()
//            val pwd = binding.etPassword.text.toString().trim()
//            val username = binding.etUsername.text.toString().trim()
//            val mobile = binding.etMobile.text.toString().trim()
//            val state = binding.etState.text.toString().trim()
//            val village = binding.etVillage.text.toString().trim()
//            val pincode = binding.etPincode.text.toString().trim()
//
//
//            if (email.isEmpty() || pwd.length < 6) {
//                Toast.makeText(this, "Enter valid credentials", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//
//            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pwd)
//                .addOnSuccessListener { authRes ->
//                    val uid = authRes.user!!.uid
//                    val user = User(username, mobile, email, "GeneralUser", state, village, pincode)
//                    FirebaseUtils.createUserDocument(uid, mapOf(
//                        "username" to user.username,
//                        "mobile" to user.mobile,
//                        "email" to user.email,
//                        "role" to user.role,
//                        "state" to user.state,
//                        "village" to user.village,
//                        "pincode" to user.pincode
//                    )) { success, err ->
//                        if (success) {
//                            startActivity(android.content.Intent(this, MainActivity::class.java))
//                            finish()
//                        } else Toast.makeText(this, "Error: $err", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .addOnFailureListener { e -> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show() }
//        }
//    }
//}
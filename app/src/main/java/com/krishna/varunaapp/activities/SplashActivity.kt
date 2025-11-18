package com.krishna.varunaapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.krishna.varunaapp.utils.FirebaseUtils


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// minimal blank layout
        startActivity(
            Intent(this, if (FirebaseUtils.currentUser() != null) MainActivity::class.java else LoginActivity::class.java)
        )
        finish()
    }
}
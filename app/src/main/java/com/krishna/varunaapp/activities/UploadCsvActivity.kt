package com.krishna.varunaapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.krishna.varunaapp.databinding.ActivityUploadCsvBinding

class UploadCsvActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadCsvBinding
    private var villageName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadCsvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        villageName = intent.getStringExtra("villageName") ?: ""
    }
}

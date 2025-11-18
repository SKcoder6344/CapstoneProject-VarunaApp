package com.krishna.varunaapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.krishna.varunaapp.databinding.ActivityWaterDataBinding

class WaterDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaterDataBinding
    private var villageName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaterDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        villageName = intent.getStringExtra("villageName") ?: ""
    }
}

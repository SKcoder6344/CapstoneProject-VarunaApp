package com.krishna.varunaapp

import android.app.Application
import com.google.firebase.FirebaseApp

class VarunaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

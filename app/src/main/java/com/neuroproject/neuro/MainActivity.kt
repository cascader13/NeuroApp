package com.neuroproject.neuro

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gelo.capsule.CapsuleNative
import com.gyf.immersionbar.ktx.immersionBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        immersionBar {
            transparentStatusBar()
            transparentNavigationBar()
        }
        CapsuleNative.initCapsule()
        CapsuleNative.requestPermissions(this)

        setContent {
            NeuroApplication()
        }
    }
}
 
package com.controlprestamos

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import com.controlprestamos.core.app.AppDependencies
import com.controlprestamos.core.ui.theme.ControlPrestamosTheme
import com.controlprestamos.core.navigation.AppNavGraph

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppDependencies.initialize(applicationContext)

        setContent {
            ControlPrestamosTheme {
                AppNavGraph()
            }
        }
    }
}





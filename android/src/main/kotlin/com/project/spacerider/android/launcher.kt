package com.project.spacerider.android

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.project.spacerider.Game

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize(Game(), AndroidApplicationConfiguration().apply {
            hideStatusBar = true
            useImmersiveMode = true
        })
    }
}

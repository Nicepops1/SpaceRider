package com.project.spacerider.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.project.spacerider.Game

fun main() {
    Lwjgl3Application(
        com.project.spacerider.Game(),
        Lwjgl3ApplicationConfiguration().apply {
            setTitle("Space Rider")
            setWindowSizeLimits(360, 640, -1, -1)
            setWindowedMode(360, 640)
            setWindowIcon("icon.png")
        })
}

package com.project.spacerider.screen

import com.project.spacerider.Game
import com.project.spacerider.asset.MusicAsset
import com.project.spacerider.ui.GameOverUI
import ktx.actors.onClick
import ktx.actors.plusAssign

class GameOverScreen(game: Game) : Screen(game, MusicAsset.GAME_OVER) {
    private val ui = GameOverUI(bundle).apply {
        backButton.onClick {
            game.setScreen<MenuScreen>()
        }
    }
    var score = 0
    var highScore = 0

    override fun show() {
        super.show()

        ui.run {
            updateScores(score, highScore)
            stage += this.table
        }
    }

    override fun render(delta: Float) {
        engine.update(delta)
        stage.run {
            viewport.apply()
            act(delta)
            draw()
        }
    }
}

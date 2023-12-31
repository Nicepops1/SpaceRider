package com.project.spacerider.screen

import com.badlogic.gdx.Gdx
import com.project.spacerider.Game
import com.project.spacerider.PREFERENCE_HIGHSCORE_KEY
import com.project.spacerider.PREFERENCE_MUSIC_ENABLED_KEY
import com.project.spacerider.asset.MusicAsset
import com.project.spacerider.ecs.createSpaceRider
import com.project.spacerider.ecs.createPlayer
import com.project.spacerider.ecs.system.MoveSystem
import com.project.spacerider.ecs.system.PlayerAnimationSystem
import com.project.spacerider.ecs.system.PowerUpSystem
import com.project.spacerider.ui.ConfirmDialog
import com.project.spacerider.ui.MenuUI
import com.project.spacerider.ui.TextDialog
import ktx.actors.onChangeEvent
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.ashley.getSystem
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set

private const val PLAYER_SPAWN_Y = 3f

class MenuScreen(game: Game) : Screen(game, MusicAsset.MENU) {
    private val preferences = game.preferences
    private val ui = MenuUI(bundle).apply {
        startGameButton.onClick { game.setScreen<GameScreen>() }
        soundButton.onChangeEvent {
            audioService.enabled = !this.isChecked
            preferences.flush {
                this[PREFERENCE_MUSIC_ENABLED_KEY] = audioService.enabled
            }
        }
        controlButton.onClick {
            controlsDialog.show(stage)
        }
        quitGameButton.onClick {
            confirmDialog.show(stage)
        }
    }
    private val confirmDialog = ConfirmDialog(bundle).apply {
        yesButton.onClick { Gdx.app.exit() }
        noButton.onClick { hide() }
    }
    private val controlsDialog = TextDialog(bundle, "controls")

    override fun show() {
        super.show()
        engine.run {
            createPlayer(assets, spawnY = PLAYER_SPAWN_Y)
            createSpaceRider()
        }
        audioService.enabled = preferences[PREFERENCE_MUSIC_ENABLED_KEY, true]
        setupUI()
    }

    private fun setupUI() {
        ui.run {
            soundButton.isChecked = !audioService.enabled
            updateHighScore(preferences[PREFERENCE_HIGHSCORE_KEY, 0])
            stage += this.table
        }
    }

    override fun hide() {
        super.hide()
        engine.run {
            getSystem<PowerUpSystem>().setProcessing(true)
            getSystem<MoveSystem>().setProcessing(true)
            getSystem<PlayerAnimationSystem>().setProcessing(true)
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

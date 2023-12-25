package com.project.spacerider.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.project.spacerider.Game
import com.project.spacerider.PREFERENCE_HIGHSCORE_KEY
import com.project.spacerider.asset.I18NBundleAsset
import com.project.spacerider.asset.MusicAsset
import com.project.spacerider.asset.SoundAsset.SPAWN
import com.project.spacerider.ecs.PLAYER_START_SPEED
import com.project.spacerider.ecs.component.MAX_LIFE
import com.project.spacerider.ecs.component.MAX_SHIELD
import com.project.spacerider.ecs.component.PlayerComponent
import com.project.spacerider.ecs.component.PowerUpType
import com.project.spacerider.ecs.createDarkMatter
import com.project.spacerider.ecs.createPlayer
import com.project.spacerider.ecs.system.MoveSystem
import com.project.spacerider.ecs.system.PlayerAnimationSystem
import com.project.spacerider.ecs.system.PowerUpSystem
import com.project.spacerider.ecs.system.RenderSystem
import com.project.spacerider.event.GameEvent
import com.project.spacerider.event.GameEventListener
import com.project.spacerider.ui.GameUI
import ktx.actors.onChangeEvent
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.ashley.get
import ktx.ashley.getSystem
import ktx.log.debug
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import kotlin.math.min
import kotlin.math.roundToInt

private val LOG = logger<GameScreen>()
private const val MAX_DELTA_TIME = 1 / 30f

class GameScreen(game: com.project.spacerider.Game) : Screen(game, MusicAsset.GAME), GameEventListener {
    private val ui = GameUI(assets[I18NBundleAsset.DEFAULT.descriptor]).apply {
        quitImageButton.onClick {
            game.setScreen<MenuScreen>()
        }
        pauseResumeButton.onChangeEvent {
            when (this.isChecked) {
                true -> audioService.pause()
                else -> audioService.resume()
            }
        }
    }
    private val renderSystem = game.engine.getSystem<RenderSystem>()
    private val preferences = game.preferences

    override fun show() {
        super.show()
        gameEventManager.run {
            addListener(GameEvent.PlayerSpawn::class, this@GameScreen)
            addListener(GameEvent.PlayerDeath::class, this@GameScreen)
            addListener(GameEvent.PlayerMove::class, this@GameScreen)
            addListener(GameEvent.PlayerHit::class, this@GameScreen)
            addListener(GameEvent.PowerUp::class, this@GameScreen)
            addListener(GameEvent.PlayerBlock::class, this@GameScreen)
        }
        engine.run {
            // remove any power ups and reset the spawn timer
            getSystem<PowerUpSystem>().run {
                setProcessing(true)
                reset()
            }
            getSystem<MoveSystem>().setProcessing(true)
            getSystem<PlayerAnimationSystem>().setProcessing(true)
            createPlayer(assets)
            audioService.play(SPAWN)
            gameEventManager.dispatchEvent(GameEvent.PlayerSpawn)
            createDarkMatter()
        }
        setupUI()
    }

    override fun hide() {
        super.hide()
        engine.run {
            getSystem<PowerUpSystem>().setProcessing(false)
            getSystem<MoveSystem>().setProcessing(false)
            getSystem<PlayerAnimationSystem>().setProcessing(false)
        }
    }

    private fun setupUI() {
        ui.run {
            // reset to initial values
            updateDistance(0f)
            updateSpeed(PLAYER_START_SPEED)
            updateLife(
                MAX_LIFE,
                MAX_LIFE
            )
            updateShield(0f, MAX_SHIELD)

            // disable pauseResume button until game was started
            pauseResumeButton.run {
                this.touchable = Touchable.disabled
                this.isChecked = false
            }
            touchToBeginLabel.isVisible = true
        }
        stage += ui
    }

    override fun render(delta: Float) {
        if (Gdx.input.justTouched() && ui.touchToBeginLabel.isVisible) {
            ui.touchToBeginLabel.isVisible = false
            ui.pauseResumeButton.touchable = Touchable.enabled
        }

        val deltaTime = min(delta, MAX_DELTA_TIME)
        if (ui.pauseResumeButton.isChecked || ui.touchToBeginLabel.isVisible) {
            renderSystem.update(0f)
        } else {
            engine.update(deltaTime)
            audioService.update()
        }

        // render UI
        stage.run {
            viewport.apply()
            act(deltaTime)
            draw()
        }
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.PlayerSpawn -> {
                LOG.debug { "Spawn new player" }
                ui.updateDistance(0f)
            }
            is GameEvent.PlayerDeath -> {
                onPlayerDeath(event)
            }
            is GameEvent.PlayerMove -> {
                ui.run {
                    updateDistance(event.distance)
                    updateSpeed(event.speed)
                }
            }
            is GameEvent.PlayerHit -> {
                ui.run {
                    updateLife(event.life, event.maxLife)
                    showWarning()
                }
            }
            is GameEvent.PowerUp -> {
                onPlayerPowerUp(event)
            }
            is GameEvent.PlayerBlock -> {
                ui.updateShield(event.shield, event.maxShield)
            }
        }
    }

    private fun onPlayerPowerUp(event: GameEvent.PowerUp) {
        event.player[PlayerComponent.mapper]?.let { player ->
            when (event.type) {
                PowerUpType.LIFE -> ui.updateLife(player.life, player.maxLife)
                PowerUpType.SHIELD -> ui.updateShield(player.shield, player.maxShield)
                else -> {
                    // ignore
                }
            }
        }
    }

    private fun onPlayerDeath(event: GameEvent.PlayerDeath) {
        val distance = event.distance.roundToInt()
        LOG.debug { "Player died with a distance of $distance" }
        if (distance > preferences[com.project.spacerider.PREFERENCE_HIGHSCORE_KEY, 0]) {
            preferences.flush {
                this[com.project.spacerider.PREFERENCE_HIGHSCORE_KEY] = distance
            }
        }
        game.getScreen<GameOverScreen>().run {
            score = distance
            highScore = preferences[com.project.spacerider.PREFERENCE_HIGHSCORE_KEY, 0]
        }
        game.setScreen<GameOverScreen>()
    }
}

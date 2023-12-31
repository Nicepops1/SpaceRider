package com.project.spacerider.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.project.spacerider.asset.SoundAsset
import com.project.spacerider.audio.AudioService
import com.project.spacerider.ecs.component.PlayerComponent
import com.project.spacerider.ecs.component.PowerUpType
import com.project.spacerider.ecs.component.TransformComponent
import com.project.spacerider.event.GameEvent
import com.project.spacerider.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.getSystem
import kotlin.math.max
import kotlin.math.min

private const val SHIELD_GAIN = 25f
private const val PLAYER_DAMAGE = 5f
private const val NUM_SOUNDS_TO_TEST = 3
private const val WINDOW_INFO_UPDATE_INTERVAL = 0.25f

class DebugSystem(
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService
) : IntervalIteratingSystem(allOf(PlayerComponent::class).get(), WINDOW_INFO_UPDATE_INTERVAL) {
    init {
        setProcessing(false)
    }

    override fun processEntity(entity: Entity) {
        entity[PlayerComponent.mapper]?.let { player ->
            entity[TransformComponent.mapper]?.let { transform ->
                when {
                    Gdx.input.isKeyPressed(Input.Keys.NUM_1) -> {
                        // убийство игрока
                        transform.position.y = 1f
                        player.life = 1f
                        player.shield = 0f
                    }
                    Gdx.input.isKeyPressed(Input.Keys.NUM_2) -> {
                        // применение щита
                        player.shield = min(player.maxShield, player.shield + SHIELD_GAIN)
                        gameEventManager.dispatchEvent(GameEvent.PowerUp.apply {
                            type = PowerUpType.SHIELD
                            this.player = entity
                        })
                    }
                    Gdx.input.isKeyPressed(Input.Keys.NUM_3) -> {
                        // снять щит
                        player.shield = max(0f, player.shield - SHIELD_GAIN)
                        gameEventManager.dispatchEvent(GameEvent.PlayerBlock.apply {
                            shield = player.shield
                            maxShield = player.maxShield
                        })
                    }
                    Gdx.input.isKeyPressed(Input.Keys.NUM_4) -> {
                        // отключение управления
                        engine.getSystem<MoveSystem>().setProcessing(false)
                    }
                    Gdx.input.isKeyPressed(Input.Keys.NUM_5) -> {
                        // включение управления
                        engine.getSystem<MoveSystem>().setProcessing(true)
                    }
                    Gdx.input.isKeyPressed(Input.Keys.NUM_6) -> {
                        // эффект получения урона
                        player.life = max(1f, player.life - PLAYER_DAMAGE)
                        gameEventManager.dispatchEvent(GameEvent.PlayerHit.apply {
                            this.player = entity
                            life = player.life
                            maxLife = player.maxLife
                        })
                    }
                    Gdx.input.isKeyPressed(Input.Keys.NUM_7) -> {
                        // восстановление здоровья
                        engine.getSystem<PowerUpSystem>()
                            .spawnPowerUp(PowerUpType.LIFE, transform.position.x, transform.position.y)
                    }
                    Gdx.input.isKeyPressed(Input.Keys.NUM_8) -> {
                        // получение энергии
                        engine.getSystem<PowerUpSystem>()
                            .spawnPowerUp(PowerUpType.SPEED_1, transform.position.x, transform.position.y)
                    }
                    Gdx.input.isKeyPressed(Input.Keys.NUM_9) -> {
                        // проигрывание 3 звуков
                        repeat(NUM_SOUNDS_TO_TEST) {
                            audioService.play(SoundAsset.values()[MathUtils.random(0, SoundAsset.values().size - 1)])
                        }
                    }
                }

                Gdx.graphics.setTitle(
                    "Space Rider Debug - pos:${transform.position}, life:${player.life}, shield:${player.shield}"
                )
            }
        }
    }
}

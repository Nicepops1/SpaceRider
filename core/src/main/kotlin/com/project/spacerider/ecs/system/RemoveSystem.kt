package com.project.spacerider.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.project.spacerider.ecs.component.PlayerComponent
import com.project.spacerider.ecs.component.RemoveComponent
import com.project.spacerider.event.GameEvent
import com.project.spacerider.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.get

class RemoveSystem(
    private val gameEventManager: GameEventManager
) : IteratingSystem(allOf(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val remove = entity[RemoveComponent.mapper]
        require(remove != null) { "Entity |entity| must have a RemoveComponent. entity=$entity" }

        remove.delay -= deltaTime
        if (remove.delay <= 0f) {
            entity[PlayerComponent.mapper]?.let { player ->
                gameEventManager.dispatchEvent(GameEvent.PlayerDeath.apply {
                    distance = player.distance
                })
            }

            engine.removeEntity(entity)
        }
    }
}

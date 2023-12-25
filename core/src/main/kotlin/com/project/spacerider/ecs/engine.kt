package com.project.spacerider.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.project.spacerider.asset.TextureAtlasAsset
import com.project.spacerider.ecs.component.*
import com.project.spacerider.ecs.component.AnimationType.DARK_MATTER
import com.project.spacerider.ecs.component.AnimationType.FIRE
import com.project.spacerider.ecs.system.DAMAGE_AREA_HEIGHT
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.async.AssetStorage

private const val SHIP_FIRE_OFFSET_X = 1f // in pixels
private const val SHIP_FIRE_OFFSET_Y = -6f // in pixels
const val PLAYER_START_SPEED = 3f

fun Engine.createPlayer(
    assets: AssetStorage,
    spawnX: Float = com.project.spacerider.V_WIDTH * 0.5f,
    spawnY: Float = com.project.spacerider.V_HEIGHT * 0.5f
): Entity {
    // ship
    val ship = entity {
        with<PlayerComponent>()
        with<FacingComponent>()
        with<MoveComponent> {
            speed.y = PLAYER_START_SPEED
        }
        with<TransformComponent> {
            val atlas = assets[TextureAtlasAsset.GRAPHICS.descriptor]
            val playerGraphicRegion = atlas.findRegion("ship_base")
            size.set(
                playerGraphicRegion.originalWidth * com.project.spacerider.UNIT_SCALE,
                playerGraphicRegion.originalHeight * com.project.spacerider.UNIT_SCALE
            )
            setInitialPosition(
                spawnX - size.x * 0.5f,
                spawnY - size.y * 0.5f,
                1f
            )
        }
        with<GraphicComponent>()
    }

    entity {
        with<TransformComponent>()
        with<AttachComponent> {
            entity = ship
            offset.set(
                SHIP_FIRE_OFFSET_X * com.project.spacerider.UNIT_SCALE,
                SHIP_FIRE_OFFSET_Y * com.project.spacerider.UNIT_SCALE
            )
        }
        with<GraphicComponent>()
        with<AnimationComponent> {
            type = FIRE
        }
    }

    return ship
}

fun Engine.createSpaceRider() {
    entity {
        with<TransformComponent> {
            size.set(
                com.project.spacerider.V_WIDTH.toFloat(),
                DAMAGE_AREA_HEIGHT
            )
        }
        with<AnimationComponent> {
            type = DARK_MATTER
        }
        with<GraphicComponent>()
    }
}

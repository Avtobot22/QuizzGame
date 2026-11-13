package com.quizzgame.core.navigation.chrome

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth

internal fun Modifier.cacheChromeSize(useCache: Boolean): Modifier = then(CacheChromeSizeElement(useCache))

private data class CacheChromeSizeElement(val useCache: Boolean) : ModifierNodeElement<CacheChromeSizeNode>() {
    override fun create() = CacheChromeSizeNode(useCache)
    override fun update(node: CacheChromeSizeNode) {
        if (node.useCache != useCache) {
            node.useCache = useCache
            node.invalidateMeasurement()
        }
    }
    override fun InspectorInfo.inspectableProperties() { name = "cacheChromeSize" }
}

private class CacheChromeSizeNode(var useCache: Boolean) : Modifier.Node(), LayoutModifierNode {
    private var cachedSize: IntSize? = null

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val child = measurable.measure(constraints)
        val size = if (useCache) {
            cachedSize ?: IntSize(child.width, child.height)
        } else {
            IntSize(child.width, child.height)
        }
        cachedSize = size
        return layout(constraints.constrainWidth(size.width), constraints.constrainHeight(size.height)) {
            child.placeRelative(0, 0)
        }
    }
}

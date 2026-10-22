package com.kuikly.starrating

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StarRatingInteractionTest {
    @Test
    fun initialAndRepeatedPropsDoNotEmit() {
        val model = model()
        assertNull(model.applyControlled(3.7f, 5, allowHalf = true))
        assertEquals(3.7f, model.rawRating)
        assertClose(3.5f, model.displayed)
        assertNull(model.applyControlled(3.7f, 5, allowHalf = true))
        assertEquals(3.7f, model.rawRating)
    }

    @Test
    fun halfStarToggleEmitsOnceAndEchoDoesNotLoop() {
        val model = model()
        model.applyControlled(3.5f, 5, allowHalf = true)
        assertEquals(4f, model.applyControlled(3.5f, 5, allowHalf = false))
        assertEquals(3.5f, model.rawRating)
        assertClose(4f, model.displayed)
        assertNull(model.applyControlled(4f, 5, allowHalf = false))
        assertEquals(4f, model.rawRating)
        assertNull(model.applyControlled(3.5f, 5, allowHalf = false))
        assertClose(4f, model.displayed)
    }

    @Test
    fun maxStarsShrinkEmitsClampedRating() {
        val model = model()
        model.applyControlled(5f, 5, allowHalf = true)
        assertEquals(3f, model.applyControlled(5f, 3, allowHalf = true))
        assertClose(3f, model.displayed)
    }

    @Test
    fun movePreviewsAndUpCommitsOnce() {
        val model = model()
        model.applyControlled(0f, 5, allowHalf = true)
        model.onDown(0f)
        assertTrue(model.isDragging)
        assertClose(0.5f, model.displayed)
        assertEquals(0f, model.rawRating)
        model.onMove(20f)
        assertClose(2.5f, model.displayed)
        assertEquals(0f, model.rawRating)
        assertEquals(2.5f, model.onUp(20f))
        assertFalse(model.isDragging)
        assertEquals(2.5f, model.rawRating)
        assertClose(2.5f, model.displayed)
        assertNull(model.onUp(20f))
    }

    @Test
    fun cancelRestoresCommittedRating() {
        val model = model()
        model.applyControlled(0f, 5, allowHalf = true)
        model.onDown(0f)
        assertEquals(1.5f, model.onUp(10f))
        model.onDown(0f)
        model.onMove(30f)
        assertClose(3.5f, model.displayed)
        model.onCancel()
        assertFalse(model.isDragging)
        assertEquals(1.5f, model.rawRating)
        assertClose(1.5f, model.displayed)
    }

    @Test
    fun sameSlotUpDoesNotEmitButCanonicalizesRaw() {
        val model = model()
        model.applyControlled(3.7f, 5, allowHalf = true)
        assertClose(3.5f, model.displayed)
        model.onDown(30f)
        assertNull(model.onUp(30f))
        assertEquals(3.5f, model.rawRating)
        assertClose(3.5f, model.displayed)
    }

    @Test
    fun readOnlyIgnoresTouches() {
        val model = model()
        model.applyControlled(1f, 5, allowHalf = true)
        model.readOnly = true
        model.onDown(30f)
        model.onMove(40f)
        assertFalse(model.isDragging)
        assertNull(model.onUp(40f))
        assertEquals(1f, model.rawRating)
        assertClose(1f, model.displayed)
    }

    @Test
    fun setRatingEmitsSnappedValueAndCancelsPreview() {
        val model = model()
        model.applyControlled(0f, 5, allowHalf = true)
        model.onDown(0f)
        assertTrue(model.isDragging)
        assertEquals(3.5f, model.setRating(3.7f))
        assertFalse(model.isDragging)
        assertEquals(3.7f, model.rawRating)
        assertClose(3.5f, model.displayed)
        assertNull(model.setRating(3.6f))
        assertClose(3.5f, model.displayed)
    }

    @Test
    fun widthAndHeightMarkUserSize() {
        val attr = StarRatingAttr()
        assertFalse(attr.userSetWidth)
        assertFalse(attr.userSetHeight)
        attr.width(20f)
        attr.height(10f)
        assertTrue(attr.userSetWidth)
        assertTrue(attr.userSetHeight)
    }

    private fun model(): StarRatingInteraction {
        return StarRatingInteraction().apply {
            starSize = 10f
            starSpacing = 0f
        }
    }

    private fun assertClose(expected: Float, actual: Float) {
        assertTrue(
            kotlin.math.abs(expected - actual) < 0.001f,
            "expected $expected but was $actual",
        )
    }
}

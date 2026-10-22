package com.kuikly.starrating

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StarRatingGeometryTest {
    @Test
    fun contentWidthIncludesGapsBetweenStars() {
        assertEquals(164f, StarRatingGeometry.contentWidth(5, 28f, 6f))
        assertEquals(28f, StarRatingGeometry.contentWidth(1, 28f, 6f))
    }

    @Test
    fun maxStarsIsClamped() {
        assertEquals(1, StarRatingGeometry.normalizeMaxStars(0))
        assertEquals(20, StarRatingGeometry.normalizeMaxStars(100))
    }

    @Test
    fun touchMapsWholeAndHalfSlots() {
        assertClose(0.5f, touch(0f, allowHalf = true))
        assertClose(0.5f, touch(4.9f, allowHalf = true))
        assertClose(1f, touch(5f, allowHalf = true))
        assertClose(1f, touch(9.9f, allowHalf = true))
        assertClose(1.5f, touch(10f, allowHalf = true))
        assertClose(5f, touch(50f, allowHalf = true))
        assertClose(0f, touch(-1f, allowHalf = true))
    }

    @Test
    fun touchWithoutHalfUsesWholeStars() {
        assertClose(1f, touch(0f, allowHalf = false))
        assertClose(1f, touch(9.9f, allowHalf = false))
        assertClose(2f, touch(10f, allowHalf = false))
        assertClose(5f, touch(49f, allowHalf = false))
    }

    @Test
    fun gapSnapsToNearestStar() {
        // size 10, spacing 4, pitch 14. gap after star 0 is [10, 14).
        assertClose(1f, touch(10f, allowHalf = true, spacing = 4f))
        assertClose(1f, touch(12f, allowHalf = true, spacing = 4f))
        assertClose(1.5f, touch(12.1f, allowHalf = true, spacing = 4f))
        assertClose(2f, touch(12.1f, allowHalf = false, spacing = 4f))
    }

    @Test
    fun snapRatingUsesHalfStepsOnlyWhenEnabled() {
        assertClose(3.5f, StarRatingGeometry.snapRating(3.7f, 5, allowHalf = true))
        assertClose(4f, StarRatingGeometry.snapRating(3.7f, 5, allowHalf = false))
        assertClose(0f, StarRatingGeometry.snapRating(-2f, 5, allowHalf = true))
        assertClose(5f, StarRatingGeometry.snapRating(9f, 5, allowHalf = true))
        assertEquals("0", StarRatingGeometry.formatRating(0f))
        assertEquals("3.5", StarRatingGeometry.formatRating(3.5f))
        assertEquals("4", StarRatingGeometry.formatRating(4f))
    }

    private fun touch(
        x: Float,
        allowHalf: Boolean,
        spacing: Float = 0f,
    ): Float {
        return StarRatingGeometry.ratingFromTouch(
            x = x,
            maxStars = 5,
            starSize = 10f,
            starSpacing = spacing,
            allowHalf = allowHalf,
        )
    }

    private fun assertClose(expected: Float, actual: Float) {
        assertTrue(
            kotlin.math.abs(expected - actual) < 0.001f,
            "expected $expected but was $actual",
        )
    }
}

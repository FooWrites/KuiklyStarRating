package com.kuikly.starrating

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

    @Test
    fun contentSizeStaysContentSizedInsideLooseConstraints() {
        assertEquals(164, StarRatingGeometry.resolveContentSize(164, 0, 1080, bounded = true))
        assertEquals(28, StarRatingGeometry.resolveContentSize(28, 0, 1080, bounded = true))
    }

    @Test
    fun contentSizeUsesTightConstraintsAndUnboundedAxis() {
        assertEquals(200, StarRatingGeometry.resolveContentSize(164, 200, 200, bounded = true))
        assertEquals(100, StarRatingGeometry.resolveContentSize(164, 0, 100, bounded = true))
        assertEquals(164, StarRatingGeometry.resolveContentSize(164, 0, Int.MAX_VALUE, bounded = false))
        assertEquals(50, StarRatingGeometry.resolveContentSize(40, 50, Int.MAX_VALUE, bounded = false))
    }

    @Test
    fun constraintChangeEmitsOnlyWhenSnapMoves() {
        val half = StarRatingConstraints.of(5, allowHalf = true)
        val whole = StarRatingConstraints.of(5, allowHalf = false)
        assertNull(StarRatingGeometry.ratingAfterConstraintChange(3.5f, previous = null, next = whole))
        assertEquals(4f, StarRatingGeometry.ratingAfterConstraintChange(3.5f, half, whole))
        assertNull(StarRatingGeometry.ratingAfterConstraintChange(4f, whole, whole))
        assertNull(StarRatingGeometry.ratingAfterConstraintChange(4f, half, whole))
        assertEquals(
            3f,
            StarRatingGeometry.ratingAfterConstraintChange(
                5f,
                StarRatingConstraints.of(5, true),
                StarRatingConstraints.of(3, true),
            ),
        )
        assertNull(
            StarRatingGeometry.ratingAfterConstraintChange(
                1f,
                StarRatingConstraints.of(100, true),
                StarRatingConstraints.of(20, true),
            ),
        )
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

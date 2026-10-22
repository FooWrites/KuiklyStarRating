package com.kuikly.starrating

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StarRatingControllerTest {
    @Test
    fun pendingValueIsDeliveredOnceWhenBound() {
        val controller = StarRatingController()
        controller.updateDisplayed(1f)
        assertEquals(1f, controller.rating())
        controller.setRating(1f)
        controller.setRating(4f)
        assertEquals(4f, controller.rating())
        var got: Float? = null
        var calls = 0
        controller.bind {
            calls += 1
            got = it
        }
        assertEquals(1, calls)
        assertEquals(4f, got)
        controller.updateDisplayed(4f)
        assertEquals(4f, controller.rating())
        controller.bind {
            calls += 1
        }
        assertEquals(1, calls)
    }

    @Test
    fun unbindStopsDeliveryUntilNextBind() {
        val controller = StarRatingController()
        var got: Float? = null
        controller.bind { got = it }
        controller.unbind()
        controller.setRating(2f)
        assertNull(got)
        assertEquals(2f, controller.rating())
        controller.bind { got = it }
        assertEquals(2f, got)
    }

    @Test
    fun clearWhileBoundSendsZero() {
        val controller = StarRatingController()
        controller.updateDisplayed(3f)
        var got = -1f
        controller.bind { got = it }
        controller.clear()
        assertEquals(0f, got)
    }
}

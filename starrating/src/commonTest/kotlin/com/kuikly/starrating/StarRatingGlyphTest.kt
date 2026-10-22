package com.kuikly.starrating

import kotlin.test.Test
import kotlin.test.assertEquals

class StarRatingGlyphTest {
    @Test
    fun emptyGlyphFallsBackToDefaultStar() {
        assertEquals("★", DefaultStarGlyph)
        assertEquals(DefaultStarGlyph, normalizeStarGlyph(""))
    }

    @Test
    fun customGlyphIsKept() {
        assertEquals("☆", normalizeStarGlyph("☆"))
        assertEquals("♥", normalizeStarGlyph("♥"))
    }

    @Test
    fun attrRoundTripStoresNormalizedGlyph() {
        val attr = StarRatingAttr()
        assertEquals(DefaultStarGlyph, attr.starGlyph)
        var changes = 0
        attr.onStyleChanged = { changes += 1 }
        attr.starGlyph("☆")
        assertEquals("☆", attr.starGlyph)
        assertEquals(1, changes)
        attr.starGlyph("")
        assertEquals(DefaultStarGlyph, attr.starGlyph)
        assertEquals(2, changes)
    }
}

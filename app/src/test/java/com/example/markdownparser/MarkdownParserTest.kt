package com.example.markdownparser

import com.example.markdownparser.parser.MarkdownElement
import com.example.markdownparser.parser.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {

    private var markdownParser = MarkdownParser()

    @Test
    fun `parse heading level 1`() {
        val result = markdownParser.parse("# Heading 1")
        assertEquals(1, (result[0] as MarkdownElement.Heading).level)
        assertEquals("Heading 1", (result[0] as MarkdownElement.Heading).text)
    }

    @Test
    fun `parse heading level 3`() {
        val result = markdownParser.parse("### Heading 3")
        assertEquals(3, (result[0] as MarkdownElement.Heading).level)
        assertEquals("Heading 3", (result[0] as MarkdownElement.Heading).text)
    }

    @Test
    fun `parse heading with extra spaces`() {
        val result = markdownParser.parse("  ##  Heading with spaces  ")
        assertEquals(2, (result[0] as MarkdownElement.Heading).level)
        assertEquals("Heading with spaces", (result[0] as MarkdownElement.Heading).text)
    }

    @Test
    fun `parse image with alt text`() {
        val result = markdownParser.parse("![Alt Text](image.png)")
        assertEquals("Alt Text", (result[0] as MarkdownElement.Image).altText)
        assertEquals("image.png", (result[0] as MarkdownElement.Image).url)
    }

    @Test
    fun `parse image without alt text`() {
        val result = markdownParser.parse("![](image.png)")
        assertEquals("", (result[0] as MarkdownElement.Image).altText)
        assertEquals("image.png", (result[0] as MarkdownElement.Image).url)
    }

    @Test
    fun `parse plain text`() {
        val result = markdownParser.parse("Simple text line")
        assertEquals("Simple text line", (result[0] as MarkdownElement.Text).text)
        assertFalse((result[0] as MarkdownElement.Text).isBold)
        assertFalse((result[0] as MarkdownElement.Text).isItalic)
        assertFalse((result[0] as MarkdownElement.Text).isStrikethrough)
    }

    @Test
    fun `parse bold text`() {
        val result = markdownParser.parse("**Bold text**")
        assertEquals("Bold text", (result[0] as MarkdownElement.Text).text)
        assertTrue((result[0] as MarkdownElement.Text).isBold)
    }

    @Test
    fun `parse italic text`() {
        val result = markdownParser.parse("*Italic text*")
        assertEquals("Italic text", (result[0] as MarkdownElement.Text).text)
        assertTrue((result[0] as MarkdownElement.Text).isItalic)
    }

    @Test
    fun `parse strikethrough text`() {
        val result = markdownParser.parse("~~Strikethrough~~")
        assertEquals("Strikethrough", (result[0] as MarkdownElement.Text).text)
        assertTrue((result[0] as MarkdownElement.Text).isStrikethrough)
    }

    @Test
    fun `parse mixed formatted text`() {
        val result = markdownParser.parse("_**Bold-Italic**_ ~~strike~~")
        val text = result[0] as MarkdownElement.Text
        assertEquals("Bold-Italic strike", text.text)
        assertTrue(text.isBold)
        assertTrue(text.isItalic)
        assertTrue(text.isStrikethrough)
    }

    @Test
    fun `parse simple table`() {
        val markdown = """
            | Header 1 | Header 2 |
            |----------|----------|
            | Cell 1   | Cell 2   |
            | Cell 3   | Cell 4   |
        """.trimIndent()

        val result = markdownParser.parse(markdown)
        val table = result[0] as MarkdownElement.Table

        assertEquals(2, table.headers.size)
        assertEquals("Header 1", table.headers[0])
        assertEquals("Header 2", table.headers[1])

        assertEquals(2, table.rows.size)
        assertEquals(listOf("Cell 1", "Cell 2"), table.rows[0])
        assertEquals(listOf("Cell 3", "Cell 4"), table.rows[1])
    }

    @Test
    fun `parse table with alignment`() {
        val markdown = """
            | Left | Center | Right |
            |:-----|:------:|------:|
            | A    | B      | C     |
        """.trimIndent()

        val result = markdownParser.parse(markdown)
        val table = result[0] as MarkdownElement.Table

        assertEquals(3, table.headers.size)
        assertEquals("Left", table.headers[0])
        assertEquals("Center", table.headers[1])
        assertEquals("Right", table.headers[2])

        assertEquals(1, table.rows.size)
        assertEquals(listOf("A", "B", "C"), table.rows[0])
    }

    @Test
    fun `parse multiple elements`() {
        val markdown = """
            # Title
            ![Image](logo.png)
            First paragraph
            **Second** paragraph
            
            | Header |
            |--------|
            | Cell   |
        """.trimIndent()

        val result = markdownParser.parse(markdown)

        assertEquals(5, result.size)
        assertTrue(result[0] is MarkdownElement.Heading)
        assertTrue(result[1] is MarkdownElement.Image)
        assertTrue(result[2] is MarkdownElement.Text)
        assertTrue(result[3] is MarkdownElement.Text)
        assertTrue(result[4] is MarkdownElement.Table)
    }

    @Test
    fun `ignore empty lines`() {
        val markdown = """
            # Heading
            
            
            Content after empty lines
            
        """.trimIndent()

        val result = markdownParser.parse(markdown)
        assertEquals(2, result.size)
        assertTrue(result[0] is MarkdownElement.Heading)
        assertTrue(result[1] is MarkdownElement.Text)
    }

    @Test
    fun `parse incomplete image syntax`() {
        val markdown = "![Missing bracket (image.png)"
        val result = markdownParser.parse(markdown)
        val text = result[0] as MarkdownElement.Text

        assertEquals("![Missing bracket (image.png)", text.text)
        assertFalse(text.isBold)
        assertFalse(text.isItalic)
    }

    @Test
    fun `parse table without closing pipe`() {
        val markdown = """
            Header 1 | Header 2
            --- | ---
            Cell 1 | Cell 2
        """.trimIndent()

        val result = markdownParser.parse(markdown)
        // Должен обработаться как таблица, несмотря на отсутствие закрывающих pipes
        assertTrue(result[0] is MarkdownElement.Table)
    }

    @Test
    fun `parse text immediately after table`() {
        val markdown = """
            | Header |
            |--------|
            | Cell   |
            Text after table
        """.trimIndent()

        val result = markdownParser.parse(markdown)
        assertEquals(2, result.size)
        assertTrue(result[0] is MarkdownElement.Table)
        assertTrue(result[1] is MarkdownElement.Text)
    }

    @Test
    fun `parse multiple tables`() {
        val markdown = """
            | Table 1 |
            |---------|
            | Cell 1  |
            
            | Table 2 |
            |---------|
            | Cell 2  |
        """.trimIndent()

        val result = markdownParser.parse(markdown)
        assertEquals(2, result.size)
        assertTrue(result[0] is MarkdownElement.Table)
        assertTrue(result[1] is MarkdownElement.Table)

        assertEquals("Table 1", (result[0] as MarkdownElement.Table).headers[0])
        assertEquals("Table 2", (result[1] as MarkdownElement.Table).headers[0])
    }

    @Test
    fun `parse table with empty cells`() {
        val markdown = """
            | Col1 | Col2 | Col3 |
            |------|------|------|
            |      | Data |      |
            | Data |      | Data |
        """.trimIndent()

        val result = markdownParser.parse(markdown)
        val table = result[0] as MarkdownElement.Table

        assertEquals(2, table.rows.size)
        assertEquals(listOf("", "Data", ""), table.rows[0])
        assertEquals(listOf("Data", "", "Data"), table.rows[1])
    }
}

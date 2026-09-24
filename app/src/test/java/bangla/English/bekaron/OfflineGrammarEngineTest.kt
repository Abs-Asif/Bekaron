package bangla.English.bekaron.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking

class OfflineGrammarEngineTest {

    private lateinit var engine: OfflineGrammarEngine

    @Before
    fun setUp() {
        engine = OfflineGrammarEngine(null)
    }

    @Test
    fun testDetectLanguage_English() {
        val text = "The quick brown fox jumps over the lazy dog."
        val lang = engine.detectLanguage(text)
        assertEquals(SupportedLanguage.ENGLISH, lang)
    }

    @Test
    fun testDetectLanguage_Bangla() {
        val text = "আমি বাংলায় গান গাই।"
        val lang = engine.detectLanguage(text)
        assertEquals(SupportedLanguage.BANGLA, lang)
    }

    @Test
    fun testDetectLanguage_Arabic() {
        val text = "العلم نور والجهل ظلام"
        val lang = engine.detectLanguage(text)
        assertEquals(SupportedLanguage.ARABIC, lang)
    }

    @Test
    fun testGetPOS_English() = runBlocking {
        val results = engine.getPOS("The cat sat on the mat")
        assertTrue(results.isNotEmpty())
        assertEquals("the", results[0].word.lowercase())
        assertEquals("DET", results[0].tag)
    }

    @Test
    fun testGetPOS_Bangla() = runBlocking {
        val results = engine.getPOS("আমি বই পড়ছি")
        assertTrue(results.isNotEmpty())
        assertEquals("PRON", results[0].tag) // "আমি"
    }

    @Test
    fun testGetPOS_Arabic() = runBlocking {
        val results = engine.getPOS("الكتاب على الطاولة")
        assertTrue(results.isNotEmpty())
        assertEquals("NOUN", results[0].tag) // "الكتاب"
        assertEquals("ADP", results[1].tag)  // "على"
    }

    @Test
    fun testFillGap() = runBlocking {
        val sentence = "The sun ___ in the east."
        val options = listOf("rises", "sets", "runs")
        val result = engine.fillGap(sentence, options)

        assertNotNull(result)
        assertEquals(sentence, result.sentenceWithGap)
        assertTrue(result.filledSentence.contains("rises") || result.filledSentence.contains("sets"))
        assertEquals(3, result.options.size)
    }
}

/*
 * Copyright (C) 2026 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * Romanization uses Android ICU transliterators for script conversion and
 * Kuromoji for Japanese readings (kanji pronunciation cannot be inferred
 * reliably from a character-only transliterator).
 */

package com.dd3boh.outertune.lyrics

import android.icu.text.Transliterator
import com.atilika.kuromoji.ipadic.Tokenizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Character.UnicodeScript

/**
 * Language switches used by the lyrics romanization feature.
 *
 * Romanization is deliberately opt-in per script. Latin text is never changed,
 * and punctuation, emoji, and other common/inherited characters are preserved
 * by the transliterators.
 */
data class LyricsRomanizationPreferences(
    val romanizeJapanese: Boolean = true,
    val romanizeKorean: Boolean = true,
    val romanizeChinese: Boolean = true,
    val romanizeHindi: Boolean = true,
    val romanizeOther: Boolean = true,
) {
    val isEnabled: Boolean
        get() = romanizeJapanese || romanizeKorean || romanizeChinese || romanizeHindi || romanizeOther
}

/**
 * Converts non-Latin lyric lines to a readable Latin representation.
 *
 * This is kept separate from the lyric parser: the parser remains the source
 * of truth for timing and the UI can render the generated text as a secondary
 * line without changing seek positions or karaoke word ranges.
 */
object LyricsRomanizer {
    private val whitespaceRegex = Regex("\\s+")
    private val punctuationAfterSpaceRegex = Regex("\\s+([。、，．！？!?.,:;])")

    private const val GENERIC_TRANSLITERATION = "Any-Latin; Latin-ASCII"
    private const val KOREAN_TRANSLITERATION = "Hangul-Latin; Latin-ASCII"

    private val genericTransliterator = ThreadLocal.withInitial {
        Transliterator.getInstance(GENERIC_TRANSLITERATION)
    }
    private val koreanTransliterator = ThreadLocal.withInitial {
        Transliterator.getInstance(KOREAN_TRANSLITERATION)
    }
    private val kuromojiTokenizer = ThreadLocal.withInitial {
        Tokenizer()
    }

    /*
     * Kuromoji returns readings in katakana. Keeping the map local to this
     * feature avoids treating Japanese kanji as Chinese pinyin. Digraphs must
     * be checked before single characters.
     */
    private val kanaRomajiMap = mapOf(
        "キャ" to "kya", "キュ" to "kyu", "キョ" to "kyo",
        "シャ" to "sha", "シュ" to "shu", "ショ" to "sho",
        "チャ" to "cha", "チュ" to "chu", "チョ" to "cho",
        "ニャ" to "nya", "ニュ" to "nyu", "ニョ" to "nyo",
        "ヒャ" to "hya", "ヒュ" to "hyu", "ヒョ" to "hyo",
        "ミャ" to "mya", "ミュ" to "myu", "ミョ" to "myo",
        "リャ" to "rya", "リュ" to "ryu", "リョ" to "ryo",
        "ギャ" to "gya", "ギュ" to "gyu", "ギョ" to "gyo",
        "ジャ" to "ja", "ジュ" to "ju", "ジョ" to "jo",
        "ヂャ" to "ja", "ヂュ" to "ju", "ヂョ" to "jo",
        "ビャ" to "bya", "ビュ" to "byu", "ビョ" to "byo",
        "ピャ" to "pya", "ピュ" to "pyu", "ピョ" to "pyo",
        // Common foreign-word combinations.
        "ティ" to "ti", "ディ" to "di", "トゥ" to "tu", "ドゥ" to "du",
        "ツァ" to "tsa", "ツィ" to "tsi", "ツェ" to "tse", "ツォ" to "tso",
        "ファ" to "fa", "フィ" to "fi", "フェ" to "fe", "フォ" to "fo",
        "ウィ" to "wi", "ウェ" to "we", "ウォ" to "wo",
        "ヴァ" to "va", "ヴィ" to "vi", "ヴェ" to "ve", "ヴォ" to "vo",
        "ア" to "a", "イ" to "i", "ウ" to "u", "エ" to "e", "オ" to "o",
        "ァ" to "a", "ィ" to "i", "ゥ" to "u", "ェ" to "e", "ォ" to "o",
        "カ" to "ka", "キ" to "ki", "ク" to "ku", "ケ" to "ke", "コ" to "ko",
        "サ" to "sa", "シ" to "shi", "ス" to "su", "セ" to "se", "ソ" to "so",
        "タ" to "ta", "チ" to "chi", "ツ" to "tsu", "テ" to "te", "ト" to "to",
        "ナ" to "na", "ニ" to "ni", "ヌ" to "nu", "ネ" to "ne", "ノ" to "no",
        "ハ" to "ha", "ヒ" to "hi", "フ" to "fu", "ヘ" to "he", "ホ" to "ho",
        "マ" to "ma", "ミ" to "mi", "ム" to "mu", "メ" to "me", "モ" to "mo",
        "ヤ" to "ya", "ユ" to "yu", "ヨ" to "yo",
        "ラ" to "ra", "リ" to "ri", "ル" to "ru", "レ" to "re", "ロ" to "ro",
        "ワ" to "wa", "ヮ" to "wa", "ヲ" to "o", "ン" to "n",
        "ガ" to "ga", "ギ" to "gi", "グ" to "gu", "ゲ" to "ge", "ゴ" to "go",
        "ザ" to "za", "ジ" to "ji", "ズ" to "zu", "ゼ" to "ze", "ゾ" to "zo",
        "ダ" to "da", "ヂ" to "ji", "ヅ" to "zu", "デ" to "de", "ド" to "do",
        "バ" to "ba", "ビ" to "bi", "ブ" to "bu", "ベ" to "be", "ボ" to "bo",
        "パ" to "pa", "ピ" to "pi", "プ" to "pu", "ペ" to "pe", "ポ" to "po",
        "ヴ" to "vu", "ヰ" to "wi", "ヱ" to "we",
        "ー" to "",
    )

    private val otherRomanizationExcludedScripts = setOf(
        UnicodeScript.LATIN,
        UnicodeScript.COMMON,
        UnicodeScript.INHERITED,
        UnicodeScript.HAN,
        UnicodeScript.HIRAGANA,
        UnicodeScript.KATAKANA,
        UnicodeScript.HANGUL,
        UnicodeScript.DEVANAGARI,
    )

    /** True when the text contains Japanese kana/iteration marks. */
    fun isJapanese(text: String): Boolean = text.any { char ->
        hasScript(char, UnicodeScript.HIRAGANA) ||
            hasScript(char, UnicodeScript.KATAKANA) ||
            char == '々' || char == '〆' || char == 'ヶ'
    }

    /** True when the text contains Hangul syllables or jamo. */
    fun isKorean(text: String): Boolean = text.any { char ->
        hasScript(char, UnicodeScript.HANGUL)
    }

    /**
     * Han-only text is treated as Chinese. Japanese kanji mixed with kana is
     * handled by [isJapanese] first, which is the reliable distinction that
     * can be made without a language detector.
     */
    fun isChinese(text: String): Boolean {
        if (text.isBlank()) return false
        val hasHan = text.any { hasScript(it, UnicodeScript.HAN) }
        if (!hasHan) return false
        return text.none {
            hasScript(it, UnicodeScript.HIRAGANA) ||
                hasScript(it, UnicodeScript.KATAKANA) ||
                hasScript(it, UnicodeScript.HANGUL)
        }
    }

    fun isHindi(text: String): Boolean = text.any { hasScript(it, UnicodeScript.DEVANAGARI) }

    /** Detect scripts such as Cyrillic, Greek, Arabic, Thai, Bengali, etc. */
    fun hasOtherRomanizableScript(text: String): Boolean = text.any { char ->
        if (!char.isLetter()) return@any false
        UnicodeScript.of(char.code) !in otherRomanizationExcludedScripts
    }

    fun shouldRomanize(
        text: String,
        preferences: LyricsRomanizationPreferences,
    ): Boolean {
        if (!preferences.isEnabled || text.isBlank()) return false
        return when {
            preferences.romanizeJapanese && isJapanese(text) -> true
            preferences.romanizeKorean && isKorean(text) -> true
            preferences.romanizeChinese && isChinese(text) -> true
            preferences.romanizeHindi && isHindi(text) -> true
            preferences.romanizeOther && hasOtherRomanizableScript(text) -> true
            else -> false
        }
    }

    /**
     * Validate a romanized line supplied by a lyrics provider. Provider text
     * is preferred when it targets a script enabled by the user, because it
     * can contain context-aware readings that a local transliterator cannot
     * infer (especially Japanese kanji).
     */
    fun providedRomanizedText(
        originalText: String,
        providerText: String?,
        providerLanguage: String?,
        preferences: LyricsRomanizationPreferences,
    ): String? {
        if (!preferences.isEnabled || originalText.isBlank()) return null
        val normalized = normalizeRomanizedText(originalText, providerText) ?: return null
        val language = providerLanguage
            ?.substringBefore('-')
            ?.substringBefore('_')
            ?.lowercase()

        val enabled = when (language) {
            "ja" -> preferences.romanizeJapanese
            "ko" -> preferences.romanizeKorean
            "zh", "cmn", "yue" -> preferences.romanizeChinese
            "hi", "sa", "mr", "ne" -> preferences.romanizeHindi
            null, "" -> shouldRomanize(originalText, preferences)
            else -> shouldRomanize(originalText, preferences)
        }
        return normalized.takeIf { enabled }
    }

    /**
     * Romanize a complete lyric line. The work is performed off the main
     * thread because Kuromoji and ICU both do non-trivial text processing.
     */
    suspend fun romanize(
        text: String,
        preferences: LyricsRomanizationPreferences,
    ): String? = withContext(Dispatchers.Default) {
        if (!shouldRomanize(text, preferences)) return@withContext null

        val converted = when {
            preferences.romanizeJapanese && isJapanese(text) -> romanizeJapanese(text)
            preferences.romanizeKorean && isKorean(text) -> koreanTransliterator.get().transliterate(text)
            preferences.romanizeChinese && isChinese(text) -> genericTransliterator.get().transliterate(text)
            preferences.romanizeHindi && isHindi(text) -> genericTransliterator.get().transliterate(text)
            preferences.romanizeOther && hasOtherRomanizableScript(text) -> genericTransliterator.get().transliterate(text)
            else -> null
        }

        normalizeRomanizedText(text, converted)
    }

    /**
     * Romanize a timed word while using its complete line for script
     * detection. This is useful to callers that render phonetics per word.
     */
    suspend fun romanizeWord(
        word: String,
        lineText: String,
        preferences: LyricsRomanizationPreferences,
    ): String? = withContext(Dispatchers.Default) {
        if (word.isBlank()) return@withContext null
        val converted = when {
            preferences.romanizeJapanese && isJapanese(lineText) -> romanizeJapanese(word)
            preferences.romanizeKorean && isKorean(lineText) -> koreanTransliterator.get().transliterate(word)
            preferences.romanizeChinese && isChinese(lineText) -> genericTransliterator.get().transliterate(word)
            preferences.romanizeHindi && isHindi(lineText) -> genericTransliterator.get().transliterate(word)
            preferences.romanizeOther && hasOtherRomanizableScript(lineText) -> genericTransliterator.get().transliterate(word)
            else -> null
        }
        normalizeRomanizedText(word, converted)
    }

    private fun romanizeJapanese(text: String): String {
        val tokenizer = kuromojiTokenizer.get()
        return text.split('\n', ignoreCase = false, limit = -1).joinToString("\n") { line ->
            if (line.isBlank()) return@joinToString ""
            val tokens = tokenizer.tokenize(line)
            tokens.mapIndexed { index, token ->
                val reading = token.reading
                    ?.takeIf { it.isNotBlank() && it != "*" }
                if (reading != null) {
                    val nextReading = tokens.getOrNull(index + 1)?.reading
                        ?.takeIf { it.isNotBlank() && it != "*" }
                    katakanaToRomaji(reading, nextReading)
                } else {
                    // Preserve Latin words and punctuation. For an unknown
                    // Japanese token, ICU is a better fallback than leaking
                    // the original non-Latin characters into the result.
                    val surface = token.surface
                    if (isJapanese(surface) || isChinese(surface)) {
                        genericTransliterator.get().transliterate(surface)
                    } else {
                        surface
                    }
                }
            }
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .replace(punctuationAfterSpaceRegex, "\$1")
        }
    }

    /** Convert katakana (and hiragana readings) to lower-case romaji. */
    private fun katakanaToRomaji(
        katakana: String,
        nextKatakana: String? = null,
    ): String {
        if (katakana.isEmpty()) return ""
        val out = StringBuilder(katakana.length)
        var index = 0
        while (index < katakana.length) {
            val twoChars = katakana.substring(index, (index + 2).coerceAtMost(katakana.length))
                .map(::toKatakana)
                .joinToString("")
            val mappedTwo = if (twoChars.length == 2) kanaRomajiMap[twoChars] else null
            if (mappedTwo != null) {
                out.append(mappedTwo)
                index += 2
                continue
            }

            val current = toKatakana(katakana[index])
            if (current == 'ッ') {
                val next = katakana.getOrNull(index + 1)?.let(::toKatakana)
                    ?: nextKatakana?.firstOrNull()?.let(::toKatakana)
                if (next != null) {
                    val nextRomaji = kanaRomajiMap[next.toString()]
                    if (!nextRomaji.isNullOrEmpty()) out.append(nextRomaji.first())
                }
                index++
                continue
            }

            out.append(kanaRomajiMap[current.toString()] ?: current)
            index++
        }
        return out.toString().lowercase()
    }

    private fun normalizeRomanizedText(original: String, romanized: String?): String? {
        val normalized = romanized
            ?.split('\n', ignoreCase = false, limit = -1)
            ?.joinToString("\n") { it.replace(whitespaceRegex, " ").trim() }
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return normalized.takeUnless {
            it.equals(original.trim(), ignoreCase = true) ||
                it.none { char -> hasScript(char, UnicodeScript.LATIN) }
        }
    }

    private fun toKatakana(char: Char): Char =
        if (char in '\u3041'..'\u3096') (char.code + 0x60).toChar() else char

    private fun hasScript(char: Char, script: UnicodeScript): Boolean =
        char.isLetter() && UnicodeScript.of(char.code) == script
}

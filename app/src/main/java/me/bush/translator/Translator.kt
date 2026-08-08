package me.bush.translator

class Language(val code: String)

class Translation(val translatedText: String)

class Translator {
    fun translateBlocking(text: String, targetLanguage: Language): Translation {
        return Translation(text)
    }
}

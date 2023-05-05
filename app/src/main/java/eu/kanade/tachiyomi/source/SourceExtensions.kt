package eu.kanade.tachiyomi.source

import android.graphics.drawable.Drawable
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.extension.ExtensionManager
import tachiyomi.domain.source.model.StubSource
import tachiyomi.source.local.isLocal
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

fun Source.icon(): Drawable? = Injekt.get<ExtensionManager>().getAppIconForSource(this.id)

fun Source.getPreferenceKey(): String = "source_$id"

fun Source.toStubSource(): StubSource = StubSource(id = id, lang = lang, name = name)

fun Source.getNameForMangaInfo(): String {
    val preferences = Injekt.get<SourcePreferences>()
    val enabledLanguages = preferences.enabledLanguages().get()
        .filterNot { it in listOf("all", "other") }
    val hasOneActiveLanguages = enabledLanguages.size == 1
    val isInEnabledLanguages = lang in enabledLanguages
    return when {
        // For edge cases where user disables a source they got manga of in their library.
        hasOneActiveLanguages && !isInEnabledLanguages -> toString()
        // Hide the language tag when only one language is used.
        hasOneActiveLanguages && isInEnabledLanguages -> name
        else -> toString()
    }
}

fun Source.isLocalOrStub(): Boolean = isLocal() || this is StubSource

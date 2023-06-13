package eu.kanade.presentation.more.settings.screen

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.os.LocaleListCompat
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.TabletUiMode
import eu.kanade.domain.ui.model.ThemeMode
import eu.kanade.domain.ui.model.setAppCompatDelegateThemeMode
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.util.collectAsState
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.system.LocaleHelper
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.merge
import org.xmlpull.v1.XmlPullParser
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.Date

object SettingsAppearanceScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    @StringRes
    override fun getTitleRes() = R.string.pref_category_appearance

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val uiPreferences = remember { Injekt.get<UiPreferences>() }

        return listOf(
            getThemeGroup(context = context, uiPreferences = uiPreferences),
            getDisplayGroup(context = context, uiPreferences = uiPreferences),
            getTimestampGroup(uiPreferences = uiPreferences),
        )
    }

    @Composable
    private fun getThemeGroup(
        context: Context,
        uiPreferences: UiPreferences,
    ): Preference.PreferenceGroup {
        val themeModePref = uiPreferences.themeMode()
        val themeMode by themeModePref.collectAsState()
        val appThemePref = uiPreferences.appTheme()
        val amoledPref = uiPreferences.themeDarkAmoled()

        LaunchedEffect(themeMode) {
            setAppCompatDelegateThemeMode(themeMode)
        }

        LaunchedEffect(Unit) {
            merge(appThemePref.changes(), amoledPref.changes())
                .drop(2)
                .collectLatest { (context as? Activity)?.let { ActivityCompat.recreate(it) } }
        }

        return Preference.PreferenceGroup(
            title = stringResource(R.string.pref_category_theme),
            preferenceItems = listOf(
                Preference.PreferenceItem.ListPreference(
                    pref = themeModePref,
                    title = stringResource(R.string.pref_theme_mode),
                    entries = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mapOf(
                            ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                            ThemeMode.LIGHT to stringResource(R.string.theme_light),
                            ThemeMode.DARK to stringResource(R.string.theme_dark),
                        )
                    } else {
                        mapOf(
                            ThemeMode.LIGHT to stringResource(R.string.theme_light),
                            ThemeMode.DARK to stringResource(R.string.theme_dark),
                        )
                    },
                ),
                Preference.PreferenceItem.AppThemePreference(
                    title = stringResource(R.string.pref_app_theme),
                    pref = appThemePref,
                ),
                Preference.PreferenceItem.SwitchPreference(
                    pref = amoledPref,
                    title = stringResource(R.string.pref_dark_theme_pure_black),
                    enabled = themeMode != ThemeMode.LIGHT,
                ),
            ),
        )
    }

    @Composable
    private fun getDisplayGroup(
        context: Context,
        uiPreferences: UiPreferences,
    ): Preference.PreferenceGroup {
        val langs = remember { getLangs(context) }
        var currentLanguage by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag() ?: "") }

        LaunchedEffect(currentLanguage) {
            val locale = if (currentLanguage.isEmpty()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(currentLanguage)
            }
            AppCompatDelegate.setApplicationLocales(locale)
        }

        return Preference.PreferenceGroup(
            title = stringResource(R.string.pref_category_display),
            preferenceItems = listOf(
                Preference.PreferenceItem.BasicListPreference(
                    value = currentLanguage,
                    title = stringResource(R.string.pref_app_language),
                    entries = langs,
                    onValueChanged = { newValue ->
                        currentLanguage = newValue
                        true
                    },
                ),
                Preference.PreferenceItem.ListPreference(
                    pref = uiPreferences.tabletUiMode(),
                    title = stringResource(R.string.pref_tablet_ui_mode),
                    entries = TabletUiMode.values().associateWith { stringResource(it.titleResId) },
                    onValueChanged = {
                        context.toast(R.string.requires_app_restart)
                        true
                    },
                ),
            ),
        )
    }

    @Composable
    private fun getTimestampGroup(uiPreferences: UiPreferences): Preference.PreferenceGroup {
        val now = remember { Date().time }
        return Preference.PreferenceGroup(
            title = stringResource(R.string.pref_category_timestamps),
            preferenceItems = listOf(
                Preference.PreferenceItem.ListPreference(
                    pref = uiPreferences.relativeTime(),
                    title = stringResource(R.string.pref_relative_format),
                    entries = mapOf(
                        0 to stringResource(R.string.off),
                        2 to stringResource(R.string.pref_relative_time_short),
                        7 to stringResource(R.string.pref_relative_time_long),
                    ),
                ),
                Preference.PreferenceItem.ListPreference(
                    pref = uiPreferences.dateFormat(),
                    title = stringResource(R.string.pref_date_format),
                    entries = DateFormats
                        .associateWith {
                            val formattedDate = UiPreferences.dateFormat(it).format(now)
                            "${it.ifEmpty { stringResource(R.string.label_default) }} ($formattedDate)"
                        },
                ),
            ),
        )
    }

    private fun getLangs(context: Context): Map<String, String> {
        val langs = mutableListOf<Pair<String, String>>()
        val parser = context.resources.getXml(R.xml.locales_config)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "locale") {
                for (i in 0 until parser.attributeCount) {
                    if (parser.getAttributeName(i) == "name") {
                        val langTag = parser.getAttributeValue(i)
                        val displayName = LocaleHelper.getDisplayName(langTag)
                        if (displayName.isNotEmpty()) {
                            langs.add(Pair(langTag, displayName))
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        langs.sortBy { it.second }
        langs.add(0, Pair("", context.getString(R.string.label_default)))

        return langs.toMap()
    }
}

private val DateFormats = listOf(
    "", // Default
    "MM/dd/yy",
    "dd/MM/yy",
    "yyyy-MM-dd",
    "dd MMM yyyy",
    "MMM dd, yyyy",
)

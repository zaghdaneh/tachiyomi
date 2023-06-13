package eu.kanade.tachiyomi.ui.browse.extension.details

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.preference.DialogPreference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.forEach
import androidx.preference.getOnBindEditTextListener
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.preference.SharedPreferencesDataStore
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.getPreferenceKey
import eu.kanade.tachiyomi.widget.TachiyomiTextInputEditText.Companion.setIncognito
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.presentation.core.components.material.Scaffold
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class SourcePreferencesScreen(val sourceId: Long) : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = Injekt.get<SourceManager>().getOrStub(sourceId).toString()) },
                    navigationIcon = {
                        IconButton(onClick = navigator::pop) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.abc_action_bar_up_description),
                            )
                        }
                    },
                    scrollBehavior = it,
                )
            },
        ) { contentPadding ->
            FragmentContainer(
                fragmentManager = (context as FragmentActivity).supportFragmentManager,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                val fragment = SourcePreferencesFragment.getInstance(sourceId)
                add(it, fragment, null)
            }
        }
    }

    /**
     * From https://stackoverflow.com/questions/60520145/fragment-container-in-jetpack-compose/70817794#70817794
     */
    @Composable
    private fun FragmentContainer(
        fragmentManager: FragmentManager,
        modifier: Modifier = Modifier,
        commit: FragmentTransaction.(containerId: Int) -> Unit,
    ) {
        val containerId by rememberSaveable {
            mutableIntStateOf(View.generateViewId())
        }
        var initialized by rememberSaveable { mutableStateOf(false) }
        AndroidView(
            modifier = modifier,
            factory = { context ->
                FragmentContainerView(context)
                    .apply { id = containerId }
            },
            update = { view ->
                if (!initialized) {
                    fragmentManager.commit { commit(view.id) }
                    initialized = true
                } else {
                    fragmentManager.onContainerAvailable(view)
                }
            },
        )
    }

    /** Access to package-private method in FragmentManager through reflection */
    private fun FragmentManager.onContainerAvailable(view: FragmentContainerView) {
        val method = FragmentManager::class.java.getDeclaredMethod(
            "onContainerAvailable",
            FragmentContainerView::class.java,
        )
        method.isAccessible = true
        method.invoke(this, view)
    }
}

class SourcePreferencesFragment : PreferenceFragmentCompat() {

    override fun getContext(): Context? {
        val superCtx = super.getContext() ?: return null
        val tv = TypedValue()
        superCtx.theme.resolveAttribute(R.attr.preferenceTheme, tv, true)
        return ContextThemeWrapper(superCtx, tv.resourceId)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceScreen = populateScreen()
    }

    private fun populateScreen(): PreferenceScreen {
        val sourceId = requireArguments().getLong(SOURCE_ID)
        val source = Injekt.get<SourceManager>().get(sourceId)!!

        check(source is ConfigurableSource)

        val sharedPreferences = requireContext().getSharedPreferences(source.getPreferenceKey(), Context.MODE_PRIVATE)
        val dataStore = SharedPreferencesDataStore(sharedPreferences)
        preferenceManager.preferenceDataStore = dataStore

        val sourceScreen = preferenceManager.createPreferenceScreen(requireContext())
        source.setupPreferenceScreen(sourceScreen)
        sourceScreen.forEach { pref ->
            pref.isIconSpaceReserved = false
            if (pref is DialogPreference) {
                pref.dialogTitle = pref.title
            }

            // Apply incognito IME for EditTextPreference
            if (pref is EditTextPreference) {
                val setListener = pref.getOnBindEditTextListener()
                pref.setOnBindEditTextListener {
                    setListener?.onBindEditText(it)
                    it.setIncognito(lifecycleScope)
                }
            }
        }

        return sourceScreen
    }

    companion object {
        private const val SOURCE_ID = "source_id"

        fun getInstance(sourceId: Long): SourcePreferencesFragment {
            val fragment = SourcePreferencesFragment()
            fragment.arguments = bundleOf(SOURCE_ID to sourceId)
            return fragment
        }
    }
}

package eu.kanade.presentation.browse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import eu.kanade.presentation.browse.components.BaseBrowseItem
import eu.kanade.presentation.browse.components.BaseSourceItem
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.source.SourcesFilterState
import eu.kanade.tachiyomi.util.system.LocaleHelper
import tachiyomi.domain.source.model.Source
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.screens.EmptyScreen
import java.util.SortedMap

@Composable
fun SourcesFilterScreen(
    navigateUp: () -> Unit,
    state: SourcesFilterState.Success,
    onClickLanguage: (String) -> Unit,
    onClickSource: (Source) -> Unit,
    onClickEnablePinSources: (SortedMap<String, List<Source>>) -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = stringResource(R.string.label_sources),
                navigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        if (state.isEmpty) {
            EmptyScreen(
                textResource = R.string.source_filter_empty_screen,
                modifier = Modifier.padding(contentPadding),
            )
            return@Scaffold
        }
        SourcesFilterContent(
            contentPadding = contentPadding,
            state = state,
            onClickLanguage = onClickLanguage,
            onClickSource = onClickSource,
            onClickEnablePinSources = onClickEnablePinSources,
        )
    }
}

@Composable
private fun SourcesFilterContent(
    contentPadding: PaddingValues,
    state: SourcesFilterState.Success,
    onClickLanguage: (String) -> Unit,
    onClickSource: (Source) -> Unit,
    onClickEnablePinSources: (SortedMap<String, List<Source>>) -> Unit,
) {
    FastScrollLazyColumn(
        contentPadding = contentPadding,
    ) {
        item {
            EnablePinItemsButton(
                onClick = { onClickEnablePinSources(state.items) },
            )
        }
        state.items.forEach { (language, sources) ->
            val enabled = language in state.enabledLanguages
            item(
                key = language,
                contentType = "source-filter-header",
            ) {
                SourcesFilterHeader(
                    modifier = Modifier.animateItemPlacement(),
                    language = language,
                    enabled = enabled,
                    onClickItem = onClickLanguage,
                )
            }
            if (enabled) {
                items(
                    items = sources,
                    key = { "source-filter-${it.key()}" },
                    contentType = { "source-filter-item" },
                ) { source ->
                    SourcesFilterItem(
                        modifier = Modifier.animateItemPlacement(),
                        source = source,
                        enabled = "${source.id}" !in state.disabledSources,
                        onClickItem = onClickSource,
                    )
                }
            }
        }
    }
}

@Composable
private fun SourcesFilterHeader(
    modifier: Modifier,
    language: String,
    enabled: Boolean,
    onClickItem: (String) -> Unit,
) {
    SwitchPreferenceWidget(
        modifier = modifier,
        title = LocaleHelper.getSourceDisplayName(language, LocalContext.current),
        checked = enabled,
        onCheckedChanged = { onClickItem(language) },
    )
}

@Composable
private fun SourcesFilterItem(
    modifier: Modifier,
    source: Source,
    enabled: Boolean,
    onClickItem: (Source) -> Unit,
) {
    BaseSourceItem(
        modifier = modifier,
        source = source,
        showLanguageInContent = false,
        onClickItem = { onClickItem(source) },
        action = {
            Checkbox(checked = enabled, onCheckedChange = null)
        },
    )
}

@Composable
private fun EnablePinItemsButton(
    onClick: () -> Unit,
) {
    BaseBrowseItem(
        onClickItem = onClick,
        content = { EnablePinItemsContent() },
        action = {},
    )
}

@Composable
private fun RowScope.EnablePinItemsContent() {
    Column(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.padding.medium)
            .weight(1f),
    ) {
        Text(
            text = stringResource(R.string.action_enable_pinned_sources),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

package eu.kanade.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.util.isTabletUi
import eu.kanade.tachiyomi.R

@Composable
fun ChapterNavigator(
    isRtl: Boolean,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onSliderValueChange: (Int) -> Unit,
) {
    val isTabletUi = isTabletUi()
    val horizontalPadding = if (isTabletUi) 24.dp else 16.dp
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val haptic = LocalHapticFeedback.current

    // We explicitly handle direction based on the reader viewer rather than the system direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Match with toolbar background color set in ReaderActivity
            val backgroundColor = MaterialTheme.colorScheme
                .surfaceColorAtElevation(3.dp)
                .copy(alpha = if (isSystemInDarkTheme()) 0.9f else 0.95f)

            val isLeftEnabled = if (isRtl) enabledNext else enabledPrevious
            if (isLeftEnabled) {
                FilledIconButton(
                    onClick = if (isRtl) onNextChapter else onPreviousChapter,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = backgroundColor,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = stringResource(if (isRtl) R.string.action_next_chapter else R.string.action_previous_chapter),
                    )
                }
            }

            if (totalPages > 1) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(backgroundColor)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = currentPage.toString())

                        Slider(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            value = currentPage.toFloat(),
                            valueRange = 1f..totalPages.toFloat(),
                            steps = totalPages,
                            onValueChange = {
                                onSliderValueChange(it.toInt() - 1)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                        )

                        Text(text = totalPages.toString())
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            val isRightEnabled = if (isRtl) enabledPrevious else enabledNext
            if (isRightEnabled) {
                FilledIconButton(
                    onClick = if (isRtl) onPreviousChapter else onNextChapter,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = backgroundColor,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = stringResource(if (isRtl) R.string.action_previous_chapter else R.string.action_next_chapter),
                    )
                }
            }
        }
    }
}

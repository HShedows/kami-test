package eu.kanade.presentation.library.components

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

/**
 * Renders [items] as fixed, non-scrolling pages instead of one continuously
 * scrolling grid/list. Swiping moves between pages with no slide/fade
 * settle animation (an instant cut), similar to an e-reader page turn.
 *
 * This intentionally does not support the "jump to global search" item
 * shown by the regular scrolling grid when a search query is active —
 * that item doesn't fit the fixed-page-size model. It simply isn't shown
 * while paged browsing is enabled.
 *
 * @param columns number of columns per page. Use 1 for a list layout.
 * @param cellHeightForWidth given the width a single cell will actually be
 *   rendered at (after dividing available width across [columns] and
 *   subtracting grid spacing), return that cell's real rendered height.
 *   This must reflect the same cover aspect ratio / title area used by
 *   [cell], otherwise rows will be mis-measured and either get cut off
 *   (estimate too short) or leave a gap (estimate too tall).
 */
@Composable
internal fun <T> PagedLibraryGrid(
    items: List<T>,
    columns: Int,
    contentPadding: PaddingValues,
    cellHeightForWidth: (cellWidth: Dp) -> Dp,
    cell: @Composable (T) -> Unit,
) {
    val safeColumns = columns.coerceAtLeast(1)
    val layoutDirection = LocalLayoutDirection.current
    val horizontalSpacing = CommonMangaItemDefaults.GridHorizontalSpacer
    val verticalSpacing = CommonMangaItemDefaults.GridVerticalSpacer

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidth = maxWidth -
            contentPadding.calculateStartPadding(layoutDirection) -
            contentPadding.calculateEndPadding(layoutDirection)
        val availableHeight = maxHeight -
            contentPadding.calculateTopPadding() -
            contentPadding.calculateBottomPadding()

        val cellWidth = (availableWidth - horizontalSpacing * (safeColumns - 1)) / safeColumns
        val rowHeight = cellHeightForWidth(cellWidth)

        // How many full rows fit, accounting for the gap between rows too:
        // n rows take n*rowHeight + (n-1)*spacing of vertical space.
        val rowsPerPage = ((availableHeight + verticalSpacing) / (rowHeight + verticalSpacing))
            .toInt()
            .coerceAtLeast(1)
        val pageSize = (safeColumns * rowsPerPage).coerceAtLeast(1)
        val pages = remember(items, pageSize) { items.chunked(pageSize) }
        val pageCount = pages.size.coerceAtLeast(1)

        val pagerState = rememberPagerState(pageCount = { pageCount })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // Anchor each page's content to the top instead of the pager's
            // default vertical centering — otherwise a last page with fewer
            // items than a full page looks vertically centered.
            verticalAlignment = Alignment.Top,
            // No settle animation: page changes are an instant cut, not a slide.
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = snap(),
            ),
        ) { page ->
            val pageItems = pages.getOrElse(page) { emptyList() }
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(safeColumns),
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                ),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(verticalSpacing),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(horizontalSpacing),
                userScrollEnabled = false,
            ) {
                items(pageItems) { item -> cell(item) }
            }
        }
    }
}

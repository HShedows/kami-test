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
 * @param rowHeight approximate rendered height of a single cell (cover +
 *   label, or list row), used to work out how many rows fit on one page.
 *   Doesn't need to be pixel-perfect — it only affects how many items are
 *   grouped onto each page.
 */
@Composable
internal fun <T> PagedLibraryGrid(
    items: List<T>,
    columns: Int,
    rowHeight: Dp,
    contentPadding: PaddingValues,
    cell: @Composable (T) -> Unit,
) {
    val safeColumns = columns.coerceAtLeast(1)
    val layoutDirection = LocalLayoutDirection.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableHeight = maxHeight -
            contentPadding.calculateTopPadding() -
            contentPadding.calculateBottomPadding()
        val rowsPerPage = (availableHeight / rowHeight).toInt().coerceAtLeast(1)
        val pageSize = (safeColumns * rowsPerPage).coerceAtLeast(1)
        val pages = remember(items, pageSize) { items.chunked(pageSize) }
        val pageCount = pages.size.coerceAtLeast(1)

        val pagerState = rememberPagerState(pageCount = { pageCount })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // No settle animation: page changes are an instant cut, not a slide.
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = snap(),
            ),
        ) { page ->
            val pageItems = pages.getOrElse(page) { emptyList() }
            LazyVerticalGrid(
                columns = GridCells.Fixed(safeColumns),
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                ),
                userScrollEnabled = false,
            ) {
                items(pageItems) { item -> cell(item) }
            }
        }
    }
}

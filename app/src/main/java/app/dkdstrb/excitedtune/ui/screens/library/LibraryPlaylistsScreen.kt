package app.dkdstrb.excitedtune.ui.screens.library

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Input
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import app.dkdstrb.excitedtune.LocalMenuState
import app.dkdstrb.excitedtune.LocalPlayerAwareWindowInsets
import app.dkdstrb.excitedtune.MainActivity
import app.dkdstrb.excitedtune.R
import app.dkdstrb.excitedtune.constants.CONTENT_TYPE_HEADER
import app.dkdstrb.excitedtune.constants.CONTENT_TYPE_PLAYLIST
import app.dkdstrb.excitedtune.constants.GridThumbnailHeight
import app.dkdstrb.excitedtune.constants.LibraryViewType
import app.dkdstrb.excitedtune.constants.LibraryViewTypeKey
import app.dkdstrb.excitedtune.constants.LocalLibraryEnableKey
import app.dkdstrb.excitedtune.constants.PlaylistFilter
import app.dkdstrb.excitedtune.constants.PlaylistFilterKey
import app.dkdstrb.excitedtune.constants.PlaylistSortDescendingKey
import app.dkdstrb.excitedtune.constants.PlaylistSortType
import app.dkdstrb.excitedtune.constants.PlaylistSortTypeKey
import app.dkdstrb.excitedtune.constants.PlaylistViewTypeKey
import app.dkdstrb.excitedtune.constants.ShowLikedAndDownloadedPlaylist
import app.dkdstrb.excitedtune.db.entities.PlaylistEntity
import app.dkdstrb.excitedtune.ui.component.ChipsRow
import app.dkdstrb.excitedtune.ui.component.EmptyPlaceholder
import app.dkdstrb.excitedtune.ui.component.LazyColumnScrollbar
import app.dkdstrb.excitedtune.ui.component.LazyVerticalGridScrollbar
import app.dkdstrb.excitedtune.ui.component.LibraryPlaylistGridItem
import app.dkdstrb.excitedtune.ui.component.LibraryPlaylistListItem
import app.dkdstrb.excitedtune.ui.component.ScrollToTopManager
import app.dkdstrb.excitedtune.ui.component.SortHeader
import app.dkdstrb.excitedtune.ui.component.items.AutoPlaylistGridItem
import app.dkdstrb.excitedtune.ui.component.items.AutoPlaylistListItem
import app.dkdstrb.excitedtune.ui.dialog.CreatePlaylistDialog
import app.dkdstrb.excitedtune.ui.dialog.ImportM3uDialog
import app.dkdstrb.excitedtune.ui.menu.ActionDropdown
import app.dkdstrb.excitedtune.ui.menu.DropdownItem
import app.dkdstrb.excitedtune.ui.utils.MEDIA_PERMISSION_LEVEL
import app.dkdstrb.excitedtune.utils.rememberEnumPreference
import app.dkdstrb.excitedtune.utils.rememberPreference
import app.dkdstrb.excitedtune.viewmodels.LibraryPlaylistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryPlaylistsScreen(
    navController: NavController,
    viewModel: LibraryPlaylistsViewModel = hiltViewModel(),
    libraryFilterContent: @Composable() (() -> Unit)? = null,
) {
    Log.v("LibraryPlaylistsScreen", "LP_RC-1")
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val coroutineScope = rememberCoroutineScope()

    var filter by rememberEnumPreference(PlaylistFilterKey, PlaylistFilter.LIBRARY)
    libraryFilterContent?.let { filter = PlaylistFilter.LIBRARY }
    val localLibEnable by rememberPreference(LocalLibraryEnableKey, defaultValue = true)

    var playlistViewType by rememberEnumPreference(PlaylistViewTypeKey, LibraryViewType.GRID)
    val libraryViewType by rememberEnumPreference(LibraryViewTypeKey, LibraryViewType.GRID)
    val viewType = if (libraryFilterContent != null) libraryViewType else playlistViewType

    val (sortType, onSortTypeChange) = rememberEnumPreference(PlaylistSortTypeKey, PlaylistSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(PlaylistSortDescendingKey, true)
    val (showLikedAndDownloadedPlaylist) = rememberPreference(ShowLikedAndDownloadedPlaylist, true)

    val playlists by viewModel.allPlaylists.collectAsState()
    val isSyncingRemotePlaylists by viewModel.isSyncingRemotePlaylists.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    val likedPlaylist = PlaylistEntity(id = "liked", name = stringResource(id = R.string.liked_songs))
    val downloadedPlaylist = PlaylistEntity(id = "downloaded", name = stringResource(id = R.string.downloaded_songs))

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

    var showImportM3uDialog by rememberSaveable { mutableStateOf(false) }
    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.syncPlaylists() }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false }
        )
    }

    val filterContent = @Composable {
        var showStoragePerm by remember {
            mutableStateOf(context.checkSelfPermission(MEDIA_PERMISSION_LEVEL) != PackageManager.PERMISSION_GRANTED)
        }
        Column {
            if (localLibEnable && showStoragePerm) {
                TextButton(
                    onClick = {
                        showStoragePerm =
                            false // allow user to hide error when clicked. This also makes the code a lot nicer too...
                        (context as MainActivity).permissionLauncher.launch(MEDIA_PERMISSION_LEVEL)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(R.string.missing_media_permission_warning),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Row {
                ChipsRow(
                    chips = listOf(
                        PlaylistFilter.LIBRARY to stringResource(R.string.filter_library),
                        PlaylistFilter.DOWNLOADED to stringResource(R.string.filter_downloaded)
                    ),
                    currentValue = filter,
                    onValueUpdate = {
                        filter = it
                        if (it == PlaylistFilter.LIBRARY) viewModel.syncPlaylists()
                    },
                    isLoading = { filter ->
                        filter == PlaylistFilter.LIBRARY && isSyncingRemotePlaylists
                    }
                )
            }
        }
    }

    // TODO: full migration to flow row?
    val headerContent = @Composable {
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 16.dp)
        ) {
            SortHeader(
                sortType = sortType,
                sortDescending = sortDescending,
                onSortTypeChange = onSortTypeChange,
                onSortDescendingChange = onSortDescendingChange,
                sortTypeText = { sortType ->
                    when (sortType) {
                        PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
                        PlaylistSortType.NAME -> R.string.sort_by_name
                        PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
                    }
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                playlists?.let { playlists ->
                    Text(
                        text = pluralStringResource(R.plurals.n_playlist, playlists.size, playlists.size),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(Modifier.width(4.dp))
                ActionDropdown(
                    actions = listOf(
                        DropdownItem(
                            title = stringResource(R.string.library_filter),
                            leadingIcon = { Icon(Icons.Rounded.FilterAlt, null) },
                            action = {},
                            secondaryDropdown =
                                listOf(
                                    DropdownItem(
                                        title = stringResource(R.string.filter_library),
                                        leadingIcon = null,
                                        action = { filter = PlaylistFilter.LIBRARY }
                                    ),
                                    DropdownItem(
                                        title = stringResource(R.string.filter_downloaded),
                                        leadingIcon = null,
                                        action = { filter = PlaylistFilter.DOWNLOADED }
                                    ),
                                )
                        ),
                        DropdownItem(
                            title = stringResource(R.string.create_playlist),
                            leadingIcon = { Icon(Icons.Rounded.Add, null) },
                            action = { showCreatePlaylistDialog = true }
                        ),
                        DropdownItem(
                            title = stringResource(R.string.import_playlist),
                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Input, null) },
                            action = { showImportM3uDialog = true }
                        ),
                    ),
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                state = pullRefreshState,
                isRefreshing = isSyncingRemotePlaylists,
                onRefresh = {
                    viewModel.syncPlaylists(true)
                }
            ),
    ) {
        ScrollToTopManager(navController, lazyListState)
        when (viewType) {
            LibraryViewType.LIST -> {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                ) {
                    item(
                        key = "filter",
                        contentType = CONTENT_TYPE_HEADER
                    ) {
                        libraryFilterContent?.let { it() } ?: filterContent()
                    }

                    item(
                        key = "header",
                        contentType = CONTENT_TYPE_HEADER
                    ) {
                        headerContent()
                    }

                    if (showLikedAndDownloadedPlaylist) {
                        item(
                            key = likedPlaylist.id,
                            contentType = { CONTENT_TYPE_PLAYLIST }
                        ) {
                            AutoPlaylistListItem(
                                playlist = likedPlaylist,
                                thumbnail = Icons.Rounded.Favorite,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("auto_playlist/${likedPlaylist.id}")
                                    }
                                    .animateItem()
                            )
                        }

                        item(
                            key = downloadedPlaylist.id,
                            contentType = { CONTENT_TYPE_PLAYLIST }
                        ) {
                            AutoPlaylistListItem(
                                playlist = downloadedPlaylist,
                                thumbnail = Icons.Rounded.CloudDownload,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("auto_playlist/${downloadedPlaylist.id}")
                                    }
                                    .animateItem()
                            )
                        }
                    }

                    playlists?.let { playlists ->
                        if (playlists.isEmpty() && !showLikedAndDownloadedPlaylist) {
                            item {
                                EmptyPlaceholder(
                                    icon = Icons.AutoMirrored.Rounded.QueueMusic,
                                    text = stringResource(R.string.library_playlist_empty),
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                        items(
                            items = playlists,
                            key = { it.id },
                            contentType = { CONTENT_TYPE_PLAYLIST }
                        ) { playlist ->
                            LibraryPlaylistListItem(
                                navController = navController,
                                menuState = menuState,
                                coroutineScope = coroutineScope,
                                playlist = playlist,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
                LazyColumnScrollbar(
                    state = lazyListState,
                )
            }

            LibraryViewType.GRID -> {
                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                ) {
                    item(
                        key = "filter",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER
                    ) {
                        libraryFilterContent?.let { it() } ?: filterContent()
                    }

                    item(
                        key = "header",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER
                    ) {
                        headerContent()
                    }

                    if (showLikedAndDownloadedPlaylist) {
                        item(
                            key = likedPlaylist.id,
                            contentType = { CONTENT_TYPE_PLAYLIST }
                        ) {
                            AutoPlaylistGridItem(
                                playlist = likedPlaylist,
                                thumbnail = Icons.Rounded.Favorite,
                                fillMaxWidth = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("auto_playlist/${likedPlaylist.id}")
                                    }
                                    .animateItem()
                            )
                        }

                        item(
                            key = downloadedPlaylist.id,
                            contentType = { CONTENT_TYPE_PLAYLIST }
                        ) {
                            AutoPlaylistGridItem(
                                playlist = downloadedPlaylist,
                                thumbnail = Icons.Rounded.CloudDownload,
                                fillMaxWidth = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("auto_playlist/${downloadedPlaylist.id}")
                                    }
                                    .animateItem()
                            )
                        }
                    }

                    playlists?.let { playlists ->
                        if (playlists.isEmpty() && !showLikedAndDownloadedPlaylist) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyPlaceholder(
                                    icon = R.drawable.queue_music,
                                    text = stringResource(R.string.library_playlist_empty),
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                        items(
                            items = playlists,
                            key = { it.id },
                            contentType = { CONTENT_TYPE_PLAYLIST }
                        ) { playlist ->
                            LibraryPlaylistGridItem(
                                navController = navController,
                                menuState = menuState,
                                coroutineScope = coroutineScope,
                                playlist = playlist,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
                LazyVerticalGridScrollbar(
                    state = lazyGridState,
                )
            }
        }

        /**
         * Dialog
         */

        if (showImportM3uDialog) {
            ImportM3uDialog(
                navController = navController,
                onDismiss = { showImportM3uDialog = false }
            )
        }

        Indicator(
            isRefreshing = isSyncingRemotePlaylists,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
        )

    }
}

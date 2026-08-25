package app.dkdstrb.excitedtune.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import app.dkdstrb.excitedtune.LocalMenuState
import app.dkdstrb.excitedtune.LocalPlayerAwareWindowInsets
import app.dkdstrb.excitedtune.R
import app.dkdstrb.excitedtune.constants.AccountNameKey
import app.dkdstrb.excitedtune.constants.GridThumbnailHeight
import app.dkdstrb.excitedtune.constants.InnerTubeCookieKey
import app.dkdstrb.excitedtune.constants.TopBarInsets
import app.dkdstrb.excitedtune.ui.component.PreferenceGroupTitle
import app.dkdstrb.excitedtune.ui.component.button.IconButton
import app.dkdstrb.excitedtune.ui.component.items.YouTubeGridItem
import app.dkdstrb.excitedtune.ui.component.shimmer.GridItemPlaceHolder
import app.dkdstrb.excitedtune.ui.component.shimmer.ShimmerHost
import app.dkdstrb.excitedtune.ui.menu.YouTubeAlbumMenu
import app.dkdstrb.excitedtune.ui.menu.YouTubeArtistMenu
import app.dkdstrb.excitedtune.ui.menu.YouTubePlaylistMenu
import app.dkdstrb.excitedtune.ui.screens.settings.fragments.AccountFrag
import app.dkdstrb.excitedtune.ui.utils.backToMain
import app.dkdstrb.excitedtune.utils.rememberPreference
import app.dkdstrb.excitedtune.viewmodels.AccountViewModel
import com.metrolist.innertube.utils.parseCookieString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val content = LocalContext.current
    val menuState = LocalMenuState.current

    val coroutineScope = rememberCoroutineScope()

    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val accountName by rememberPreference(AccountNameKey, stringResource(R.string.not_logged_in))

    val playlists by viewModel.playlists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        stickyHeader {
            if (!isLoggedIn) {
                Column {
                    PreferenceGroupTitle(
                        title = stringResource(R.string.account)
                    )
                    AccountFrag(navController)
                }
            }
        }
        items(
            items = playlists.orEmpty(),
            key = { it.id }
        ) { item ->
            YouTubeGridItem(
                item = item,
                fillMaxWidth = true,
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            navController.navigate("online_playlist/${item.id}")
                        },
                        onLongClick = {
                            menuState.show {
                                YouTubePlaylistMenu(
                                    navController = navController,
                                    playlist = item,
                                    coroutineScope = coroutineScope,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
            )
        }

        items(
            items = albums.orEmpty(),
            key = { it.id }
        ) { item ->
            YouTubeGridItem(
                item = item,
                fillMaxWidth = true,
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            navController.navigate("album/${item.id}")
                        },
                        onLongClick = {
                            menuState.show {
                                YouTubeAlbumMenu(
                                    albumItem = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
            )
        }

        items(
            items = artists.orEmpty(),
            key = { it.id }
        ) { item ->
            YouTubeGridItem(
                item = item,
                fillMaxWidth = true,
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            navController.navigate("artist/${item.id}")
                        },
                        onLongClick = {
                            menuState.show {
                                YouTubeArtistMenu(
                                    artist = item,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
            )
        }

        if (isLoggedIn && (playlists == null && isLoading < 3)) {
            items(8) {
                ShimmerHost {
                    GridItemPlaceHolder(fillMaxWidth = true)
                }
            }
        }
    }

    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = accountName,
                    overflow = TextOverflow.Ellipsis
                )
                if (isLoggedIn) {
                    IconButton(
                        onClick = {
                            navController.navigate("settings/account_sync")
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        windowInsets = TopBarInsets,
        scrollBehavior = scrollBehavior
    )
}

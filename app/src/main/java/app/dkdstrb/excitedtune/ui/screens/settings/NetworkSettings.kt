package app.dkdstrb.excitedtune.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.dkdstrb.excitedtune.R
import app.dkdstrb.excitedtune.constants.TopBarInsets
import app.dkdstrb.excitedtune.ui.component.ColumnWithContentPadding
import app.dkdstrb.excitedtune.ui.component.button.IconButton
import app.dkdstrb.excitedtune.ui.screens.settings.fragments.NetworkFrag
import app.dkdstrb.excitedtune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    ColumnWithContentPadding(
        modifier = Modifier.fillMaxHeight(),
        columnModifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        NetworkFrag()
    }

    TopAppBar(
        title = { Text(stringResource(R.string.network_settings_title)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        windowInsets = TopBarInsets,
        scrollBehavior = scrollBehavior
    )
}

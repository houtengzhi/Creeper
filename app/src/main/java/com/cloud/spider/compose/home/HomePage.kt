package com.cloud.spider.compose.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.cloud.spider.R
import com.cloud.spider.server.SpiderService
import com.cloud.spider.util.SystemUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 *
 * Created by cloud on 2024/1/26.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomePage(onConvertClick: () -> Unit = {}) {

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopAppBar(scrollBehavior = scrollBehavior)
        }
    ) { contentPadding ->
        HomePageScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            onConvertClick = onConvertClick)

        var showRationableDialog by remember {
            mutableStateOf(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionState =
                rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
            val permissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {

                    } else {
                        showRationableDialog = true
                    }
                }

            LaunchedEffect(key1 = notificationPermissionState, block = {
                if (!notificationPermissionState.status.isGranted && notificationPermissionState.status.shouldShowRationale) {
                    showRationableDialog = true
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            })
        }

        when {
            showRationableDialog -> {
                RationaleDialog(permission = Manifest.permission.POST_NOTIFICATIONS) {
                    showRationableDialog = false
                }
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                  modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(title = {
                                   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                       Text(text = stringResource(id = R.string.app_name),
                                           style = MaterialTheme.typography.displaySmall)
                                   }
    },
        modifier = modifier,
        actions = {

        },
        scrollBehavior = scrollBehavior)
}

@Composable
private fun HomePageScreen(modifier: Modifier = Modifier, onConvertClick: () -> Unit) {

    Column(modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight()) {

        val context = LocalContext.current
        var serverStarted by remember { mutableStateOf(SystemUtil.isServiceRunning(context, SpiderService::class.java.name)) }

        val intent = Intent(context, SpiderService::class.java)
        CardItem(
            iconResId = if (serverStarted) R.drawable.ic_running else R.drawable.ic_stopped,
            title = if (serverStarted) "Running" else "Stopped",
            subtitle = if (serverStarted) "hhhhhh" else "Click here to start",
            onClick = {
                if (serverStarted) {
                    context.stopService(intent)
                    serverStarted = false
                } else {
                    context.startService(intent)
                    serverStarted = true
                }
                      },
            modifier = Modifier,
            colors = if (serverStarted) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary) else CardDefaults.cardColors()
        )

        AnimatedVisibility(visible = serverStarted) {
            CardItem(
                iconResId = R.drawable.ic_cloud,
                title = "Server Status",
                subtitle = "api request",
                onClick = {

                },
                modifier = Modifier,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }

        CommonItem(
            iconResId = R.drawable.ic_convert,
            title = "Add converter",
            onClick = onConvertClick,
            modifier = Modifier
        )

        CommonItem(
            iconResId = R.drawable.ic_settings,
            title = "Settings",
            onClick = { },
            modifier = Modifier
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardItem(@DrawableRes iconResId: Int, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier, colors: CardColors) {
    ElevatedCard(modifier = modifier
        .padding(start = 24.dp, end = 24.dp, top = 12.dp)
        .fillMaxWidth()
        .wrapContentHeight(),
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(iconResId),
                contentDescription = "stopped icon",
                modifier = Modifier
                    .padding(start = 24.dp, top = 12.dp)
                    .size(24.dp, 24.dp),
                tint = LocalContentColor.current
            )

            Column {
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, top = 12.dp),
                    textAlign = TextAlign.Center)

                Text(text = subtitle,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp))
            }
        }

    }
}

@Composable
fun CommonItem(@DrawableRes iconResId: Int, title: String, onClick: () -> Unit, modifier: Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 24.dp, top = 12.dp, end = 24.dp)
            .clickable(onClick = onClick)) {
        Icon(painter = painterResource(iconResId),
            contentDescription = "stopped icon",
            modifier = Modifier
                .padding(start = 24.dp, top = 12.dp, bottom = 12.dp)
                .size(24.dp, 24.dp),
            tint = LocalContentColor.current
        )

        Text(text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp),
            textAlign = TextAlign.Center)
    }
}

@Composable
fun RationaleDialog(permission: String, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val permissionInfo = context.packageManager.getPermissionInfo(permission, 0)
    val permissionGroupInfo =
        permissionInfo.group?.let { context.packageManager.getPermissionGroupInfo(it, 0) }
    val rationale = permissionGroupInfo?.loadLabel(context.packageManager).toString()
    AlertDialog(
        text = {
               Text(text = stringResource(id = R.string.You_have_denied_to_access_to_your_y, rationale))
        },
        onDismissRequest = {
                           onDismissRequest()
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                    it.setData(Uri.fromParts("package", context.packageName, null))
                    context.startActivity(it)
                }
            }) {
                Text(text = "Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(text = "Dismiss")
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
}

@Preview
@Composable
private fun HomePagePreview() {
    HomePage()
}
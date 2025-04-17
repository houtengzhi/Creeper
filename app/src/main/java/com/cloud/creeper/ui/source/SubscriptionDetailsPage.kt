package com.cloud.creeper.ui.source

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.creeper.R
import com.cloud.creeper.base.DataState
import com.cloud.creeper.protocol.base.ProxyNode
import com.cloud.creeper.repository.entity.SubscriptionDetails
import com.cloud.creeper.repository.entity.SubscriptionSource

private const val TAG = "SubscriptionDetailsPage"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailsPage(viewModel: SubscriptionViewModel, onUpClick: () -> Unit = {}) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val uiState = viewModel.subscriptionDetailsState.collectAsStateWithLifecycle()

    val deleteState = viewModel.deleteState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubscriptionDetailsTopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick, onUpdateClick = {

            })
        }
    ) { contentPadding ->

        SubscriptionDetailsScreen(uiState.value, modifier = Modifier.padding(top = contentPadding.calculateTopPadding()))

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionDetailsTopAppBar(scrollBehavior: TopAppBarScrollBehavior?,
                                        modifier: Modifier = Modifier, onUpClick: () -> Unit, onUpdateClick: () -> Unit) {
    var moreMenuExpanded by remember { mutableStateOf(false) }
    Log.d(TAG, "SubscriptionManageTopAppBar() $currentRecomposeScope")
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.Subscription_Details))
    },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onUpClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = {
                moreMenuExpanded = true
            }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add converter")
            }
            MoreMenu(expanded = moreMenuExpanded, onDismissRequest = {
                moreMenuExpanded = false
            }, onUpdateClick = onUpdateClick)
        },
        scrollBehavior = scrollBehavior)
}

@Composable
private fun MoreMenu(expanded: Boolean, onDismissRequest: () -> Unit, onUpdateClick: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Refresh))
        }, onClick = {
            onDismissRequest()
            onUpdateClick()
        } )
    }
}

@Composable
fun SubscriptionDetailsScreen(dataState: DataState<SubscriptionDetails>, modifier: Modifier = Modifier) {
    Log.d(TAG, "SubscriptionDetailsScreen(), dataState=${dataState}")
    when {
        dataState.isLoading -> {
            LoadingIndicator()
        }
        dataState.throwable != null -> {

        }
        dataState.error != null -> {

        }
        dataState.data != null -> {
            dataState.data.let { details ->

                LazyVerticalGrid(modifier = modifier,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(details.nodeList) {
                        NodeItem(proxyNode = it)
                    }
                }
            }

        }
    }

}

@Composable
private fun NodeItem(proxyNode: ProxyNode) {

    ElevatedCard(modifier = Modifier
        .padding(start = 12.dp, end = 12.dp, top = 12.dp)
        .fillMaxWidth()
        .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {

        Column {
            Text(
                text = proxyNode.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp),
                textAlign = TextAlign.Start
            )

            Text(
                text = proxyNode.type,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp)
            )
        }

    }

}

@Composable
private fun DeleteSubscriptionSourceDialog(subscriptionSource: SubscriptionSource, onDismissRequest: () -> Unit, onDeleteClick: (subscriptionSource: SubscriptionSource) -> Unit) {
    AlertDialog(onDismissRequest = onDismissRequest, modifier = Modifier,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                onDeleteClick(subscriptionSource)

            }) {
                Text(text = stringResource(id = R.string.OK))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.Cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.Delete_subscription_source))
        },
        text = {
            Text(text = stringResource(id = R.string.You_wont_be_able_to_recover_it_once_confirmed))
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@Composable
private fun LoadingIndicator() {
    CircularProgressIndicator()
}
package com.cloud.spider.compose.source

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.spider.R
import com.cloud.spider.base.DataState
import com.cloud.spider.protocol.ClientType
import com.cloud.spider.repository.entity.SubscriptionSource
import com.cloud.spider.util.SystemUtil

/**
 *
 * Created by cloud on 2024/2/5.
 */
private const val TAG = "SubscriptionManagePage"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionManagePage(viewModel: SubscriptionViewModel = hiltViewModel(), onUpClick: () -> Unit = {}, onNewClick: () -> Unit) {
    Log.d(TAG, "SubscriptionManagePage()")
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showAddSubscriptionDialog by remember {
        mutableStateOf(false)
    }
    val uiState = viewModel.subscribeSubscriptionSourceList().collectAsStateWithLifecycle()

    val deleteState = viewModel.deleteState.collectAsStateWithLifecycle()

    val addState = viewModel.addState.observeAsState(DataState.initial())


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubscriptionManageTopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick, onNewClick = {
                showAddSubscriptionDialog = true
            })
        }
    ) { contentPadding ->
        Log.d(TAG, "Scaffold()")

        ConverterPageScreen(uiState.value, addState.value, deleteState.value, modifier = Modifier.padding(top = contentPadding.calculateTopPadding()), onDeleteClick = {
            viewModel.deleteSubscriptionSource(it)
        })

        when {
            showAddSubscriptionDialog -> {
                AddSubscriptionSourceDialog(onDismissRequest = {
                    showAddSubscriptionDialog = false
                }, onSaveClick = { name: String, url: String ->
                    val subscriptionSource = SubscriptionSource(SystemUtil.generateSubscriptionSourceId(), name, url, ClientType.Clash.text)
                    subscriptionSource.createdTime = System.currentTimeMillis()
                    subscriptionSource.updatedTime = System.currentTimeMillis()
                    viewModel.addSubscriptionSource(subscriptionSource)
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionManageTopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                                     modifier: Modifier = Modifier, onUpClick: () -> Unit, onNewClick: () -> Unit) {
    var moreMenuExpanded by remember { mutableStateOf(false) }
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.Subscription_Manage))
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
            }, onNewClick = onNewClick)
        },
        scrollBehavior = scrollBehavior)
}

@Composable
private fun MoreMenu(expanded: Boolean, onDismissRequest: () -> Unit, onNewClick: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(text = {
            Text(text = "Add subscription")
        }, onClick = {
            onDismissRequest()
            onNewClick()
        } )
        DropdownMenuItem(text = {
            Text(text = "About")
        }, onClick = {
            onDismissRequest()
        })
    }
}

@Composable
fun ConverterPageScreen(dataState: DataState<List<SubscriptionSource>>, addState: DataState<Boolean>, deleteState: DataState<Boolean>, modifier: Modifier = Modifier, onDeleteClick: (subscriptionSource: SubscriptionSource) -> Unit) {
    Log.d(TAG, "ConverterPageScreen(), dataState=${dataState}")
    when {
        dataState.isLoading -> {
            LoadingIndicator()
        }
        dataState.throwable != null -> {

        }
        dataState.error != null -> {

        }
        dataState.data != null -> {
            dataState.data.let { dataList ->
                LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dataList) {
                        SubscriptionSourceItem(it, onDeleteClick)
                    }
                }
            }

        }
    }

    when {
        addState.isLoading -> {
            LoadingIndicator()
        }
        addState.throwable != null -> {

        }
        addState.error != null -> {

        }
        else -> {

        }
    }

    when {
        deleteState.isLoading -> {
            LoadingIndicator()
        }
        deleteState.throwable != null -> {

        }
        deleteState.error != null -> {

        }
        else -> {

        }
    }
}

@Composable
private fun SubscriptionSourceItem(subscriptionSource: SubscriptionSource, onDeleteClick: (subscriptionSource: SubscriptionSource) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable {

        }, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = subscriptionSource.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 6.dp))
            Text(text = subscriptionSource.sourceUrl, modifier = Modifier.padding(start = 24.dp, bottom = 12.dp))
        }

        IconButton(onClick = {
            menuExpanded = true
        }, modifier = Modifier.padding(end = 12.dp)) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Menu")

            SubscriptionSourceMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onEditClick = { /*TODO*/ },
                onDeleteClick = { showDeleteDialog = true } )
        }
    }

    if (showDeleteDialog) {
        DeleteSubscriptionSourceDialog(
            subscriptionSource = subscriptionSource,
            onDismissRequest = { showDeleteDialog = false },
            onDeleteClick = {onDeleteClick(subscriptionSource)}
        )
    }

}

@Composable
private fun SubscriptionSourceMenu(expanded: Boolean, onDismissRequest: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = Modifier) {
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Edit))
        }, onClick = {
            onDismissRequest()
            onEditClick()
        } )
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Delete))
        }, onClick = {
            onDismissRequest()
            onDeleteClick()
        })
    }
}

@Composable
private fun AddSubscriptionSourceDialog(onDismissRequest: () -> Unit, onSaveClick: (name: String, url: String) -> Unit) {
    var name by remember {
        mutableStateOf("")
    }
    var url by remember {
        mutableStateOf("")
    }

    var isSaveBtnEnabled by remember {
        mutableStateOf(false)
    }

    AlertDialog(onDismissRequest = onDismissRequest, modifier = Modifier,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                onSaveClick(name, url)

            }, enabled = isSaveBtnEnabled) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(text = "Cancel")
            }
        },
        title = {
            Text(text = "Add subscription")
        },
        text = {
            Column(modifier = Modifier) {
                TextField(value = name,
                    onValueChange = {
                        name = it
                        isSaveBtnEnabled = url.isNotEmpty() && it.isNotEmpty()
                    }, label = { Text("Name")}, singleLine = true, modifier = Modifier)
                TextField(value = url,
                    onValueChange = {
                        url = it
                        isSaveBtnEnabled = name.isNotEmpty() && it.isNotEmpty()
                    }, label = { Text("Subscription Url")}, singleLine = true, modifier = Modifier.padding(top = 12.dp))

            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
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
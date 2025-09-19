package com.cloud.creeper.ui.source

import android.content.ClipData
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.creeper.R
import com.cloud.creeper.base.DataState
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.ui.ErrorDialog
import com.cloud.creeper.util.SUPPORTED_SOURCE_TYPE_LIST
import com.cloud.creeper.util.SystemUtil
import kotlinx.coroutines.launch

/**
 *
 * Created by cloud on 2024/2/5.
 */
private const val TAG = "SubscriptionManagePage"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionManagePage(viewModel: SubscriptionViewModel, onUpClick: () -> Unit = {}, onNewClick: () -> Unit,
                           onResultSet: ((subscriptionSource: SubscriptionSource) -> Unit)? = null, onDetailsClick: (subscriptionSource: SubscriptionSource) -> Unit) {
    Log.d(TAG, "SubscriptionManagePage() $currentRecomposeScope")
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showAddSubscriptionDialog by remember {
        mutableStateOf(false)
    }
    val uiState = viewModel.subscribeSubscriptionListState.collectAsStateWithLifecycle()

    val deleteState = viewModel.deleteState.collectAsStateWithLifecycle()

    val addState = viewModel.addState.observeAsState(DataState.initial())

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubscriptionManageTopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick, onNewClick = {
                showAddSubscriptionDialog = true
            })
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { contentPadding ->
        Log.d(TAG, "Scaffold() $currentRecomposeScope")

        ConverterPageScreen(viewModel, uiState.value, addState.value, deleteState.value, modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            onItemClick = onResultSet ?: onDetailsClick,
            onEditClick = {
                viewModel.editSubscriptionSource(it)
            },
            onDeleteClick = {
                viewModel.deleteSubscriptionSource(it)
        }, onUpdateClick = {
            viewModel.fetchSubscriptionDetails(it, true)
        }, onDetailsClick = onDetailsClick,
            onShowSnackbar = {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                }
            })

        when {
            showAddSubscriptionDialog -> {
                AddSubscriptionSourceDialog(onDismissRequest = {
                    showAddSubscriptionDialog = false
                }, onSaveClick = { source: SubscriptionSource ->
                    viewModel.addSubscriptionSource(source)
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionManageTopAppBar(scrollBehavior: TopAppBarScrollBehavior?,
                                     modifier: Modifier = Modifier, onUpClick: () -> Unit, onNewClick: () -> Unit) {
    var moreMenuExpanded by remember { mutableStateOf(false) }
    Log.d(TAG, "SubscriptionManageTopAppBar() $currentRecomposeScope")
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
    }
}

@Composable
fun ConverterPageScreen(viewModel: SubscriptionViewModel, dataState: DataState<List<SubscriptionSource>>, addState: DataState<Boolean>, deleteState: DataState<Boolean>, modifier: Modifier = Modifier,
                        onItemClick: (subscriptionSource: SubscriptionSource) -> Unit,
                        onEditClick: (subscriptionSource: SubscriptionSource) -> Unit,
                        onDeleteClick: (subscriptionSource: SubscriptionSource) -> Unit,
                        onUpdateClick: (subscriptionSource: SubscriptionSource) -> Unit,
                        onDetailsClick: (subscriptionSource: SubscriptionSource) -> Unit,
                        onShowSnackbar: (message: String) -> Unit) {
    Log.d(TAG, "ConverterPageScreen(), dataState=${dataState} addState=${addState}")
    when {
        dataState.isLoading -> {
        }
        dataState.throwable != null -> {

        }
        dataState.error != null -> {

        }
        dataState.data != null -> {
            dataState.data.let { dataList ->
                LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dataList) {
                        SubscriptionSourceItem(it, onItemClick, onEditClick, onDeleteClick, onUpdateClick, onShowSnackbar)
                    }
                }
            }

        }
    }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    when {
        addState.isLoading -> {
            com.cloud.creeper.ui.LoadingIndicator(modifier = Modifier.width(64.dp))
        }
        addState.throwable != null -> {
            Log.d(TAG, "add failed for exception: ${addState.throwable.message}")
            showErrorDialog = true
            errorMessage = addState.throwable.message ?: "Exception"
            viewModel.resetAddState()

        }
        addState.error != null -> {
            Log.d(TAG, "add failed for ${addState.error}")
            showErrorDialog = true
            errorMessage = addState.error!!.toString()
            viewModel.resetAddState()
        }
        else -> {

        }
    }

    when {
        deleteState.isLoading -> {
            com.cloud.creeper.ui.LoadingIndicator(modifier = Modifier.width(64.dp))
        }
        deleteState.throwable != null -> {

        }
        deleteState.error != null -> {

        }
        else -> {

        }
    }

    if (showErrorDialog) {
        ErrorDialog(message = errorMessage, onDismissRequest = { showErrorDialog = false })
    }
}

@Composable
private fun SubscriptionSourceItem(subscriptionSource: SubscriptionSource, onItemClick: (subscriptionSource: SubscriptionSource) -> Unit,
                                   onEditClick: (subscriptionSource: SubscriptionSource) -> Unit,
                                   onDeleteClick: (subscriptionSource: SubscriptionSource) -> Unit,
                                   onUpdateClick: (subscriptionSource: SubscriptionSource) -> Unit,
                                   onShowSnackbar: (message: String) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable {
            onItemClick(subscriptionSource)
        }, verticalAlignment = Alignment.CenterVertically) {

        Image(painter = painterResource(id = subscriptionSource.getClientIconResId()), contentDescription = "",
            modifier = Modifier.padding(start = 24.dp), colorFilter = ColorFilter.tint(subscriptionSource.getClientIconColor()))

        Column(modifier = Modifier.weight(1f)) {

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = subscriptionSource.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 24.dp), maxLines = 1)
                Text(text = subscriptionSource.getUpdatedTimeText(context),
                    modifier = Modifier.padding(end = 12.dp),
                    style = MaterialTheme.typography.labelMedium)
            }
            Text(text = if (subscriptionSource.description.isNullOrEmpty()) subscriptionSource.sourceUrl else subscriptionSource.description!!, modifier = Modifier.padding(start = 24.dp, bottom = 12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        IconButton(onClick = {
            menuExpanded = true
        }, modifier = Modifier.padding(end = 12.dp)) {
            val message = stringResource(R.string.Copied_successfully)

            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Menu")

            SubscriptionSourceMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onEditClick = { showEditDialog = true },
                onDeleteClick = { showDeleteDialog = true },
                onUpdateClick = { onUpdateClick(subscriptionSource)},
                onCopyClick = {
                    val clipData = ClipData.newPlainText("plain text", subscriptionSource.sourceUrl)
                    clipboardManager.nativeClipboard.setPrimaryClip(clipData)
                    onShowSnackbar(message)
                })
        }
    }

    if (showDeleteDialog) {
        DeleteSubscriptionSourceDialog(
            subscriptionSource = subscriptionSource,
            onDismissRequest = { showDeleteDialog = false },
            onDeleteClick = {
                onDeleteClick(it)
            }
        )
    }

    if (showEditDialog) {
        EditSubscriptionSourceDialog(
            subscriptionSource = subscriptionSource,
            onDismissRequest = { showEditDialog = false },
            onSaveClick = {
                onEditClick(it)
            }
        )
    }

}

@Composable
private fun SubscriptionSourceMenu(expanded: Boolean, onDismissRequest: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit, onUpdateClick: () -> Unit, onCopyClick: () -> Unit) {
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
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Update))
        }, onClick = {
            onDismissRequest()
            onUpdateClick()
        })
        DropdownMenuItem(text = {
            Text(text = stringResource(id = R.string.Copy_Url))
        }, onClick = {
            onDismissRequest()
            onCopyClick()
        } )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSubscriptionSourceDialog(onDismissRequest: () -> Unit, onSaveClick: (source: SubscriptionSource) -> Unit) {
    var name by remember {
        mutableStateOf("")
    }
    var description by remember {
        mutableStateOf("")
    }
    var url by remember {
        mutableStateOf("")
    }
    var sourceType by remember {
        mutableStateOf(ClientType.Clash)
    }

    var isSaveBtnEnabled by remember {
        mutableStateOf(false)
    }

    AlertDialog(onDismissRequest = onDismissRequest, modifier = Modifier,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                val subscriptionSource = SubscriptionSource(SystemUtil.generateSubscriptionSourceId(), name, url, sourceType).apply {
                    this.description = description
                    createdTime = System.currentTimeMillis()
                    updatedTime = System.currentTimeMillis()
                }
                onSaveClick(subscriptionSource)

            }, enabled = isSaveBtnEnabled) {
                Text(text = stringResource(id = R.string.Save))
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
            Text(text = stringResource(id = R.string.Add_subscription))
        },
        text = {
            Column(modifier = Modifier) {
                var clientMenuExpanded by remember { mutableStateOf(false) }

                TextField(value = name,
                    onValueChange = {
                        name = it
                        isSaveBtnEnabled = url.isNotEmpty() && it.isNotEmpty()
                    }, label = { Text("Name")}, singleLine = true, modifier = Modifier)

                TextField(value = description,
                    onValueChange = {
                        description = it
                        isSaveBtnEnabled = url.isNotEmpty() && it.isNotEmpty()
                    }, label = { Text(stringResource(id = R.string.Description))}, singleLine = true, modifier = Modifier.padding(top = 12.dp))

                TextField(value = url,
                    onValueChange = {
                        url = it
                        isSaveBtnEnabled = name.isNotEmpty() && it.isNotEmpty()
                    }, label = { Text("Subscription Url")}, singleLine = true, modifier = Modifier.padding(top = 12.dp))

                ExposedDropdownMenuBox(modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(), expanded = clientMenuExpanded, onExpandedChange = {
                    clientMenuExpanded = it
                }) {
                    TextField(value = sourceType.name, onValueChange = {

                    },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded)
                        },
                        placeholder = {Text("Please select client")},
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth())
                    ExposedDropdownMenu(expanded = clientMenuExpanded, onDismissRequest = { clientMenuExpanded = false }) {
                        SUPPORTED_SOURCE_TYPE_LIST.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                sourceType = it
                                clientMenuExpanded = false
                            })
                        }
                    }
                }
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSubscriptionSourceDialog(subscriptionSource: SubscriptionSource, onDismissRequest: () -> Unit, onSaveClick: (subscriptionSource: SubscriptionSource) -> Unit) {
    var name by remember {
        mutableStateOf(subscriptionSource.name)
    }
    var url by remember {
        mutableStateOf(subscriptionSource.sourceUrl)
    }
    var sourceType by remember {
        mutableStateOf(subscriptionSource.type)
    }

    var dataChanged by remember {
        mutableStateOf(false)
    }

    var isSaveBtnEnabled by remember {
        mutableStateOf(false)
    }

    AlertDialog(onDismissRequest = onDismissRequest, modifier = Modifier,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                val subscription = SubscriptionSource(subscriptionSource.id, name, url, sourceType)
                subscription.createdTime = subscription.createdTime
                subscription.updatedTime = System.currentTimeMillis()
                onSaveClick(subscriptionSource)

            }, enabled = isSaveBtnEnabled) {
                Text(text = stringResource(id = R.string.Save))
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
            Text(text = stringResource(id = R.string.Edit_subscription))
        },
        text = {
            Column(modifier = Modifier) {
                var clientMenuExpanded by remember { mutableStateOf(false) }

                TextField(value = name,
                    onValueChange = {
                        name = it
                        dataChanged = dataChanged || subscriptionSource.name != name
                        isSaveBtnEnabled = url.isNotEmpty() && it.isNotEmpty() && dataChanged
                    }, label = { Text("Name")}, singleLine = true, modifier = Modifier)
                TextField(value = url,
                    onValueChange = {
                        url = it
                        dataChanged = dataChanged || subscriptionSource.sourceUrl != url
                        isSaveBtnEnabled = name.isNotEmpty() && it.isNotEmpty() && dataChanged
                    }, label = { Text("Subscription Url")}, singleLine = true, modifier = Modifier.padding(top = 12.dp))

                ExposedDropdownMenuBox(modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(), expanded = clientMenuExpanded, onExpandedChange = {
                    clientMenuExpanded = it
                }) {
                    TextField(value = sourceType.name, onValueChange = {
                        dataChanged = dataChanged || subscriptionSource.type != sourceType
                        isSaveBtnEnabled = name.isNotEmpty() && url.isNotEmpty() && dataChanged
                                                                       },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded)
                        },
                        placeholder = {Text("Please select client")},
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth())
                    ExposedDropdownMenu(expanded = clientMenuExpanded, onDismissRequest = { clientMenuExpanded = false }) {
                        SUPPORTED_SOURCE_TYPE_LIST.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                sourceType = it
                                clientMenuExpanded = false
                            })
                        }
                    }
                }
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
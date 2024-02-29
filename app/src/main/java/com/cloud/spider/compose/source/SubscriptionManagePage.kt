package com.cloud.spider.compose.source

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.spider.R
import com.cloud.spider.compose.SubscriptionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 *
 * Created by cloud on 2024/2/5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionManagePage(viewModel: SubscriptionViewModel = hiltViewModel(), onUpClick: () -> Unit = {}, onNewClick: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showAddSubscriptionDialog by remember {
        mutableStateOf(false)
    }

    val state = viewModel.subscriptionListState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubscriptionManageTopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick, onNewClick = {
                showAddSubscriptionDialog = true
            })
        }
    ) { contentPadding ->
        ConverterPageScreen(viewModel, modifier = Modifier.padding(top = contentPadding.calculateTopPadding()))

        when {
            showAddSubscriptionDialog -> {
                AddSubscriptionUrlDialog(onDismissRequest = {
                    showAddSubscriptionDialog = false
                }, onSaveClick = { name: String, url: String ->

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
        Text(text = stringResource(id = R.string.Subscription_Source_Manage))
    },
        modifier = modifier,
        navigationIcon = {
             IconButton(onClick = onUpClick) {
                 Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
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
fun ConverterPageScreen(viewModel: SubscriptionViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.subscriptionListState.collectAsStateWithLifecycle()
    when {
        state.value.isLoading -> {

        }
        state.value.throwable != null -> {

        }
        state.value.data != null ->{

        }
    }
}

@Composable
private fun AddSubscriptionUrlDialog(onDismissRequest: () -> Unit, onSaveClick: (name: String, url: String) -> Unit) {
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
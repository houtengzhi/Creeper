package com.cloud.spider.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cloud.spider.R
import com.cloud.spider.protocol.ClientType

/**
 *
 * Created by cloud on 2024/2/5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterPage(onUpClick: () -> Unit = {}) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ConverterTopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick, onMergeItemClick = { showBottomSheet = true })
        }
    ) { contentPadding ->
        ConverterPageScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()))

        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        if (showBottomSheet) {
            MergeProxiesScreen {
                showBottomSheet = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterTopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                          modifier: Modifier = Modifier, onUpClick: () -> Unit, onMergeItemClick: () -> Unit) {
    var moreMenuExpanded by remember { mutableStateOf(false) }
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.Convert))
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
            }, onMergeItemClick = onMergeItemClick)
        },
        scrollBehavior = scrollBehavior)
}

@Composable
private fun MoreMenu(expanded: Boolean, onDismissRequest: () -> Unit, onMergeItemClick: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(text = {
            Text(text = "Merge Proxies")
        }, onClick = {
            onDismissRequest()
            onMergeItemClick()
        } )
        DropdownMenuItem(text = {
            Text(text = "About")
        }, onClick = {
            onDismissRequest()
        })
    }
}

@Composable
fun ConverterPageScreen(modifier: Modifier = Modifier) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeProxiesScreen(onDismissRequest: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        var name by remember {
            mutableStateOf("")
        }
        var url by remember {
            mutableStateOf("")
        }
        var clientType by remember { mutableStateOf(ClientType.Clash) }

        var clientMenuExpanded by remember { mutableStateOf(false) }

        val supportedClientList = mutableListOf<ClientType>()
        supportedClientList.add(ClientType.Clash)
        supportedClientList.add(ClientType.V2Ray)

        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()) {

            TextField(value = name, onValueChange = {name = it}, label = { Text("Name")}, singleLine = true, modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth())

            TextField(value = url, onValueChange = {url = it}, label = { Text("Subscription Url")}, modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 24.dp)
                .fillMaxWidth())

            ExposedDropdownMenuBox(modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 24.dp)
                .fillMaxWidth(), expanded = clientMenuExpanded, onExpandedChange = {
                clientMenuExpanded = it
            }) {
                TextField(value = clientType.text, onValueChange = {url = it},
                    label = { Text("Client")},
                    readOnly = true,
                    trailingIcon = {
                                   ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded)
                    },
                    placeholder = {Text("Please select client")},
                    modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = clientMenuExpanded, onDismissRequest = { clientMenuExpanded = false }) {
                    supportedClientList.forEach {
                        DropdownMenuItem(text = { Text(text = it.text) }, onClick = {
                            clientType = it
                            clientMenuExpanded = false
                        })
                    }
                }
            }
            

        }

    }
}
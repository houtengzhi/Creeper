package com.cloud.spider.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.cloud.spider.R
import com.cloud.spider.protocol.ClientType

/**
 *
 * Created by cloud on 2024/2/22.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConverterPage(onUpClick: () -> Unit = {}, viewModel: ConvertViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NewConverterTopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick, onSaveClick = {

            })
        }
    ) { contentPadding ->

        MergeProxiesScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()), viewModel)

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewConverterTopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                               modifier: Modifier = Modifier, onUpClick: () -> Unit, onSaveClick: () -> Unit) {

    TopAppBar(title = {
        Text(text = stringResource(id = R.string.New_Converter))
    },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onUpClick) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onSaveClick) {
                Icon(imageVector = Icons.Filled.Done, contentDescription = "Save converter")
            }
        },
        scrollBehavior = scrollBehavior)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeProxiesScreen(modifier: Modifier = Modifier, viewModel: ConvertViewModel) {

        var url by remember {
            mutableStateOf("")
        }

        var clientMenuExpanded by remember { mutableStateOf(false) }

        val supportedClientList = mutableListOf<ClientType>()
        supportedClientList.add(ClientType.Clash)
        supportedClientList.add(ClientType.V2Ray)

        Column(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()) {

            Text(text = "Name", modifier = Modifier
                .padding(start = 12.dp))
            TextField(value = viewModel.converterName, onValueChange = { viewModel.updateConverterName(it) }, label = { Text("Name")}, singleLine = true, modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth())

            TextField(value = url, onValueChange = {url = it}, label = { Text("Subscription Url")}, singleLine = true, modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 24.dp)
                .fillMaxWidth())

            ExposedDropdownMenuBox(modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 24.dp)
                .fillMaxWidth(), expanded = clientMenuExpanded, onExpandedChange = {
                clientMenuExpanded = it
            }) {
                TextField(value = viewModel.clientType.text, onValueChange = {url = it},
                    label = { Text("Client")},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded)
                    },
                    placeholder = {Text("Please select client")},
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth())
                ExposedDropdownMenu(expanded = clientMenuExpanded, onDismissRequest = { clientMenuExpanded = false }) {
                    supportedClientList.forEach {
                        DropdownMenuItem(text = { Text(text = it.text) }, onClick = {
                            viewModel.updateClientType(it.text)
                            clientMenuExpanded = false
                        })
                    }
                }
            }

        }

}
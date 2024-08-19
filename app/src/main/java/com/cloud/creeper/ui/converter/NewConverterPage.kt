package com.cloud.creeper.ui.converter

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cloud.creeper.R
import com.cloud.creeper.compose.AppTheme
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.repository.Gist
import com.cloud.creeper.repository.entity.Converter
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.ui.integration.AuthViewModel
import com.cloud.creeper.util.REPOSITORY_GITHUB
import com.cloud.creeper.util.SystemUtil
import com.cloud.creeper.util.parcelable
import kotlinx.coroutines.CoroutineScope

/**
 *
 * Created by cloud on 2024/2/22.
 */
private const val TAG = "NewConverterPage"

const val REQUEST_CODE_SELECT_SUBSCRIPTION = "select_subscription"
const val REQUEST_CODE_SELECT_GIST = "select_gist"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConverterPage(
    onUpClick: () -> Unit = {},
    viewModel: ConvertViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onSubscriptionClick: () -> Unit,
    onCloudIntegrationClick: () -> Unit,
    navForResult: (coroutineScope: CoroutineScope, requestCode: String, onResult: (data: Bundle) -> Unit) -> Unit,
    navToSelectGist: (coroutineScope: CoroutineScope, requestCode: String, auth: ServiceAuth, onResult: (data: Bundle) -> Unit) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var canSave by remember {
        mutableStateOf(viewModel.canSaveConverter)
    }
    val addState = viewModel.addState.collectAsStateWithLifecycle()

    val getAuthState = authViewModel.getTokenState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NewConverterTopAppBar(
                scrollBehavior = scrollBehavior,
                canSave = canSave,
                onUpClick = onUpClick,
                onSaveClick = {
                    val converter = ConverterWithSources(
                        Converter(SystemUtil.generateConverterId(), viewModel.converterName)
                            .apply {
                                description = viewModel.converterDescription
                                createdTime = System.currentTimeMillis()
                                updatedTime = System.currentTimeMillis()
                                outputType = viewModel.outputType
                            }, viewModel.subscriptionSourceList, emptyList()
                    )
                    viewModel.addConverter(converter)
                })
        }
    ) { contentPadding ->

        NewConverterScreen(
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            viewModel,
            getAuthState.value.data,
            onSubscriptionClick,
            onCloudIntegrationClick,
            onDataChanged = {
                canSave = it
            },
            navForResult,
            navToSelectGist
        )

    }

    when {
        addState.value.isLoading -> {
            LoadingIndicator()
        }

        addState.value.throwable != null -> {

        }

        addState.value.error != null -> {

        }

        addState.value.data != null -> {
            if (addState.value.data!!) {
                onUpClick()
            }
        }

        else -> {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewConverterTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier, canSave: Boolean, onUpClick: () -> Unit, onSaveClick: () -> Unit
) {

    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.New_Converter))
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onUpClick) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onSaveClick, enabled = canSave) {
                Icon(imageVector = Icons.Filled.Done, contentDescription = "Save converter")
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConverterScreen(
    modifier: Modifier = Modifier,
    viewModel: ConvertViewModel,
    auth: ServiceAuth?,
    onSubscriptionClick: () -> Unit,
    onCloudIntegrationClick: () -> Unit,
    onDataChanged: (canSave: Boolean) -> Unit,
    navForResult: (coroutineScope: CoroutineScope, requestCode: String, onResult: (data: Bundle) -> Unit) -> Unit,
    navToSelectGist: (coroutineScope: CoroutineScope, requestCode: String, auth: ServiceAuth, onResult: (data: Bundle) -> Unit) -> Unit
) {

    var clientMenuExpanded by remember { mutableStateOf(false) }

    var showAddSubscriptionDialog by remember {
        mutableStateOf(false)
    }

    var gistsOn by remember {
        mutableStateOf(viewModel.supportedCloudRepositories.contains(REPOSITORY_GITHUB))
    }
    val isAuthorized = auth != null && !auth.isExpired()

    val supportedClientList = mutableListOf<ClientType>()
    supportedClientList.add(ClientType.Clash)
    supportedClientList.add(ClientType.V2Ray)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {

        var url by remember {
            mutableStateOf("")
        }

        Text(
            text = stringResource(id = R.string.Name), modifier = Modifier
                .padding(start = 20.dp)
        )
        TextField(
            value = viewModel.converterName, onValueChange = {
                viewModel.updateConverterName(it)
                onDataChanged(viewModel.converterName.isNotEmpty() && viewModel.subscriptionSourceList.isNotEmpty())

            }, singleLine = true, modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth()
        )

        Text(
            text = stringResource(id = R.string.Description), modifier = Modifier
                .padding(start = 20.dp, top = 24.dp)
        )
        TextField(
            value = viewModel.converterDescription, onValueChange = {
                viewModel.updateConverterDescription(it)
                onDataChanged(viewModel.converterName.isNotEmpty() && viewModel.subscriptionSourceList.isNotEmpty())

            },
            placeholder = { Text(text = stringResource(id = R.string.Optional)) },
            singleLine = true, modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Subscription Url", modifier = Modifier
                    .padding(start = 20.dp)
            )

            IconButton(modifier = Modifier.padding(end = 20.dp), onClick = {
                navForResult(viewModel.viewModelScope, "SELECT_SUBSCRIPTION") { data ->
                    val requestCode = data.getString("REQUEST_CODE")
                    Log.d(TAG, "onResult(), requestCode=${requestCode}")
                    if ("SELECT_SUBSCRIPTION" == requestCode) {
                        val subscriptionSource =
                            data.parcelable<SubscriptionSource>("SUBSCRIPTION_SOURCE")
                        subscriptionSource?.let {
                            Log.d(TAG, "onResult(), url=${it.sourceUrl}")
                            url = it.sourceUrl
                            viewModel.subscriptionSourceList.add(it)
                            onDataChanged(viewModel.canSaveConverter)
                        }
                    }
                }

            }) {
                Icon(imageVector = Icons.TwoTone.Add, contentDescription = "Add subscription")
            }
        }

        viewModel.subscriptionSourceList.forEach { source ->

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.name,
                        modifier = Modifier
                            .padding(start = 12.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge
                    )

                    Text(
                        text = source.sourceUrl,
                        modifier = Modifier.padding(start = 12.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    )
                }


                IconButton(modifier = Modifier.padding(end = 20.dp), onClick = {
                    viewModel.subscriptionSourceList.remove(source)
                    onDataChanged(viewModel.canSaveConverter)
                }) {
                    Icon(
                        imageVector = Icons.TwoTone.Clear,
                        contentDescription = "Remove subscription url"
                    )
                }
            }
        }


        Text(
            text = "Output Type", modifier = Modifier
                .padding(start = 20.dp, top = 24.dp)
        )
        ExposedDropdownMenuBox(modifier = Modifier
            .padding(start = 12.dp, end = 12.dp)
            .fillMaxWidth(), expanded = clientMenuExpanded, onExpandedChange = {
            clientMenuExpanded = it
        }) {
            TextField(
                value = viewModel.outputType.name, onValueChange = {

                },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded)
                },
                placeholder = { Text("Please select client") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = clientMenuExpanded,
                onDismissRequest = { clientMenuExpanded = false }) {
                supportedClientList.forEach {
                    DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                        viewModel.updateClientType(it)
                        clientMenuExpanded = false
                    })
                }
            }
        }

        Text(
            text = "Save to Cloud", modifier = Modifier
                .padding(start = 20.dp, top = 24.dp)
        )
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(id = R.string.Gists))
            if (!isAuthorized) {
                TextButton(onClick = {  onCloudIntegrationClick() }) {
                    Text(text = stringResource(id = R.string.Authorize))
                }
            }
            Switch(
                checked = viewModel.supportedCloudRepositories.contains(REPOSITORY_GITHUB),
                onCheckedChange = {
                    gistsOn = it
                    if (it) {
                        viewModel.supportedCloudRepositories.add(REPOSITORY_GITHUB)

                    } else {
                        viewModel.supportedCloudRepositories.remove(REPOSITORY_GITHUB)
                    }
                }, enabled = isAuthorized)
        }
        AnimatedVisibility(visible = gistsOn) {
            Row( modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = auth?.userName!!)
                Text(text = stringResource(id = R.string.Create_a_new),
                        modifier = Modifier.padding(end = 24.dp).clickable {

                            navToSelectGist(viewModel.viewModelScope, REQUEST_CODE_SELECT_GIST, auth) { data ->
                                val requestCode = data.getString("REQUEST_CODE")
                                Log.d(TAG, "onResult(), requestCode=${requestCode}")
                                if (REQUEST_CODE_SELECT_GIST == requestCode) {

                                }
                            }
                        }, style = MaterialTheme.typography.labelMedium)
            }
        }

    }

    when {
        showAddSubscriptionDialog -> {
            AddSubscriptionUrlDialog(onDismissRequest = {
                showAddSubscriptionDialog = false
            }, onSaveClick = { name: String, url: String ->

            })
        }
    }

}

@Composable
private fun AddSubscriptionUrlDialog(
    onDismissRequest: () -> Unit,
    onSaveClick: (name: String, url: String) -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }
    var url by remember {
        mutableStateOf("")
    }

    var isSaveBtnEnabled by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest, modifier = Modifier,
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
                    }, label = { Text("Name") }, singleLine = true, modifier = Modifier
                )
                TextField(value = url,
                    onValueChange = {
                        url = it
                        isSaveBtnEnabled = name.isNotEmpty() && it.isNotEmpty()
                    },
                    label = { Text("Subscription Url") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 12.dp)
                )

            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}
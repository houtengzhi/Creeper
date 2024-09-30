package com.cloud.creeper.ui.converter


import android.os.Bundle
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.repository.GistFile
import com.cloud.creeper.repository.entity.CloudRepository
import com.cloud.creeper.repository.entity.Converter
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.ui.integration.AuthViewModel
import com.cloud.creeper.util.RepositoryType
import com.cloud.creeper.util.SystemUtil
import com.cloud.creeper.util.parcelable
import kotlinx.coroutines.CoroutineScope

/**
 *
 * Created by cloud on 2024/2/22.
 */
private const val TAG = "EditConverterPage"

private const val FLAG_NAME = 1 shl 0
private const val FLAG_DESC = 1 shl 1
private const val FLAG_SUBSCRIPTION_LIST = 1 shl 2
private const val FLAG_OUTPUT_TYPE = 1 shl 3
private const val FLAG_CLOUD_REPOSITORY_LIST = 1 shl 4


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditConverterPage(
    converterWithSources: ConverterWithSources,
    onUpClick: () -> Unit = {},
    viewModel: ConvertViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
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
            EditConverterTopAppBar(
                scrollBehavior = scrollBehavior,
                canSave = canSave,
                onUpClick = onUpClick,
                onSaveClick = {
                    val cloudRepositoryList = mutableListOf<CloudRepository>()
                    viewModel.supportedCloudRepositories.forEach {
                        val cloudRepository = CloudRepository(SystemUtil.generateCloudRepositoryId(), it)
                        cloudRepository.accessToken = getAuthState.value.data?.accessToken
                        cloudRepository.gistId = viewModel.gistFile?.gistId
                        cloudRepository.gistFileName = viewModel.gistFile?.fileName
                        cloudRepositoryList.add(cloudRepository)
                    }
                    val converter = ConverterWithSources(
                        Converter(SystemUtil.generateConverterId(), viewModel.converterName)
                            .apply {
                                description = viewModel.converterDescription
                                createdTime = System.currentTimeMillis()
                                updatedTime = System.currentTimeMillis()
                                outputType = viewModel.outputType
                            }, viewModel.subscriptionSourceList, cloudRepositoryList
                    )
                    viewModel.updateConverter(converter)
                })
        }
    ) { contentPadding ->

        EditConverterScreen(
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            converterWithSources = converterWithSources,
            viewModel,
            getAuthState.value.data,
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
private fun EditConverterTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier, canSave: Boolean, onUpClick: () -> Unit, onSaveClick: () -> Unit
) {

    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.Edit_Converter))
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
fun EditConverterScreen(
    modifier: Modifier = Modifier,
    converterWithSources: ConverterWithSources,
    viewModel: ConvertViewModel,
    auth: ServiceAuth?,
    onCloudIntegrationClick: () -> Unit,
    onDataChanged: (canSave: Boolean) -> Unit,
    navForResult: (coroutineScope: CoroutineScope, requestCode: String, onResult: (data: Bundle) -> Unit) -> Unit,
    navToSelectGist: (coroutineScope: CoroutineScope, requestCode: String, auth: ServiceAuth, onResult: (data: Bundle) -> Unit) -> Unit
) {

    var clientMenuExpanded by remember { mutableStateOf(false) }

    var showAddSubscriptionDialog by remember {
        mutableStateOf(false)
    }

    val isAuthorized = auth != null && !auth.isExpired()

    val supportedClientList = mutableListOf<ClientType>()
    supportedClientList.add(ClientType.Clash)
    supportedClientList.add(ClientType.V2Ray)

    converterWithSources.apply {
        viewModel.updateConverterName(converter.name)
        converter.description?.let { viewModel.updateConverterDescription(it) }
    }

    var dataChangeStatus by remember {
        mutableIntStateOf(0)
    }

    var name by remember {
        mutableStateOf(converterWithSources.converter.name)
    }

    var desc by remember {
        mutableStateOf(converterWithSources.converter.description)
    }

    val subscriptionSourceList = remember {
        mutableStateListOf(*converterWithSources.subscriptionSourceList.toTypedArray())
    }

    var outputType by remember {
        mutableStateOf(ClientType.Clash)
    }

    val supportedCloudRepositories = remember {
        var s = converterWithSources.cloudRepositoryList?.map { cloudRepository ->
            cloudRepository.type
        }?.toTypedArray()
        if (s == null) {
            s = arrayOf()
        }
        mutableStateListOf(*s)
    }

    var gistsOn by remember {
        var on = false
        converterWithSources.cloudRepositoryList?.forEach { cloudRepository ->
            if (RepositoryType.REPOSITORY_GITHUB == cloudRepository.type) {
                on = true
            }
        }
        mutableStateOf(on)
    }

    var gistId by remember {
        var f: String? = null
        converterWithSources.cloudRepositoryList?.forEach { cloudRepository ->
            if (RepositoryType.REPOSITORY_GITHUB == cloudRepository.type) {
                f = cloudRepository.gistId
            }
        }
        mutableStateOf(f)
    }

    var gistFileName by remember {
        var f: String? = null
        converterWithSources.cloudRepositoryList?.forEach { cloudRepository ->
            if (RepositoryType.REPOSITORY_GITHUB == cloudRepository.type) {
                f = cloudRepository.gistFileName
            }
        }
        mutableStateOf(f)
    }



    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {

        Text(
            text = stringResource(id = R.string.Name), modifier = Modifier
                .padding(start = 20.dp)
        )
        TextField(
            value = name, onValueChange = {
                name = it.trim()
                val changed = name.isNotEmpty() && name != converterWithSources.converter.name
                dataChangeStatus = if (changed) dataChangeStatus or FLAG_NAME else dataChangeStatus and FLAG_NAME.inv()
                onDataChanged( dataChangeStatus != 0)

            }, singleLine = true, modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth()
        )

        Text(
            text = stringResource(id = R.string.Description), modifier = Modifier
                .padding(start = 20.dp, top = 24.dp)
        )
        TextField(
            value = if (desc != null) desc!! else "", onValueChange = {
                desc = it.trim()
                val changed = desc != converterWithSources.converter.description
                dataChangeStatus = if (changed) dataChangeStatus or FLAG_DESC else dataChangeStatus and FLAG_DESC.inv()
                onDataChanged(dataChangeStatus != 0)

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
                            if (!subscriptionSourceList.contains(it)) {
                                subscriptionSourceList.add(it)
                                val changed = subscriptionSourceList != converterWithSources.subscriptionSourceList
                                dataChangeStatus = if (changed) dataChangeStatus or FLAG_SUBSCRIPTION_LIST else dataChangeStatus and FLAG_SUBSCRIPTION_LIST.inv()
                                onDataChanged(dataChangeStatus != 0)
                            }
                        }
                    }
                }

            }) {
                Icon(imageVector = Icons.TwoTone.Add, contentDescription = "Add subscription")
            }
        }

        subscriptionSourceList.forEach { source ->

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
                    subscriptionSourceList.remove(source)
                    val changed = subscriptionSourceList != converterWithSources.subscriptionSourceList
                    dataChangeStatus = if (changed) dataChangeStatus or FLAG_SUBSCRIPTION_LIST else dataChangeStatus and FLAG_SUBSCRIPTION_LIST.inv()
                    onDataChanged(dataChangeStatus != 0)
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
                value = outputType.name, onValueChange = {

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
                        outputType = it
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
                checked = supportedCloudRepositories.contains(RepositoryType.REPOSITORY_GITHUB),
                onCheckedChange = {
                    gistsOn = it
                    if (gistsOn) {
                        supportedCloudRepositories.add(RepositoryType.REPOSITORY_GITHUB)

                    } else {
                        supportedCloudRepositories.remove(RepositoryType.REPOSITORY_GITHUB)
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

                var gistMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(modifier = Modifier
                    .padding(end = 12.dp)
                    .fillMaxWidth()
                    , expanded = gistMenuExpanded, onExpandedChange = {
                        gistMenuExpanded = it
                    }) {

                    Text(text = if (gistFileName == null) stringResource(id = R.string.Create_a_new) else gistFileName!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp)
                            .menuAnchor()
                        , style = MaterialTheme.typography.labelMedium)

                    ExposedDropdownMenu(
                        expanded = gistMenuExpanded,
                        onDismissRequest = { gistMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.Create_a_new)) },
                            onClick = {
                                gistMenuExpanded = false
                                gistId = null
                                gistFileName = null

                            })
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.Overwrite_the_existing)) },
                            onClick = {
                                gistMenuExpanded = false
                                navToSelectGist(
                                    viewModel.viewModelScope,
                                    REQUEST_CODE_SELECT_GIST,
                                    auth
                                ) { data ->
                                    val requestCode = data.getString("REQUEST_CODE")
                                    Log.d(TAG, "onResult(), requestCode=${requestCode}")
                                    if (REQUEST_CODE_SELECT_GIST == requestCode) {
                                        val gistFile = data.parcelable<GistFile>(KEY_GIST_FILE)
                                        gistId = gistFile?.gistId
                                        gistFileName = gistFile?.fileName
                                    }
                                }
                            })
                    }
                }
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
package com.neuroproject.neuro.screens.devicesearch

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.neuroproject.neuro.models.CapsuleInitializedState
import com.neuroproject.neuro.models.DeviceConnectionState
import com.neuroproject.neuro.models.DeviceInfo
import com.neuroproject.neuro.services.CapsuleDeviceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext
// Hilt нужен для привязки зависимостей и для привязки к жизненному циклу navgraph
@HiltViewModel
//Если в данном скрине напрямую используется наше устройство, то нужно сделать @Inject конструктора
class DeviceSearchScreenViewModel @Inject constructor(dm: CapsuleDeviceManager) :ViewModel() {

    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val _foundDevices = MutableStateFlow<Array<DeviceInfo>>(emptyArray())

    private val _initState = MutableStateFlow<CapsuleInitializedState>(CapsuleInitializedState.NonInitialized);

    val foundDevices = _foundDevices.asStateFlow()
    val deviceState = dm.connectionState

    val capsuleDM: CapsuleDeviceManager = dm

    init {
        if (deviceState.value == DeviceConnectionState.connected)
        {}
        else {

           //коллбэк на подключение
            capsuleDM.initializeStateChanged = {state ->
                scope.launch { _initState.emit(state) }
                when (state) {
                    CapsuleInitializedState.Initialized ->  capsuleDM.startSearch()
                    CapsuleInitializedState.NonInitialized -> Log.d("Search", "Capsule is not init")
                }
            }

            capsuleDM.devicesFound = {
                scope.launch {
                    _foundDevices.emit(it)
                }
            }


            capsuleDM.initCapsule()
        }
    }

    fun connect(id: String){
        capsuleDM.connect(id)
    }

    fun stopSearch() {

    }
}
package com.neuroproject.neuro.screens.sensorchecking

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neuroproject.neuro.services.CapsuleDeviceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

//НАДО ПОЛНОСТЬЮ ПЕРЕДЕЛАТЬ. Да, эта часть работает, безусловно. Но это не наш код


enum class ResistState(val value:Int){
    BAD(0),
    OK(1)
}
data class ResistStateRecord(val o1:ResistState = ResistState.BAD, val o2:ResistState= ResistState.BAD, val t3:ResistState= ResistState.BAD, val t4:ResistState= ResistState.BAD){
    fun isAllOk():Boolean{
        return o1 == ResistState.OK && o2 == ResistState.OK && t3==ResistState.OK && t4 == ResistState.OK
    }
}

@HiltViewModel
class SensorCheckingScreenViewModel @Inject constructor(dm: CapsuleDeviceManager) : ViewModel() {
    val capsuleDM: CapsuleDeviceManager = dm

    private val _scope = CoroutineScope(EmptyCoroutineContext)
    private val _resistState = MutableStateFlow(ResistStateRecord())
    val resistState = _resistState.asStateFlow()

    init {
        capsuleDM.resistanceReceived = { o1: Double, o2: Double, t3: Double, t4: Double ->
            Log.d(
                "SensorCheckingScreenViewModel",
                "o1 = " + o1 + ", o2 = " + o2 + ", t3 = " + t3 + "t4 = " + t4
            )
            _scope.launch {
                _resistState.emit(
                    ResistStateRecord(
                        setResistColor(o1),
                        setResistColor(o2),
                        setResistColor(t3),
                        setResistColor(t4)
                    )
                )
            }
        }
    }

    fun start() {
        Log.d("TAG", "TRY START RESIST")
        capsuleDM.startResistance()
    }

    fun finish() {
        capsuleDM.stopResistance()
    }

    private fun setResistColor(value: Double): ResistState {
        if (value > 1000) {
            return ResistState.BAD
        } else {
            return ResistState.OK
        }
    }
}
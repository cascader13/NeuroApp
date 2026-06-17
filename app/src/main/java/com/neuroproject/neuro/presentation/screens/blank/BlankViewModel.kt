package com.neuroproject.neuro.presentation.screens.blank

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.neuroproject.neuro.services.CapsuleDeviceManager


@HiltViewModel
class BlankViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val dm: CapsuleDeviceManager
) : ViewModel() {

}
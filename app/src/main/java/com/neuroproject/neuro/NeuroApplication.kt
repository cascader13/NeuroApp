package com.neuroproject.neuro


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
@Composable
fun NeuroApplication(modifier: Modifier = Modifier){

    NeuroApplicationTheme {
        NeuroNavGraph(modifier = modifier)
    }
}

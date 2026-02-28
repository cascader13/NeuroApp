package com.neuroproject.neuro.screens.subtest

sealed class SubTestScreenState {
    object Instruction : SubTestScreenState()
    object Question : SubTestScreenState()
    object Comment : SubTestScreenState()
}
// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

/**
 * \brief Capsule error
 */
typedef enum clCError_Code {
    clCError_OK = 0,
    clCError_FailedToConnect,
    clCError_FailedToInitConnection,
    clCError_FailedToInitialize,
    clCError_DeviceError,
    clCError_IndividualNFBNotCalibrated,
    clCError_NotReceived,
    clCError_NullPointer,
    clCError_ModuleAlreadyExists,
    clCError_ModuleIsNotSupported,
    clCError_FailedToSendData,
    clCError_IndexOutOfRange,
    clCError_EmptyCollection,
    clCError_NotFound,
    clCError_SizeMismatch,
    clCError_UnknownEnum,
    clCError_Unknown = 255
} clCError_Code;

typedef struct clCError {
    char message[256];
    bool success;
    clCError_Code code;
} clCError;

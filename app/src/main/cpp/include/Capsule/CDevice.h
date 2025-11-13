// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CDeviceLocator.h"
#include "CEEGArtifacts.h"
#include "CEEGTimedData.h"
#include "CError.h"
#include "CPSDData.h"
#include "CResistances.h"

#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief Device.
 *
 * Controls connection to the device
 */
CLC_CLASS_WN(DevicePrivate, clCDevice);

typedef enum clCDevice_Mode {
    clCDevice_Mode_Resistance = 0,
    clCDevice_Mode_Signal,
    clCDevice_Mode_SignalAndResist,
    clCDevice_Mode_StartMEMS,
    clCDevice_Mode_StopMEMS,
    clCDevice_Mode_StartPPG,
    clCDevice_Mode_StopPPG,
} clCDevice_Mode;

/**
 * \brief Device connection status.
 */
typedef enum clCDevice_ConnectionStatus {
    clCDevice_ConnectionState_Disconnected = 0,
    clCDevice_ConnectionState_Connected = 1,
    clCDevice_ConnectionState_UnsupportedConnection = 2,
} clCDevice_ConnectionStatus;

CLC_STRUCT_WNN(DeviceChannelNames, clCDevice_ChannelNames, device);

/**
 * Create device for interaction.
 *
 * \param locator device locator handle
 * \param deviceSerial device ID string
 * \param error
 * \returns device handle
 */
CL_DLL clCDevice clCDeviceLocator_CreateDevice(clCDeviceLocator locator, const char* deviceSerial, clCError* error) NOEXCEPT;

typedef void (*clCDevice_ConnectionStatusChangedHandler)(clCDevice, clCDevice_ConnectionStatus) NOEXCEPT;
CL_DLL void clCDevice_SetOnConnectionStatusChangedEvent(clCDevice device, clCDevice_ConnectionStatusChangedHandler handler) NOEXCEPT;

typedef void (*clCDevice_OnResistanceUpdateHandler)(clCDevice, clCResistance) NOEXCEPT;
CL_DLL void clCDevice_SetOnResistanceUpdateEvent(clCDevice device, clCDevice_OnResistanceUpdateHandler handler) NOEXCEPT;

typedef void (*clCDevice_OnBatteryChargeUpdateHandler)(clCDevice, uint8_t) NOEXCEPT;
CL_DLL void clCDevice_SetOnBatteryChargeUpdateEvent(clCDevice device, clCDevice_OnBatteryChargeUpdateHandler) NOEXCEPT;

typedef void (*clCDevice_ModeSwitchedHandler)(clCDevice, clCDevice_Mode) NOEXCEPT;
CL_DLL void clCDevice_SetOnModeSwitchedEvent(clCDevice device, clCDevice_ModeSwitchedHandler handler) NOEXCEPT;

typedef void (*clCDevice_EEGDataHandler)(clCDevice, clCEEGTimedData) NOEXCEPT;
CL_DLL void clCDevice_SetOnEEGDataEvent(clCDevice device, clCDevice_EEGDataHandler handler) NOEXCEPT;

typedef void (*clCDevice_PSDDataHandler)(clCDevice, clCPSDData) NOEXCEPT;
CL_DLL void clCDevice_SetOnPSDDataEvent(clCDevice device, clCDevice_PSDDataHandler handler) NOEXCEPT;

typedef void (*clCDevice_EEGArtifactsHandler)(clCDevice, clCEEGArtifacts) NOEXCEPT;
CL_DLL void clCDevice_SetOnEEGArtifactsEvent(clCDevice device, clCDevice_EEGArtifactsHandler handler) NOEXCEPT;

typedef void (*clCDevice_ErrorHandler)(clCDevice, const char*) NOEXCEPT;
CL_DLL void clCDevice_SetOnErrorEvent(clCDevice device, clCDevice_ErrorHandler handler) NOEXCEPT;

/**
 * Connect to device. Non-blocking - OnConnectionStateChanged event is
 * fired when connected.
 *
 * \param device device handle
 * \param bipolarChannels true if channels should be joined pairwise to provide bipolar signal
 * (the signal is the difference between two signals in the channel pair)
 * \param error out parameter for error
 */
CL_DLL void clCDevice_Connect(clCDevice device, bool bipolarChannels, clCError* error) NOEXCEPT;
/**
 * Disconnect from device. Non-blocking - OnConnectionStateChanged event
 * is fired when disconnected.
 *
 * \param device device handle
 * \param error out parameter for error
 */
CL_DLL void clCDevice_Disconnect(clCDevice device, clCError* error) NOEXCEPT;
/**
 * \brief Get device battery charge
 *
 * \param device device handle
 * \param error out parameter for error
 * \return battery charge
 */
CL_DLL uint8_t clCDevice_GetBatteryCharge(clCDevice device, clCError* error) NOEXCEPT;

/**
 * \brief Start device
 *
 * \param device device handle
 * \param error out parameter for error
 */
CL_DLL void clCDevice_Start(clCDevice device, clCError* error) NOEXCEPT;
/**
 * \brief Stop device
 *
 * \param device device handle
 * \param error out parameter for error
 */
CL_DLL void clCDevice_Stop(clCDevice device, clCError* error) NOEXCEPT;

/**
 * Get current device mode
 * @param device device handle
 * @return device mode
 */
CL_DLL clCDevice_Mode clCDevice_GetMode(clCDevice device) NOEXCEPT;

/**
 * Is device connected to Capsule.
 *
 * \param device device handle
 * \param error out parameter for error
 */
CL_DLL bool clCDevice_IsConnected(clCDevice device, clCError* error) NOEXCEPT;

/**
 * Get device information.
 *
 * \param device device handle
 * \param error out parameter for error
 * \returns device info handle
 */
CL_DLL clCDeviceInfo clCDevice_GetInfo(clCDevice device, clCError* error) NOEXCEPT;

/**
 * Get device's EEG sample rate in Hz
 *
 * \param device device handle
 * \param error out parameter for error
 * \returns device EEG sample rate
 */
CL_DLL float clCDevice_GetEEGSampleRate(clCDevice device, clCError* error) NOEXCEPT;

/**
 * Get device's PPG sample rate in Hz
 *
 * \param device device handle
 * \param error out parameter for error
 * \returns device PPG sample rate
 */
CL_DLL float clCDevice_GetPPGSampleRate(clCDevice device, clCError* error) NOEXCEPT;
/**
 * Get device's MEMS sample rate in Hz
 *
 * \param device device handle
 * \param error out parameter for error
 * \returns device MEMS sample rate
 */
CL_DLL float clCDevice_GetMEMSSampleRate(clCDevice device, clCError* error) NOEXCEPT;
/**
 * Get device's infrared light amplitude
 *
 * \param device device handle
 * \param error out parameter for error
 * \returns device infrared light amplitude
 */
CL_DLL int32_t clCDevice_GetPPGIrAmplitude(clCDevice device, clCError* error) NOEXCEPT;
/**
 * Get device's red light amplitude
 *
 * \param device device handle
 * \param error out parameter for error
 * \returns device red light amplitude
 */
CL_DLL int32_t clCDevice_GetPPGRedAmplitude(clCDevice device, clCError* error) NOEXCEPT;

/**
 * Get names of channels of the device
 *
 * \param device device handle
 * \param error out parameter for error
 * \returns device channel names handle
 */
CL_DLL clCDevice_ChannelNames clCDevice_GetChannelNames(clCDevice device, clCError* error) NOEXCEPT;

CL_DLL int32_t clCDevice_ChannelNames_GetChannelsCount(clCDevice_ChannelNames deviceChannels, clCError* error) NOEXCEPT;

CL_DLL int32_t clCDevice_ChannelNames_GetChannelIndexByName(clCDevice_ChannelNames deviceChannels, const char* channelName, clCError* error) NOEXCEPT;

CL_DLL const char* clCDevice_ChannelNames_GetChannelNameByIndex(clCDevice_ChannelNames deviceChannels, int32_t channelIndex, clCError* error) NOEXCEPT;

/**
 * Release the device.
 *
 * \param device device handle
 */
CL_DLL void clCDevice_Release(clCDevice device) NOEXCEPT;

#ifdef __cplusplus
}
#endif

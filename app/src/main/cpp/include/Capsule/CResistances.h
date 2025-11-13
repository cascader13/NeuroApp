// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief Used to receive channel resistances from Capsule.
 *
 * Contains electrode resistances of the device
 */
CLC_STRUCT_WNN(Resistances, clCResistance, device);

/**
 * \brief Get total number of resistance channels.
 *
 * \param resistance resistances handle
 * \returns number of channels
 */
CL_DLL int32_t clCResistance_GetCount(clCResistance resistance) NOEXCEPT;
/**
 * \brief Get channel name by index.
 *
 * \param resistance resistances handle
 * \param index channel index
 * \returns channel name
 */
CL_DLL const char* clCResistance_GetChannelName(clCResistance resistance, int32_t index) NOEXCEPT;
/**
 * \brief Get channel resistance by index.
 *
 * \param resistance resistances handle
 * \param index channel index
 * \returns resistance value
 */
CL_DLL float clCResistance_GetValue(clCResistance resistance, int32_t index) NOEXCEPT;

#ifdef __cplusplus
}
#endif

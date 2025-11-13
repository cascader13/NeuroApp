cmake_minimum_required(VERSION 3.22)
project(native-lib)
set(GRADLE_TOP_PROJECT_DIR ${PROJECT_SOURCE_DIR}/../../..)


set(CAPSULE_API_SHARED_LIB_DIR ${GRADLE_TOP_PROJECT_DIR}/build/capsule_shared/${CMAKE_ANDROID_ARCH_ABI})
message(${CAPSULE_API_SHARED_LIB_DIR})

include_directories(
        include
        src/helpers
)
file(GLOB SOURCE_JNI_FILES src/jni/*.cpp)
file(GLOB SOURCE_HELPERS_FILES src/helpers/*.cpp)
file(GLOB SOURCE_FILES src/*.cpp)

add_library(CapsuleAPI SHARED IMPORTED)
set_target_properties(CapsuleAPI
        PROPERTIES IMPORTED_LOCATION
        ${CAPSULE_API_SHARED_LIB_DIR}/libCapsuleClient.so)

add_library(${PROJECT_NAME} SHARED ${SOURCE_FILES} ${SOURCE_JNI_FILES} ${SOURCE_HELPERS_FILES})




find_library(log-lib
        log)


target_link_libraries(${PROJECT_NAME}
        ${log-lib}
        log
        CapsuleAPI
)
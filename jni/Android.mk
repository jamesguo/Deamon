LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_MODULE    := daemon
LOCAL_SRC_FILES := daemon.c
LOCAL_MODULE_FILENAME    := "libdaemon"
include $(BUILD_EXECUTABLE)

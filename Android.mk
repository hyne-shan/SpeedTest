LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

src_dirs := src
res_dirs := res

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_JAVA_LIBRARIES :=
LOCAL_STATIC_JAVA_LIBRARIES := 


LOCAL_PACKAGE_NAME := SpeedTest
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_TAGS := optional

include $(BUILD_PACKAGE)



#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "VidyoClient.h"
#include "include/AndroidDebug.h"

jobject applicationJniObj = 0;
JavaVM* global_vm = 0;
static VidyoBool joinStatus = 0;
int x;
int y;
static VidyoBool allVideoDisabled = 0;

void SampleSwitchCamera(const char *name);
void SampleStartConference();
void SampleEndConference();
void SampleLoginSuccessful();
void LibraryStarted();
void Event_GroupChat(const VidyoClientOutEventGroupChat * chat);

// Callback for out-events from VidyoClient
#define PRINT_EVENT(X) if(event==X) LOGI("GuiOnOutEvent recieved %s", #X);
void SampleGuiOnOutEvent(VidyoClientOutEvent event,
				   VidyoVoidPtr param,
				   VidyoUint paramSize,
				   VidyoVoidPtr data)
{
	LOGI("GuiOnOutEvent enter Event = %d\n",(int) event);
	if(event == VIDYO_CLIENT_OUT_EVENT_LICENSE)
	{
		VidyoClientOutEventLicense *eventLicense;
		eventLicense = (VidyoClientOutEventLicense *) param;

		VidyoUint error = eventLicense->error;
		VidyoUint vmConnectionPath = eventLicense->vmConnectionPath;
		VidyoBool OutOfLicenses = eventLicense->OutOfLicenses;

		LOGI("License Error: errorid=%d vmConnectionPath=%d OutOfLicense=%d\n", error, vmConnectionPath, OutOfLicenses);
	}
	else if(event == VIDYO_CLIENT_OUT_EVENT_SIGN_IN)
	{
		VidyoClientOutEventSignIn *eventSignIn;
		eventSignIn = (VidyoClientOutEventSignIn *) param;

		VidyoUint activeEid = eventSignIn->activeEid;
		VidyoBool signinSecured = eventSignIn->signinSecured;

		LOGI("activeEid=%d signinSecured=%d\n", activeEid, signinSecured);

			// close tools bar .
		  	VidyoClientInEventEnable event1;
		  	event1.willEnable = VIDYO_FALSE;
		    VidyoBool bret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_ENABLE_BUTTON_BAR, &event1,sizeof(VidyoClientInEventEnable));


		/*
		 * If the EID is not setup, it will return activeEid = 0
		 * in this case, we invoke the license request using below event
		 */
		if(!activeEid)
			(void)VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_LICENSE, NULL, 0);
	    VidyoClientRequestCurrentUser user_id;
	    VidyoUint ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CURRENT_USER, &user_id, sizeof(user_id));
	    LOGE("SG: logged in with %d. user_id.CurrentUserID: %s, user_id.CurrentUserDisplay: %s .", ret, user_id.currentUserID, user_id.currentUserDisplay);
	}
    else if(event == VIDYO_CLIENT_OUT_EVENT_SIGNED_IN)
	{
        // Send message to Client/application
		SampleLoginSuccessful();
    }
	else if(event == VIDYO_CLIENT_OUT_EVENT_CONFERENCE_ACTIVE)
	{
		LOGI("Join Conference Event - received VIDYO_CLIENT_OUT_EVENT_CONFERENCE_ACTIVE\n");
        SampleStartConference();
		joinStatus = 1;
		doResize(x,y);
	}
	else if(event == VIDYO_CLIENT_OUT_EVENT_CONFERENCE_ENDED)
	{
		LOGI("Left Conference Event\n");
		SampleEndConference();
		joinStatus = 0;
	}
	else if(event == VIDYO_CLIENT_OUT_EVENT_INCOMING_CALL)
	{
		LOGW("VIDYO_CLIENT_OUT_EVENT_INCOMING_CALL\n");
		VidyoBool ret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_ANSWER, NULL, 0);
		LOGW("SG: VIDYO_CLIENT_OUT_EVENT_INCOMING_CALL %d.", ret);
	}
    /*else if(event == VIDYO_CLIENT_OUT_EVENT_ADD_SHARE)
    {
        VidyoClientRequestWindowShares shareRequest;
        VidyoUint result;

        LOGI("VIDYO_CLIENT_OUT_EVENT_ADD_SHARE\n");
        memset(&shareRequest, 0, sizeof(shareRequest));
        shareRequest.requestType = LIST_SHARING_WINDOWS;
         VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_WINDOW_SHARES,
                                              &shareRequest,
                                              sizeof(shareRequest));
        if (result != VIDYO_CLIENT_ERROR_OK)
        {
            LOGE("VIDYO_CLIENT_REQUEST_GET_WINDOW_SHARES failed");
        }
        else
        {
            LOGI("VIDYO_CLIENT_REQUEST_GET_WINDOW_SHARES success:%d, %d", shareRequest.shareList.numApp, shareRequest.shareList.currApp);

            shareRequest.shareList.newApp = shareRequest.shareList.currApp = 1;
            shareRequest.requestType = ADD_SHARING_WINDOW;
    
            result = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_WINDOW_SHARES,
                                              &shareRequest,
                                              sizeof(shareRequest));

            if (result != VIDYO_CLIENT_ERROR_OK)
            {
                LOGE("VIDYO_CLIENT_REQUEST_SET_WINDOW_SHARES failed\n");

            }
            else
            {
                LOGI("VIDYO_CLIENT_REQUEST_SET_WINDOW_SHARES success\n");
            }
        }
	}*/
	else if (event == VIDYO_CLIENT_OUT_EVENT_DEVICE_SELECTION_CHANGED)
	{
		VidyoClientOutEventDeviceSelectionChanged *eventOutDeviceSelectionChg = (VidyoClientOutEventDeviceSelectionChanged *)param;

		if (eventOutDeviceSelectionChg->changeType == VIDYO_CLIENT_USER_MESSAGE_DEVICE_SELECTION_CHANGED)
		{
			if (eventOutDeviceSelectionChg->deviceType == VIDYO_CLIENT_DEVICE_TYPE_VIDEO) 
			{
				SampleSwitchCamera((char *)eventOutDeviceSelectionChg->newDeviceName);
			}
		}
	}
	else if (event == VIDYO_CLIENT_OUT_EVENT_LOGIC_STARTED)
	{
		LOGI("Library Started Event\n");
		LibraryStarted();
	}

	else if(event == VIDYO_CLIENT_OUT_EVENT_GROUP_CHAT)
	{
		VidyoClientOutEventGroupChat * chatmsg = (VidyoClientOutEventGroupChat *)param;
		LOGI("chatmsg:%s",chatmsg->message);
		//发送消息到java
		Event_GroupChat(chatmsg);
	}

}


static JNIEnv *getJniEnv(jboolean *isAttached)
{
	int status;
	JNIEnv *env;
	*isAttached = 0;

	status = (*global_vm)->GetEnv(global_vm, (void **) &env, JNI_VERSION_1_4);
	if (status < 0) 
	{
		//LOGE("getJavaEnv: Failed to get Java VM");
		status = (*global_vm)->AttachCurrentThread(global_vm, &env, NULL);
		if(status < 0) 
		{
			LOGE("getJavaEnv: Failed to get Attach Java VM");
			return NULL;
		}
		//LOGE("getJavaEnv: Attaching to Java VM");
		*isAttached = 1;
	}

	return env;
}

static jmethodID getApplicationJniMethodId(JNIEnv *env, jobject obj, const char* methodName, const char* methodSignature)
{
	jmethodID mid;
	jclass appClass;

	appClass = (*env)->GetObjectClass(env, obj);
	if (!appClass) 
	{
		LOGE("getApplicationJniMethodId - getApplicationJniMethodId: Failed to get applicationJni obj class");
		return NULL;
	}
	
	mid = (*env)->GetMethodID(env, appClass, methodName, methodSignature);
	if (mid == NULL)
	{
		LOGE("getApplicationJniMethodId - getApplicationJniMethodId: Failed to get %s method", methodName);
		return NULL;
	}
	
	return mid;
}


void Event_GroupChat(const VidyoClientOutEventGroupChat * chat)
{
        jboolean isAttached;
        JNIEnv *env;
        jmethodID mid;
        jstring js;
        env = getJniEnv(&isAttached);
        if (env == NULL)
                goto FAIL0;

        mid = getApplicationJniMethodId(env, applicationJniObj, "GroupChat", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
        if (mid == NULL)
                goto FAIL1;

        jstring jsuri = (*env)->NewStringUTF(env, chat->uri);
        jstring jsmsg = (*env)->NewStringUTF(env, chat->message);
        jstring jsdisplayname = (*env)->NewStringUTF(env, chat->displayName);

        (*env)->CallVoidMethod(env, applicationJniObj, mid, jsmsg,jsdisplayname,jsuri);

		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
        LOGE("Event_GroupChat End");
        return;
FAIL1:
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
FAIL0:
        LOGE("Event_GroupChat FAILED");
        return;
}

void LibraryStarted()
{
	jboolean isAttached;
	JNIEnv *env;
	jmethodID mid;
	jstring js;
	LOGE("LibraryStarted Begin");
	env = getJniEnv(&isAttached);
	if (env == NULL)
		goto FAIL0;

	mid = getApplicationJniMethodId(env, applicationJniObj, "libraryStartedCallback", "()V");
	if (mid == NULL)
		goto FAIL1;

	(*env)->CallVoidMethod(env, applicationJniObj, mid);

	if (isAttached)
	{
		(*global_vm)->DetachCurrentThread(global_vm);
	}
	LOGE("LibraryStarted End");
	return;
	FAIL1:
	if (isAttached)
	{
		(*global_vm)->DetachCurrentThread(global_vm);
	}
	FAIL0:
	LOGE("LibraryStarted FAILED");
	return;
}

void SampleStartConference()
{
    jboolean isAttached;
    JNIEnv *env;
    jmethodID mid;
    jstring js;
    LOGE("SampleStartConference Begin");
    env = getJniEnv(&isAttached);
    if (env == NULL)
        goto FAIL0;
    
    mid = getApplicationJniMethodId(env, applicationJniObj, "callStartedCallback", "()V");
    if (mid == NULL)
        goto FAIL1;
    
    (*env)->CallVoidMethod(env, applicationJniObj, mid);
	
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
    LOGE("SampleStartConference End");
    return;
FAIL1:
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
FAIL0:
    LOGE("SampleStartConference FAILED");
    return;
}

void SampleLoginSuccessful()
{
    jboolean isAttached;
    JNIEnv *env;
    jmethodID mid;
    jstring js;
    LOGE("SampleLoginSuccessful Begin");
    env = getJniEnv(&isAttached);
    if (env == NULL)
        goto FAIL0;
    
    mid = getApplicationJniMethodId(env, applicationJniObj, "loginSuccessfulCallback", "()V");
    if (mid == NULL)
        goto FAIL1;
    
    (*env)->CallVoidMethod(env, applicationJniObj, mid);
	
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
    LOGE("SampleLoginSuccessful End");
    return;
FAIL1:
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
FAIL0:
    LOGE("SampleLoginSuccessful FAILED");
    return;
}

void SampleEndConference()
{
        jboolean isAttached;
        JNIEnv *env;
        jmethodID mid;
        jstring js;
        LOGE("SampleEndConference Begin");
        env = getJniEnv(&isAttached);
        if (env == NULL)
                goto FAIL0;

        mid = getApplicationJniMethodId(env, applicationJniObj, "callEndedCallback", "()V");
        if (mid == NULL)
                goto FAIL1;

        (*env)->CallVoidMethod(env, applicationJniObj, mid);
	
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
        LOGE("SampleEndConference End");
        return;
FAIL1:
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
FAIL0:
        LOGE("SampleEndConference FAILED");
        return;
}

void SampleSwitchCamera(const char *name)
{
        jboolean isAttached;
        JNIEnv *env;
        jmethodID mid;
        jstring js;
        LOGE("SampleSwitchCamera Begin");
        env = getJniEnv(&isAttached);
        if (env == NULL)
                goto FAIL0;

        mid = getApplicationJniMethodId(env, applicationJniObj, "cameraSwitchCallback", "(Ljava/lang/String;)V");
        if (mid == NULL)
                goto FAIL1;

        js = (*env)->NewStringUTF(env, name);
        (*env)->CallVoidMethod(env, applicationJniObj, mid, js);
	
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
        LOGE("SampleSwitchCamera End");
        return;
FAIL1:
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
FAIL0:
        LOGE("SampleSwitchCamera FAILED");
        return;
}

static jobject * SampleInitCacheClassReference(JNIEnv *env, const char *classPath) 
{
	jclass appClass = (*env)->FindClass(env, classPath);
	if (!appClass) 
	{
		LOGE("cacheClassReference: Failed to find class %s", classPath);
		return ((jobject*)0);
	}
	
	jmethodID mid = (*env)->GetMethodID(env, appClass, "<init>", "()V");
	if (!mid) 
	{
		LOGE("cacheClassReference: Failed to construct %s", classPath);
		return ((jobject*)0);
	}
	jobject obj = (*env)->NewObject(env, appClass, mid);
	if (!obj) 
	{
		LOGE("cacheClassReference: Failed to create object %s", classPath);
		return ((jobject*)0);
	}
	return (*env)->NewGlobalRef(env, obj);
}

JNIEXPORT void Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_Construct(JNIEnv* env, jobject javaThis,
                jstring caFilename, jstring logDir, jstring pathDir, jobject defaultActivity) {

	FUNCTION_ENTRY;
    
    
    VidyoClientAndroidRegisterDefaultVM(global_vm);
    VidyoClientAndroidRegisterDefaultApp(env, defaultActivity);
    
    const char *pathDirC = (*env)->GetStringUTFChars(env, pathDir, NULL);
    const char *logDirC = (*env)->GetStringUTFChars(env, logDir, NULL);
	const char *certificatesFileNameC = (*env)->GetStringUTFChars(env, caFilename, NULL);
	

 	//const char *logBaseFileName = "VidyoClientSample_";
 	//const char *installedDirPath = NULL;
 	//static const VidyoUint DEFAULT_LOG_SIZE = 1000000;
	//const char *logLevelsAndCategories = "fatal error warning debug@App info@AppEmcpClient debug@LmiApp debug@AppGui info@AppGui";
	VidyoRect videoRect = {(VidyoInt)(0), (VidyoInt)(0), (VidyoUint)(100), (VidyoUint)(100)};
    //VidyoUint logSize = DEFAULT_LOG_SIZE;

	applicationJniObj = SampleInitCacheClassReference(env, "com/esoon/vidyosample/VidyoSampleApplicationkevin");
	// This will start logging to LogCat
    // Use mainly for debugging purposes
	VidyoClientConsoleLogConfigure(VIDYO_CLIENT_CONSOLE_LOG_CONFIGURATION_ALL);

	// Start the VidyoClient Library
    
    /* VidyoBool returnValue = VidyoClientStart(SampleGuiOnOutEvent,
     NULL,
     "/data/data/com.vidyo.vidyosample/cache/",
     logBaseFileName,
     "/data/data/com.vidyo.vidyosample/files/",
     logLevelsAndCategories,
     logSize,
     (VidyoWindowId)(0),
     &videoRect,
     NULL,
     &profileParam,NULL);
     if (returnValue)
*/
    VidyoClientLogParams logParam = {0};
    logParam.logLevelsAndCategories = "fatal error warning debug@App info@AppEmcpClient debug@LmiApp debug@AppGui info@AppGui";
    logParam.logSize = 5000000;
//    logParam.pathToLogDir = "/data/data/com.vidyo.vidyosample/cache/";
    logParam.pathToLogDir = logDirC;
    logParam.logBaseFileName = "VidyoClientSample_";
//    logParam.pathToDumpDir = "/data/data/com.vidyo.vidyosample/files/";
    logParam.pathToDumpDir = logDirC;
    logParam.pathToConfigDir = pathDirC;
    
    

    LOGI("ApplicationJni_Construct: certifcateFileName=%s, configDir=%s, logDir=%s!\n", certificatesFileNameC, pathDirC, logDirC);


	VidyoBool returnValue = VidyoClientStart(SampleGuiOnOutEvent,
                                             NULL,
                                             &logParam,
											 (VidyoWindowId)(0),
											 &videoRect,
											 NULL,
											 NULL,
                                             NULL);
	if (returnValue)
	{
		LOGI("VidyoClientStart() was a SUCCESS\n");
	}
	else
	{
		//start failed
		LOGE("ApplicationJni_Construct VidyoClientStart() returned error!\n");
	}

	AppCertificateStoreInitialize(logDirC,certificatesFileNameC,NULL);

	FUNCTION_EXIT;
}

JNIEXPORT void Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_Login(JNIEnv* env, jobject javaThis,
		jstring vidyoportalName, jstring userName, jstring passwordName) {

	FUNCTION_ENTRY;
	LOGI("Java_com_vidyo_vidyosample_VidyoSampleApplicationkevin_Login() enter\n");

	const char *portalC = (*env)->GetStringUTFChars(env, vidyoportalName, NULL);
	const char *usernameC = (*env)->GetStringUTFChars(env, userName, NULL);
	const char *passwordC = (*env)->GetStringUTFChars(env, passwordName, NULL);

	LOGI("Starting Login Process\n");
	VidyoClientInEventLogIn event = {0};

	strlcpy(event.portalUri, portalC, sizeof(event.portalUri));
	strlcpy(event.userName, usernameC, sizeof(event.userName));
	strlcpy(event.userPass, passwordC, sizeof(event.userPass));

	LOGI("logging in with portalUri %s user %s ", event.portalUri, event.userName);
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_LOGIN, &event, sizeof(VidyoClientInEventLogIn));
 	FUNCTION_EXIT;
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_Dispose(JNIEnv *env, jobject jObj2)
{
	FUNCTION_ENTRY;
	if (VidyoClientStop())
		LOGI("VidyoClientStop() SUCCESS!!\n");
        
	else
		LOGE("VidyoClientStop() FAILURE!!\n");

	FUNCTION_EXIT;
}


JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *pvt )
{
	FUNCTION_ENTRY;
	LOGI("JNI_OnLoad called\n");
	global_vm = vm;
	FUNCTION_EXIT;
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload( JavaVM *vm, void *pvt )
{
	FUNCTION_ENTRY
	LOGE("JNI_OnUnload called\n");
	FUNCTION_EXIT
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_Render(JNIEnv *env, jobject jObj2)
{
//	FUNCTION_ENTRY;
	doRender();
//	FUNCTION_EXIT;
}


JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_RenderRelease(JNIEnv *env, jobject jObj2)
{
	FUNCTION_ENTRY;
	doSceneReset();
	FUNCTION_EXIT;
}

 void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_Resize(JNIEnv *env, jobject jobj, jint width, jint height)
{
	FUNCTION_ENTRY;
	LOGI("JNI Resize width=%d height=%d\n", width, height);
	x = width;
	y = height;
	doResize( (VidyoUint)width, (VidyoUint)height);
	FUNCTION_EXIT;
}


 /***
  * 发送消息
  */
 void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_SendChat(JNIEnv *env, jobject jobj, jstring emsg)
{
	FUNCTION_ENTRY;
	const char *pmsg = (*env)->GetStringUTFChars(env, emsg, NULL);
		VidyoClientInEventGroupChat event = {0};
		strlcpy(event.message, pmsg, sizeof(event.message));
		LOGI("Send message:%s \n", pmsg);
		VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_GROUP_CHAT, &event, sizeof(VidyoClientInEventGroupChat));
	FUNCTION_EXIT;
}

/**
 * band info
 */
 jstring JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_getBandInfo(JNIEnv *env)
{

	FUNCTION_ENTRY;
	char buff[64]={0};

	VidyoClientRequestBandwidthInfo requestGetBandWidth={0};
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_BANDWIDTH_INFO, &requestGetBandWidth, sizeof(VidyoClientRequestBandwidthInfo));
	LOGI("ActualRecvBwVideo:%d\n", requestGetBandWidth.ActualRecvBwVideo);
	LOGI("ActualSendBwVideo:%d\n", requestGetBandWidth.ActualSendBwVideo);
	sprintf(buff,"%d,%d", requestGetBandWidth.ActualRecvBwVideo, requestGetBandWidth.ActualSendBwVideo);

	jstring js = (*env)->NewStringUTF(env, buff);
	return js;
	FUNCTION_EXIT;
}


JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_TouchEvent(JNIEnv *env, jobject jobj, jint id, jint type, jint x, jint y)
{
	FUNCTION_ENTRY;
	doTouchEvent((VidyoInt)id, (VidyoInt)type, (VidyoInt)x, (VidyoInt)y);
	FUNCTION_EXIT;
}


JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_SetOrientation(JNIEnv *env, jobject jobj,  jint orientation)
{
FUNCTION_ENTRY;

        VidyoClientOrientation newOrientation = VIDYO_CLIENT_ORIENTATION_UP;

        //translate LMI orienation to client orientation
        switch(orientation) {
                case 0: newOrientation = VIDYO_CLIENT_ORIENTATION_UP;
                                LOGI("VIDYO_CLIENT_ORIENTATION_UP");
                                break;
                case 1: newOrientation = VIDYO_CLIENT_ORIENTATION_DOWN;
                        LOGI("VIDYO_CLIENT_ORIENTATION_DOWN");
                        break;
                case 2: newOrientation = VIDYO_CLIENT_ORIENTATION_LEFT;
                        LOGI("VIDYO_CLIENT_ORIENTATION_LEFT");
                        break;
                case 3: newOrientation = VIDYO_CLIENT_ORIENTATION_RIGHT;
                        LOGI("VIDYO_CLIENT_ORIENTATION_RIGHT");
                        break;
        }

        doClientSetOrientation(newOrientation);

FUNCTION_EXIT;
return;
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_SetCameraDevice(JNIEnv *env, jobject jobj, jint camera)
{
	// FUNCTION_ENTRY
	VidyoClientRequestConfiguration requestConfig;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));

	/*
	 * Value of 0 is (currently) used to signify the front camera
	 */
	if (camera == 0)
	{
		requestConfig.currentCamera = 0;
	}
	/*
	 * Value of 1 is (currently) used to signify the back camera
	 */
	else if (camera == 1)
	{
		requestConfig.currentCamera = 1;
	}
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));

        //FUNCTION_EXIT
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_DisableAutoLogin(JNIEnv *env, jobject jobj)
{
	//FUNCTION_ENTRY
	VidyoClientRequestConfiguration requestConfig;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));
	requestConfig.enableAutoLogIn = 0;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));
	//FUNCTION_EXIT
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_SetPreviewModeON(JNIEnv *env, jobject jobj, jboolean pip)
{
	VidyoClientInEventPreview event;
	if (pip)
		event.previewMode = VIDYO_CLIENT_PREVIEW_MODE_DOCK;
	else
		event.previewMode = VIDYO_CLIENT_PREVIEW_MODE_NONE;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_PREVIEW, &event, sizeof(VidyoClientInEventPreview));
}

void _init()
{
	FUNCTION_ENTRY;
	LOGE("_init called\n");
	FUNCTION_EXIT;
}

void _fini()
{
	FUNCTION_ENTRY;
	LOGE("_fini called\n");
	FUNCTION_EXIT;
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_DisableAllVideoStreams(JNIEnv *env, jobject jobj)
{
    if (!allVideoDisabled)
    {
        //this would have the effect of stopping all video streams but self preview
        
        VidyoClientRequestSetBackground reqBackground = {0};
        reqBackground.willBackground = VIDYO_TRUE;
        (void)VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_BACKGROUND,
                                     &reqBackground, sizeof(reqBackground));
        
        allVideoDisabled = VIDYO_TRUE;
    }
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_EnableAllVideoStreams(JNIEnv *env, jobject jobj)
{
	{
		if (allVideoDisabled)
		{
            VidyoClientRequestSetBackground reqBackground = {0};
			reqBackground.willBackground = VIDYO_FALSE;

            (void)VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_BACKGROUND,
                                         &reqBackground, sizeof(reqBackground));
            
			//this would have the effect of enabling all video streams 
			allVideoDisabled = VIDYO_FALSE;
//			rearrangeSceneLayout();
		}	



	}
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_MuteCamera(JNIEnv *env, jobject jobj, jboolean MuteCamera)
{
	VidyoClientInEventMute event;
	event.willMute = MuteCamera;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_MUTE_VIDEO, &event, sizeof(VidyoClientInEventMute));
}


//======================
JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_AutoStartMicrophone(JNIEnv *env, jobject jobj, jboolean MuteCamera)
{
	VidyoClientInEventMute event;
	event.willMute = MuteCamera;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_MUTE_AUDIO_IN, &event, sizeof(VidyoClientInEventMute));
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_AutoStartCamera(JNIEnv *env, jobject jobj, jboolean MuteCamera)
{
	VidyoClientInEventMute event;
	event.willMute = MuteCamera;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_MUTE_VIDEO, &event, sizeof(VidyoClientInEventMute));
}
JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_AutoStartSpeaker(JNIEnv *env, jobject jobj, jboolean MuteCamera)
{
//	VidyoClientInEventMute event;
//	event.willMute = MuteCamera;
//	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_MUTE_AUDIO_OUT, &event, sizeof(VidyoClientInEventMute));
}
//=========================================

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_StartConferenceMedia(JNIEnv *env, jobject jobj)
{
    doStartConferenceMedia();
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_HideToolBar(JNIEnv* env, jobject jobj, jboolean disablebar)
{
	LOGI("Java_com_vidyo_vidyosample_VidyoSampleApplicationkevin_HideToolBar() enter\n");
    VidyoClientInEventEnable event;
    event.willEnable = VIDYO_FALSE;
    VidyoBool ret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_ENABLE_BUTTON_BAR, &event,sizeof(VidyoClientInEventEnable));
    if (!ret)
        LOGW("Java_com_vidyo_vidyosample_VidyoSampleApplicationkevin_HideToolBar() failed!\n");
}

// this function will enable echo cancellation
JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_SetEchoCancellation(JNIEnv *env, jobject jobj, jboolean aecenable)
{
	// get persistent configuration values
	  VidyoClientRequestConfiguration requestConfiguration;

	  VidyoUint ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfiguration,
	                                                                             sizeof(requestConfiguration));
	  if (ret != VIDYO_CLIENT_ERROR_OK) {
	          LOGE("VIDYO_CLIENT_REQUEST_GET_CONFIGURATION returned error!");
	          return;
	  }

	  // modify persistent configuration values, based on current values of on-screen controls
	  if (aecenable) {
	          requestConfiguration.enableEchoCancellation = 1;
	  } else {
	          requestConfiguration.enableEchoCancellation = 0;
	  }

	  // set persistent configuration values
	  ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfiguration,
	                                                           sizeof(requestConfiguration));
	  if (ret != VIDYO_CLIENT_ERROR_OK) {
	          LOGE("VIDYO_CLIENT_REQUEST_SET_CONFIGURATION returned error!");
	  }
	}
JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_SetSpeakerVolume(JNIEnv *env, jobject jobj, jint volume)
{
	//FUNCTION ENTRY
	VidyoClientRequestVolume volumeRequest;
	volumeRequest.volume = volume;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_VOLUME_AUDIO_OUT, &volumeRequest,
		                                                           sizeof(volumeRequest));

	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_VOLUME_AUDIO_IN, &volumeRequest,
			                                                           sizeof(volumeRequest));

	//FUNCTION EXIT
	return;
}

JNIEXPORT void JNICALL Java_com_esoon_vidyosample_VidyoSampleApplicationkevin_DisableShareEvents(JNIEnv *env, jobject javaThisj)
{
	FUNCTION_ENTRY
	VidyoClientSendEvent (VIDYO_CLIENT_IN_EVENT_DISABLE_SHARE_EVENTS, 0, 0);
	LOGI("Disable Shares Called - Vimal");
	FUNCTION_EXIT;
}




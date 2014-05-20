#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <fcntl.h>
#include <errno.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <android/log.h>
#define TAG "Native::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define BUFFER_LENGTH 1024
#define SLEEP_INTERVEL 60*3
#define CFGMNG_TASK_NAME_LEN 1024
#define FILE_PATH_LENGTH 128

int isProcessExist();
void runProcess();
void logSelf(const char * logInfo, ...);
char * pid_to_binary_name(int pid);
int main(int argc, char* argv[]) {
	int i = 0;
	int ret = -1;
	FILE * g_lockfile = NULL;
	if(argc<=1){
		logSelf("argv failed\n");
		return -1;
	}
	char str[FILE_PATH_LENGTH] = {0};
	sprintf(str, "/data/data/%s/tmp.lock", argv[1]);
	// 检查是否已经有一个supervisor进程在运行
	g_lockfile = fopen(str, "a+");
	if (g_lockfile == NULL) {
		logSelf("g_lockfile fopen() failed\n");
		return -1;
	}

	ret = flock(fileno(g_lockfile), LOCK_EX | LOCK_NB);
	if (ret != 0) {
		fprintf(stderr, "flock() failed:%s!\n", strerror(errno));
		logSelf("this program already running\n");
		return -1;
	}

	DIR* dir;
	dir = opendir("/proc");
	struct dirent* de;
	int count = 0;
	while ((de = readdir(dir)) != 0) {
		if (isdigit(de->d_name[0])) {
			int pid = atoi(de->d_name);
			char* name = pid_to_binary_name(pid);
			char str[FILE_PATH_LENGTH] = {0};
			sprintf(str, "/data/data/%s/daemon", argv[1]); //生成要读取的文件的路径

			if (strncmp(name, str, 30) == 0) {
				logSelf(name);
				logSelf(de->d_name);
				count++;
				if (count > 1) {
					LOGE("Error Native::JNI");
				}
			}
		}
	}
	while (1) {
		char str1[FILE_PATH_LENGTH] = {0};
		char str2[FILE_PATH_LENGTH] = {0};
		sprintf(str1, "/data/data/%s", argv[1]);
		sprintf(str2, "/data/data/%s/.lock", argv[1]);

		if (access(str1, 0) != 0 || access(str2, 0) != 0) {
			logSelf("uninstall \n");
			exit(-1);
		}
		if (isProcessExist() == 0) {
			if (argc > 2) {
				runProcess(argv[1],argv[2]);
			} else {
				runProcess(argv[1],NULL);
			}
		}
		sleep(SLEEP_INTERVEL);
	}
	return 0;
}

void logSelf(const char * logInfo, ...) {
	FILE *fp1;
	if ((fp1 = fopen("textc.txt", "a+")) == NULL) {
		printf("open file failed");
		exit(0);
	}
	fprintf(fp1, "Log : %s\n",logInfo);
	fclose(fp1);

}
char * pid_to_binary_name(int pid) {
	char * cmd_f;
	FILE *fp;

	if (pid <= 0)
		return NULL;
	char filepath[256];
	char filetext[50];
	char *cTem = filetext;
	sprintf(filepath, "/proc/%d/cmdline", pid); //生成要读取的文件的路径
	fp = fopen(filepath, "r"); //打开文件
	if (NULL != fp) {
		fread(filetext, 1, 50, fp); //读取文件
		filetext[49] = '\0'; //给读出的内容加上字符串结束符
		fclose(fp);
	}
	return cTem;
}
int isProcessExist() {
	int ret = 0;
	DIR* dir;
	dir = opendir("/proc");
	struct dirent* de;
	while ((de = readdir(dir)) != 0) {
		if (isdigit(de->d_name[0])) {
			int pid = atoi(de->d_name);
			char* name = pid_to_binary_name(pid);
//			logSelf(name);
			if (strncmp(name, "ctrip.push", 10) == 0) {
				logSelf("finded");
				return 1;
			}
		}
	}
	return ret;
}
void runProcess(char * package,char * uid) {
	FILE *fp;
	char commond[BUFFER_LENGTH];
//	sprintf(commond, "am startservice -a ctrip.android.view.push.service");
	if (uid) {
//		execlp("am","am","startservice","--user",uid,"-n","ctrip.android.view/ctrip.android.view.push.PushService");
		sprintf(commond, "am startservice --user %s  -n %s/com.ctrip.push.PushService", uid,
				package);
		logSelf(commond);
	} else {
		sprintf(commond,
				"am startservice -n %s/com.ctrip.push.PushService",package);
	}
	if ((fp = popen(commond, "r")) == NULL) {
		printf("popen failed\n");
		exit(1);
	}

	pclose(fp);
}


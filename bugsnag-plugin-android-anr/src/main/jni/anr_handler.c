#include "anr_handler.h"
#include <errno.h>
#include <pthread.h>
#include <signal.h>
#include <string.h>
#include <android/log.h>
#include <jni.h>

#include "utils/string.h"

static pthread_t bsg_watchdog_thread;
static sigset_t bsg_anr_sigmask;

/* The previous SIGQUIT handler */
struct sigaction bsg_sigquit_sigaction_previous;

void bsg_handle_sigquit(int signum, siginfo_t *info, void *user_context) {
  // Invoke previous handler, if a custom handler has been set
  // This can be conditionally switched off on certain platforms (e.g. Unity) where
  // this behaviour is not desirable due to Unity's implementation failing with a SIGSEGV
  struct sigaction previous = bsg_sigquit_sigaction_previous;
  if (previous.sa_flags & SA_SIGINFO) {
    previous.sa_sigaction(SIGQUIT, info, user_context);
  } else if (previous.sa_handler == SIG_DFL) {
    // Do nothing, the default action is nothing
  } else if (previous.sa_handler != SIG_IGN) {
    void (*previous_handler)(int) = previous.sa_handler;
    previous_handler(signum);
  }
}

/**
 * Configure ANR detection using a signal handler listening for SIGQUIT
 */
void *bsg_monitor_anrs(void *_arg) {
  struct sigaction handler;
  sigemptyset(&handler.sa_mask);
  handler.sa_sigaction = bsg_handle_sigquit;

  // remove SA_ONSTACK flag as we don't require information about the SIGQUIT signal.
  // specifying this flag results in a crash when performing a JNI call. For further context
  // see https://issuetracker.google.com/issues/37035211
  handler.sa_flags = SA_SIGINFO;
  int success = sigaction(SIGQUIT, &handler, &bsg_sigquit_sigaction_previous);
  if (success != 0) {
    BUGSNAG_LOG("Failed to install SIGQUIT handler: %s", strerror(errno));
  }

  return NULL;
}

bool bsg_handler_install_anr(JNIEnv *env, jobject plugin, jboolean callPreviousSigquitHandler) {
  sigemptyset(&bsg_anr_sigmask);
  sigaddset(&bsg_anr_sigmask, SIGQUIT);

  int mask_status = pthread_sigmask(SIG_BLOCK, &bsg_anr_sigmask, NULL);
  if (mask_status != 0) {
    BUGSNAG_LOG("Failed to mask SIGQUIT: %s", strerror(mask_status));
  } else {
    pthread_create(&bsg_watchdog_thread, NULL, bsg_monitor_anrs, NULL);
    // unblock the current thread
    pthread_sigmask(SIG_UNBLOCK, &bsg_anr_sigmask, NULL);
  }
  return true;
}

void bsg_handler_uninstall_anr() {
}

package club.fdawei.datawatcher.api.task;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultTaskExecutor implements ITaskExecutor {

    private Handler mainHandler;
    private ExecutorService executorService;

    public DefaultTaskExecutor() {
        this.mainHandler = new Handler(Looper.getMainLooper());
        int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
        this.executorService = Executors.newScheduledThreadPool(corePoolSize);
    }

    @Override
    public void postToMainThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mainHandler.post(runnable);
    }

    @Override
    public void postToWorkThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        executorService.execute(runnable);
    }
}

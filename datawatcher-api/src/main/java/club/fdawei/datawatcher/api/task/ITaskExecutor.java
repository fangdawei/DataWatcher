package club.fdawei.datawatcher.api.task;

public interface ITaskExecutor {

    void postToMainThread(Runnable runnable);

    void postToWorkThread(Runnable runnable);
}

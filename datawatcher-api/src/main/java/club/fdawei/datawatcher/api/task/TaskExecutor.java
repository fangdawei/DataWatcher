package club.fdawei.datawatcher.api.task;

public class TaskExecutor {

    private static ITaskExecutor taskExecutor;

    public static void setTaskExecutor(ITaskExecutor executor) {
        taskExecutor = executor;
    }

    private static ITaskExecutor getTaskExecutor() {
        if (taskExecutor == null) {
            synchronized (TaskExecutor.class) {
                if (taskExecutor == null) {
                    taskExecutor = new DefaultTaskExecutor();
                }
            }
        }
        return taskExecutor;
    }

    public static void postToMainThread(Runnable runnable) {
        getTaskExecutor().postToMainThread(runnable);
    }

    public static void postToWorkThread(Runnable runnable) {
        getTaskExecutor().postToWorkThread(runnable);
    }
}

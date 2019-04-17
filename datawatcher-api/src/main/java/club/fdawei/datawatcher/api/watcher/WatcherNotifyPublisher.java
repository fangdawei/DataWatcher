package club.fdawei.datawatcher.api.watcher;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.api.task.TaskExecutor;

/**
 * Created by david on 2019/4/4.
 */
public abstract class WatcherNotifyPublisher {

    private int thread = DataWatch.Thread.CURRENT;
    private boolean needNotifyWhenBind = true;

    public void setThread(int thread) {
        this.thread = thread;
    }

    public void setNeedNotifyWhenBind(boolean needNotifyWhenBind) {
        this.needNotifyWhenBind = needNotifyWhenBind;
    }

    public boolean isNeedNotifyWhenBind() {
        return needNotifyWhenBind;
    }

    protected abstract void publish(Object source, Object oldValue, Object newValue);

    public void notifyWatcher(final Object source, final Object oldValue, final Object newValue) {
        if (thread == DataWatch.Thread.MAIN) {
            TaskExecutor.postToMainThread(new Runnable() {
                @Override
                public void run() {
                    publish(source, oldValue, newValue);
                }
            });
        } else if (thread == DataWatch.Thread.WORK_THREAD) {
            TaskExecutor.postToWorkThread(new Runnable() {
                @Override
                public void run() {
                    publish(source, oldValue, newValue);
                }
            });
        } else {
            publish(source, oldValue, newValue);
        }
    }
}

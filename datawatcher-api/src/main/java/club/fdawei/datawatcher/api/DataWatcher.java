package club.fdawei.datawatcher.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import club.fdawei.datawatcher.api.data.IDataBinder;
import club.fdawei.datawatcher.api.data.IDataSource;
import club.fdawei.datawatcher.api.log.ILogger;
import club.fdawei.datawatcher.api.log.Logger;
import club.fdawei.datawatcher.api.task.ITaskExecutor;
import club.fdawei.datawatcher.api.task.TaskExecutor;

public class DataWatcher {

    private static final Set<IDataSource> dataSourceSet =
            Collections.newSetFromMap(new ConcurrentHashMap<IDataSource, Boolean>());

    public static void init(ITaskExecutor taskExecutor) {
        init(taskExecutor, null);
    }

    public static void init(ITaskExecutor taskExecutor, ILogger logger) {
        TaskExecutor.setTaskExecutor(taskExecutor);
        Logger.setLogger(logger);
    }

    public static void bind(Object target, Object source) {
        if (!checkTarget(target) || !checkSource(source)) {
            return;
        }
        IDataSource dataSource = (IDataSource) source;
        IDataBinder dataBinder = dataSource.getDataBinder();
        dataBinder.addWatcher(target);
        dataSourceSet.add(dataSource);
    }

    public static void unbind(Object target, Object source) {
        if (!checkTarget(target) || !checkSource(source)) {
            return;
        }
        IDataSource dataSource = (IDataSource) source;
        IDataBinder dataBinder = dataSource.getDataBinder();
        dataBinder.removeWatcher(target);
        if (!dataBinder.hasWatcher()) {
            dataSourceSet.remove(dataSource);
        }
    }

    public static void unbindAll(Object target) {
        if (!checkTarget(target)) {
            return;
        }
        Iterator<IDataSource> iterator = dataSourceSet.iterator();
        while (iterator.hasNext()) {
            IDataSource dataSource = iterator.next();
            IDataBinder dataBinder = dataSource.getDataBinder();
            dataBinder.removeWatcher(target);
            if (!dataBinder.hasWatcher()) {
                iterator.remove();
            }
        }
    }

    public static void clear() {
        for(IDataSource dataSource : dataSourceSet) {
            dataSource.getDataBinder().clearWatcher();
        }
        dataSourceSet.clear();
    }

    private static boolean checkTarget(Object target) {
        if (target == null) {
            return false;
        }
        return true;
    }

    private static boolean checkSource(Object source) {
        if (source == null) {
            return false;
        }
        if (!(source instanceof IDataSource)) {
            return false;
        }
        if (((IDataSource) source).getDataBinder() == null) {
            return false;
        }
        return true;
    }
}

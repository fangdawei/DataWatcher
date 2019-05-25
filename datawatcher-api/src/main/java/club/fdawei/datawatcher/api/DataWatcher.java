package club.fdawei.datawatcher.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import club.fdawei.datawatcher.api.common.Config;
import club.fdawei.datawatcher.api.data.IDataBinder;
import club.fdawei.datawatcher.api.data.IDataSource;
import club.fdawei.datawatcher.api.log.ILogger;
import club.fdawei.datawatcher.api.log.Logger;
import club.fdawei.datawatcher.api.task.ITaskExecutor;
import club.fdawei.datawatcher.api.task.TaskExecutor;
import club.fdawei.datawatcher.api.watcher.IWatcherProxy;
import club.fdawei.datawatcher.api.watcher.IWatcherTarget;

public class DataWatcher {

    private static Config sConfig = new Config();
    private static final Set<IDataSource> sDataSourceSet =
            Collections.newSetFromMap(new ConcurrentHashMap<IDataSource, Boolean>());

    public static void init(ITaskExecutor taskExecutor) {
        init(taskExecutor, null);
    }

    public static void init(ITaskExecutor taskExecutor, ILogger logger) {
        TaskExecutor.setTaskExecutor(taskExecutor);
        Logger.setLogger(logger);
    }

    public static void bind(Object target, Object source) {
        if (targetIllegal(target) || sourceIllegal(source)) {
            return;
        }
        IDataSource dataSource = (IDataSource) source;
        IDataBinder dataBinder = dataSource.getDataBinder();
        IWatcherTarget watcherTarget = (IWatcherTarget) target;
        IWatcherProxy watcherProxy = watcherTarget.getWatcherProxy();
        dataBinder.addWatcher(watcherProxy);
        sDataSourceSet.add(dataSource);
    }

    public static void unbind(Object target, Object source) {
        if (targetIllegal(target) || sourceIllegal(source)) {
            return;
        }
        IDataSource dataSource = (IDataSource) source;
        IDataBinder dataBinder = dataSource.getDataBinder();
        IWatcherTarget watcherTarget = (IWatcherTarget) target;
        IWatcherProxy watcherProxy = watcherTarget.getWatcherProxy();
        dataBinder.removeWatcher(watcherProxy);
        if (!dataBinder.hasWatcher()) {
            sDataSourceSet.remove(dataSource);
        }
    }

    public static void unbindAll(Object target) {
        if (targetIllegal(target)) {
            return;
        }
        Iterator<IDataSource> iterator = sDataSourceSet.iterator();
        while (iterator.hasNext()) {
            IDataBinder dataBinder = iterator.next().getDataBinder();
            IWatcherTarget watcherTarget = (IWatcherTarget) target;
            IWatcherProxy watcherProxy = watcherTarget.getWatcherProxy();
            dataBinder.removeWatcher(watcherProxy);
            if (!dataBinder.hasWatcher()) {
                iterator.remove();
            }
        }
    }

    public static void clear() {
        for(IDataSource dataSource : sDataSourceSet) {
            dataSource.getDataBinder().clearWatcher();
        }
        sDataSourceSet.clear();
    }

    public static void setConfig(Config config) {
        if (config == null) {
            sConfig = new Config();
        } else {
            sConfig = config;
        }
    }

    public static Config getConfig() {
        return sConfig;
    }

    private static boolean targetIllegal(Object target) {
        if (!(target instanceof IWatcherTarget)) {
            return true;
        }
        if (((IWatcherTarget) target).getWatcherProxy() == null) {
            return true;
        }
        return false;
    }

    private static boolean sourceIllegal(Object source) {
        if (!(source instanceof IDataSource)) {
            return true;
        }
        if (((IDataSource) source).getDataBinder() == null) {
            return true;
        }
        return false;
    }
}

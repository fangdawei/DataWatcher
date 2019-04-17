package club.fdawei.datawatcher.api.watcher;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import club.fdawei.datawatcher.api.common.GenClassInfoDef;
import club.fdawei.datawatcher.api.log.Logger;

public class WatcherProxyFactory {

    private static final String TAG = "WatcherProxyFactory";

    private static final Map<Class<?>, IWatcherProxyCreator> watcherProxyCreatorMap = new ConcurrentHashMap<>();

    public static @Nullable IWatcherProxy createWatcherProxy(@NonNull Object target) {
        IWatcherProxyCreator creator = getWatcherProxyCreator(target);
        if (creator == null) {
            return null;
        }
        return creator.createWatcherProxy(target);
    }

    public static IWatcherProxyCreator getWatcherProxyCreator(@NonNull Object target) {
        final Class<?> targetClass = target.getClass();
        IWatcherProxyCreator creator = watcherProxyCreatorMap.get(targetClass);
        if (creator == null) {
            synchronized (WatcherProxyFactory.class) {
                creator = watcherProxyCreatorMap.get(targetClass);
                if (creator == null) {
                    final String creatorClassName = targetClass.getName() + GenClassInfoDef.WatcherProxyCreator.NAME_SUFFIX;
                    try {
                        creator = (IWatcherProxyCreator) Class.forName(creatorClassName).newInstance();
                    } catch (Exception e) {
                        Logger.e(TAG, "getWatcherProxyCreator error, %s", e.getMessage());
                    }
                    if (creator != null) {
                        watcherProxyCreatorMap.put(target.getClass(), creator);
                    }
                }
            }
        }
        return creator;
    }
}

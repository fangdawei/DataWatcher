package club.fdawei.datawatcher.api.watcher;

import android.support.annotation.Nullable;

/**
 * Created by david on 2019/4/4.
 */
public interface IWatcherProxyCreator<PROXY extends IWatcherProxy> {

    @Nullable
    PROXY createWatcherProxy(Object target);
}

package club.fdawei.datawatcher.api.watcher;

/**
 * Created by david on 2019/4/4.
 */
public interface IWatcherProxyCreator<PROXY extends IWatcherProxy, TARGET> {
    PROXY createWatcherProxy(TARGET target);
}

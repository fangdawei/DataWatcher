package club.fdawei.datawatcher.api.data;

import java.util.Map;

public interface IDataSource {
    IDataBinder getDataBinder();

    Map<String, Object> getAllFieldValue();
}

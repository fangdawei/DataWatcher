package club.fdawei.datawatcher.perfectprocessor.source;

import javax.lang.model.element.TypeElement;

import club.fdawei.datawatcher.perfectprocessor.common.AnnoWithClassInfo;

/**
 * Created by david on 2019/4/16.
 */
public class DataSourceClassInfo extends AnnoWithClassInfo {

    private String pkgName;
    private TypeElement typeElement;

    public DataSourceClassInfo(String pkgName, TypeElement typeElement) {
        this.pkgName = pkgName;
        this.typeElement = typeElement;
    }
}

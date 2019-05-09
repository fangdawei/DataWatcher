package club.fdawei.datawatcher.perfectprocessor.source;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.TypeElement;

import club.fdawei.datawatcher.perfectprocessor.common.JavaClassHandler;

/**
 * Create by david on 2019/05/08.
 */
public class DataSourceInjector extends JavaClassHandler {

    private List<DataSourceClassInfo> dataSourceList = new LinkedList<>();

    public void addTypeWithDataSource(TypeElement typeElement) {
        if (typeElement == null) {
            return;
        }
        String pkgName = getUtilProvider().getElementsUtils().getPackageOf(typeElement).toString();
        DataSourceClassInfo classInfo = new DataSourceClassInfo(pkgName, typeElement);
        dataSourceList.add(classInfo);
    }

    public void clear() {
        dataSourceList.clear();
    }

    public void injectClass() {

    }
}

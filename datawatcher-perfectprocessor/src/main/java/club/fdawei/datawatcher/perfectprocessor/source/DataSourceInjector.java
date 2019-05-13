package club.fdawei.datawatcher.perfectprocessor.source;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.TypeElement;

import club.fdawei.datawatcher.perfectprocessor.common.CommonTag;
import club.fdawei.datawatcher.perfectprocessor.common.JavaClassHandler;

/**
 * Create by david on 2019/05/08.
 */
public class DataSourceInjector extends JavaClassHandler {

    private static final String TAG = CommonTag.TAG;

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

    public void inject() {
        for (DataSourceClassInfo dataSourceClassInfo : dataSourceList) {
            InjectHandler.builder(dataSourceClassInfo)
                    .trees(getUtilProvider().getTrees())
                    .names(getUtilProvider().getNames())
                    .treeMaker(getUtilProvider().getTreeMaker())
                    .logger(this)
                    .build()
                    .inject();
        }
    }
}

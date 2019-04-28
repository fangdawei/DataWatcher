package club.fdawei.datawatcher.processor.source;


import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;

import club.fdawei.datawatcher.processor.common.CommonTag;
import club.fdawei.datawatcher.processor.common.JavaClassGenerator;

/**
 * Created by david on 2019/4/16.
 */
public class DataFieldsGenerator extends JavaClassGenerator {

    private static final String TAG = CommonTag.TAG;

    private Map<String, DataSourceClassInfo> rootDataSourceMap = new LinkedHashMap<>();

    public void clear() {
        rootDataSourceMap.clear();
    }

    public void addTypeWithDataSource(TypeElement typeElement) {
        if (typeElement == null) {
            return;
        }
        String pkgName = getUtilBox().getElementsUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String qualifiedName = typeElement.getQualifiedName().toString();
        String nameWithoutPkg = qualifiedName.replace(pkgName + ".", "");
        String[] simpleNamePath = nameWithoutPkg.split("\\.");
        if (simpleNamePath.length == 0) {
            return;
        }
        String rootSimpleName = simpleNamePath[0];
        String rootName = pkgName + "." + rootSimpleName;
        DataSourceClassInfo rootDataSource = rootDataSourceMap.get(rootName);
        if (rootDataSource == null) {
            rootDataSource = new DataSourceClassInfo(pkgName, rootSimpleName);
            rootDataSource.setTopClass(true);
            rootDataSourceMap.put(rootName, rootDataSource);
        }
        DataSourceClassInfo dataSource = rootDataSource;
        for (int i = 1; i < simpleNamePath.length; i++) {
            String innerSimpleName = simpleNamePath[i];
            DataSourceClassInfo innerClass = dataSource.getInnerClass(innerSimpleName);
            if (innerClass == null) {
                innerClass = new DataSourceClassInfo(pkgName, innerSimpleName);
                innerClass.setTopClass(false);
                dataSource.addInnerClass(innerClass);
            }
            dataSource = innerClass;
        }
        dataSource.setTypeElement(typeElement);
    }

    @Override
    public void genJavaFile(Filer filer) {
        for (DataSourceClassInfo dataSourceClass : rootDataSourceMap.values()) {
            String pkgName = dataSourceClass.getPkgName();
            String classSimpleName = dataSourceClass.getSimpleName();
            JavaFile javaFile = JavaFile.builder(dataSourceClass.getPkgName(), dataSourceClass.buildTypeSpec())
                    .addFileComment("Generated automatically. Do not modify!")
                    .build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                loge(TAG, "gen error %s.%s, %s", pkgName, classSimpleName, e.getMessage());
            }
        }
    }
}

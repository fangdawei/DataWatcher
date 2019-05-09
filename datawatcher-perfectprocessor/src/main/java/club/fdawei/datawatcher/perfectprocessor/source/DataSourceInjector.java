package club.fdawei.datawatcher.perfectprocessor.source;

import com.sun.source.tree.ClassTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.TypeElement;

import club.fdawei.datawatcher.perfectprocessor.common.ClassInfoBox;
import club.fdawei.datawatcher.perfectprocessor.common.CommonTag;
import club.fdawei.datawatcher.perfectprocessor.common.JavaClassHandler;
import club.fdawei.datawatcher.perfectprocessor.common.TypeBox;

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

    public void injectAll() {
        for(DataSourceClassInfo dataSourceClassInfo : dataSourceList) {
            injectEach(dataSourceClassInfo);
        }
    }

    private void injectEach(DataSourceClassInfo dataSource) {
        JCTree.JCClassDecl dataSourceDecl =
                (JCTree.JCClassDecl) getUtilProvider().getTrees().getTree(dataSource.getTypeElement());
        logw(TAG, "" + dataSourceDecl.toString());
        addIDataBinderImplement(dataSourceDecl);
        addMethodGetDataBinder(dataSourceDecl);
        logw(TAG, "" + dataSourceDecl.toString());
    }

    private void addIDataBinderImplement(JCTree.JCClassDecl dataSourceDecl) {
        TreeMaker treeMaker = getUtilProvider().getTreeMaker();
        Names names = getUtilProvider().getNames();
        com.sun.tools.javac.util.List<JCTree.JCExpression> impls;
        JCTree.JCExpression expr = treeMaker.Ident(names.fromString(TypeBox.I_DATA_SOURCE.toString()));
        if (dataSourceDecl.implementing == null) {
            impls = com.sun.tools.javac.util.List.of(expr);
        } else {
            impls = dataSourceDecl.implementing.append(expr);
        }
        dataSourceDecl.implementing = impls;
    }

    private void addFieldDataBinder(JCTree.JCClassDecl dataSourceDecl) {

    }

    private void addMethodGetDataBinder(JCTree.JCClassDecl dataSourceDecl) {
        TreeMaker treeMaker = getUtilProvider().getTreeMaker();
        Names names = getUtilProvider().getNames();
        JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString("getDataBinder"),
                treeMaker.Ident(names.fromString(TypeBox.I_DATA_BINDER.toString())),
                com.sun.tools.javac.util.List.nil(),
                com.sun.tools.javac.util.List.nil(),
                com.sun.tools.javac.util.List.nil(),
                treeMaker.Block(0, com.sun.tools.javac.util.List.nil()),
                null
                );
        dataSourceDecl.defs = dataSourceDecl.defs.append(methodDecl);
    }
}

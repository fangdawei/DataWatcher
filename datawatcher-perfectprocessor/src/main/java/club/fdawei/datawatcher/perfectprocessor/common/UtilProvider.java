package club.fdawei.datawatcher.perfectprocessor.common;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by david on 2019/04/28.
 */
public interface UtilProvider {

    Elements getElementsUtils();

    Types getTypeUtils();

    Trees getTrees();

    TreeMaker getTreeMaker();

    Names getNames();
}

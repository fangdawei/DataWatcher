package club.fdawei.datawatcher.perfectprocessor.source;

import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import club.fdawei.datawatcher.perfectprocessor.common.CommonTag;
import club.fdawei.datawatcher.perfectprocessor.common.TypeBox;
import club.fdawei.datawatcher.perfectprocessor.log.ILogger;

/**
 * Create by david on 2019/05/11.
 */
public class InjectHandler {

    private static final String TAG = CommonTag.TAG;

    private static final String FIELD_DATABINDER_NAME = "_dataBinder";

    private DataSourceClassInfo dataSource;

    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;
    private ILogger logger;

    private JCTree.JCCompilationUnit compilationUnit;
    private JCTree.JCClassDecl dataSourceDecl;

    private InjectHandler(DataSourceClassInfo dataSource) {
        this.dataSource = dataSource;
    }

    public static Builder builder(DataSourceClassInfo dataSource) {
        return new Builder(dataSource);
    }

    public static class Builder {

        private InjectHandler handler;

        private Builder(DataSourceClassInfo dataSource) {
            this.handler = new InjectHandler(dataSource);
        }

        public Builder trees(Trees trees) {
            this.handler.trees = trees;
            return this;
        }

        public Builder names(Names names) {
            this.handler.names = names;
            return this;
        }

        public Builder treeMaker(TreeMaker treeMaker) {
            this.handler.treeMaker = treeMaker;
            return this;
        }

        public Builder logger(ILogger logger) {
            this.handler.logger = logger;
            return this;
        }

        public InjectHandler build() {
            return handler;
        }
    }

    private boolean checkValid() {
        if (dataSource == null) {
            return false;
        }
        if (trees == null || names == null || treeMaker == null) {
            return false;
        }
        return true;
    }

    public void inject() {
        if (!checkValid()) {
            return;
        }
        dataSourceDecl = (JCTree.JCClassDecl) trees.getTree(dataSource.getTypeElement());
        compilationUnit = (JCTree.JCCompilationUnit) trees.getPath(dataSource.getTypeElement()).getCompilationUnit();
        addImports();
        addIDataBinderImplement();
        addFieldDataBinder();
        addMethodGetDataBinder();
        addMethodGetAllFieldValue();
        hookAllSetter();
    }

    private void addImports() {
        ListBuffer<JCTree> importsBuilder = new ListBuffer<>();
        importsBuilder.append(treeMaker.Import(
                treeMaker.Select(
                        _ident(TypeBox.I_DATA_SOURCE.packageName()),
                        names.fromString(TypeBox.I_DATA_SOURCE.simpleName())
                ),
                false
        ));
        importsBuilder.append(treeMaker.Import(
                treeMaker.Select(
                        _ident(TypeBox.I_DATA_BINDER.packageName()),
                        names.fromString(TypeBox.I_DATA_BINDER.simpleName())
                ),
                false
        ));
        importsBuilder.append(treeMaker.Import(
                treeMaker.Select(
                        _ident(TypeBox.DATA_BINDER.packageName()),
                        names.fromString(TypeBox.DATA_BINDER.simpleName())
                ),
                false
        ));
        importsBuilder.append(treeMaker.Import(
                treeMaker.Select(
                        _ident("java.util"),
                        names.fromString("Map")
                ),
                false
        ));
        importsBuilder.append(treeMaker.Import(
                treeMaker.Select(
                        _ident("java.util"),
                        names.fromString("LinkedHashMap")
                ),
                false
        ));
        compilationUnit.defs = compilationUnit.defs.prependList(importsBuilder.toList());
    }

    private void addIDataBinderImplement() {
        JCTree.JCExpression impl = _ident(TypeBox.I_DATA_SOURCE.simpleName());
        if (dataSourceDecl.implementing == null) {
            dataSourceDecl.implementing = List.of(impl);
        } else {
            dataSourceDecl.implementing = dataSourceDecl.implementing.append(impl);
        }
    }

    private void addFieldDataBinder() {
        JCTree.JCVariableDecl fieldDecl = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PRIVATE),
                names.fromString(FIELD_DATABINDER_NAME),
                _ident(TypeBox.I_DATA_BINDER.simpleName()),
                treeMaker.NewClass(
                        null,
                        null,
                        _ident(TypeBox.DATA_BINDER.simpleName()),
                        List.of(_ident("this")),
                        null
                )
        );
        dataSourceDecl.defs = dataSourceDecl.defs.prepend(fieldDecl);
    }

    private void addMethodGetDataBinder() {
        JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString("getDataBinder"),
                _ident(TypeBox.I_DATA_BINDER.simpleName()),
                List.nil(),
                List.nil(),
                List.nil(),
                treeMaker.Block(0, List.of(treeMaker.Return(
                        treeMaker.Select(
                                _ident("this"),
                                names.fromString(FIELD_DATABINDER_NAME)
                        )
                ))),
                null
        );
        dataSourceDecl.defs = dataSourceDecl.defs.append(methodDecl);
    }

    private void addMethodGetAllFieldValue() {
        ListBuffer<JCTree.JCStatement> blockBuilder = new ListBuffer<>();
        blockBuilder.append(treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("valueMap"),
                _typeApply("Map", "String", "Object"),
                treeMaker.NewClass(
                        null,
                        List.nil(),
                        _typeApply("LinkedHashMap", "String", "Object"),
                        List.nil(),
                        null
                )
        ));
        for (VariableElement varElement : dataSource.getDataFields()) {
            String fieldName = varElement.getSimpleName().toString();
            blockBuilder.append(_execApply(
                    treeMaker.Select(_ident("valueMap"), names.fromString("put")),
                    List.of(
                            treeMaker.Literal(fieldName),
                            treeMaker.Select(_ident("this"), names.fromString(fieldName))
                    )
            ));
        }
        blockBuilder.append(treeMaker.Return(_ident("valueMap")));
        JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString("getAllFieldValue"),
                _typeApply("Map", "String", "Object"),
                List.nil(),
                List.nil(),
                List.nil(),
                treeMaker.Block(0, blockBuilder.toList()),
                null
        );
        dataSourceDecl.defs = dataSourceDecl.defs.append(methodDecl);
    }

    private void hookAllSetter() {
        final Map<String, JCTree.JCVariableDecl> fieldMap = new HashMap<>();
        for (VariableElement varElement : dataSource.getDataFields()) {
            JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) trees.getTree(varElement);
            fieldMap.put(varElement.getSimpleName().toString(), variableDecl);
        }
        for (ExecutableElement execElement : dataSource.getPossibleSetterMethods()) {
            JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) trees.getTree(execElement);
            ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
            for (JCTree.JCStatement statement : methodDecl.body.stats) {
                boolean isFieldAssign = false;
                JCTree.JCVariableDecl variableDecl = null;
                if (statement.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                    JCTree.JCExpressionStatement exprStatement = (JCTree.JCExpressionStatement) statement;
                    if (exprStatement.expr.getKind() == Tree.Kind.ASSIGNMENT) {
                        JCTree.JCAssign assign = (JCTree.JCAssign) exprStatement.expr;
                        if (assign.lhs.getKind() == Tree.Kind.MEMBER_SELECT) {
                            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) assign.lhs;
                            if (fieldAccess.selected.getKind() == Tree.Kind.IDENTIFIER) {
                                JCTree.JCIdent ident = (JCTree.JCIdent) fieldAccess.selected;
                                if (ident.toString().equals("this") &&
                                        fieldMap.containsKey(fieldAccess.name.toString())) {
                                    isFieldAssign = true;
                                    variableDecl = fieldMap.get(fieldAccess.name.toString());
                                }
                            }
                        }
                    }
                }
                if (isFieldAssign) {
                    listBuffer.append(treeMaker.VarDef(
                            treeMaker.Modifiers(0),
                            names.fromString("_oldValue"),
                            variableDecl.vartype,
                            treeMaker.Select(_ident("this"), variableDecl.name)
                    ));
                    listBuffer.append(statement);
                    listBuffer.append(_execApply(
                            treeMaker.Select(
                                    treeMaker.Select(
                                            _ident("this"),
                                            names.fromString(FIELD_DATABINDER_NAME)
                                    ),
                                    names.fromString("onDataChanged")
                            ),
                            List.of(
                                    treeMaker.Literal(variableDecl.name.toString()),
                                    _ident("_oldValue"),
                                    treeMaker.Select(_ident("this"), variableDecl.name)
                            )
                    ));
                } else {
                    listBuffer.append(statement);
                }
                methodDecl.body.stats = listBuffer.toList();
            }
        }
    }

    private JCTree.JCIdent _ident(String name) {
        return treeMaker.Ident(names.fromString(name));
    }

    private JCTree.JCTypeApply _typeApply(String type, String... params) {
        ListBuffer<JCTree.JCExpression> listBuilder = new ListBuffer<>();
        for (String param : params) {
            listBuilder.append(_ident(param));
        }
        return treeMaker.TypeApply(_ident(type), listBuilder.toList());
    }

    private JCTree.JCExpressionStatement _execApply(JCTree.JCExpression fn, List<JCTree.JCExpression> args) {
        return treeMaker.Exec(treeMaker.Apply(List.nil(), fn, args));
    }
}

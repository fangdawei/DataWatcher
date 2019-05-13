package club.fdawei.datawatcher.perfectprocessor;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import club.fdawei.datawatcher.annotation.DataSource;
import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.annotation.WatchInherit;
import club.fdawei.datawatcher.perfectprocessor.common.CommonTag;
import club.fdawei.datawatcher.perfectprocessor.common.UtilProvider;
import club.fdawei.datawatcher.perfectprocessor.log.Logger;
import club.fdawei.datawatcher.perfectprocessor.source.DataSourceInjector;
import club.fdawei.datawatcher.perfectprocessor.watcher.WatcherProxyGenerator;

@AutoService(Processor.class)
public class DataWatcherPerfectProcessor extends AbstractProcessor {

    private static final String TAG = CommonTag.TAG;

    private Filer mFiler;
    private Elements mElementUtils;
    private Types mTypeUtils;
    private UtilProvider mUtilProvider;
    private Logger mLogger;
    private Trees mTrees;
    private TreeMaker mTreeMaker;
    private Names mNames;

    private DataSourceInjector dataSourceInjector = new DataSourceInjector();
    private WatcherProxyGenerator watcherProxyGenerator = new WatcherProxyGenerator();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mLogger = new Logger(processingEnvironment.getMessager());
        mTrees = Trees.instance(processingEnvironment);
        Context context = ((JavacProcessingEnvironment) processingEnvironment).getContext();
        mTreeMaker = TreeMaker.instance(context);
        mNames = Names.instance(context);
        mUtilProvider = new UtilProvider() {
            @Override
            public Elements getElementsUtils() {
                return mElementUtils;
            }

            @Override
            public Types getTypeUtils() {
                return mTypeUtils;
            }

            @Override
            public Trees getTrees() {
                return mTrees;
            }

            @Override
            public TreeMaker getTreeMaker() {
                return mTreeMaker;
            }

            @Override
            public Names getNames() {
                return mNames;
            }
        };
        dataSourceInjector.setLogger(mLogger);
        dataSourceInjector.setUtilProvider(mUtilProvider);
        watcherProxyGenerator.setLogger(mLogger);
        watcherProxyGenerator.setUtilProvider(mUtilProvider);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(DataSource.class.getCanonicalName());
        types.add(DataWatch.class.getCanonicalName());
        types.add(WatchInherit.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        collectDataSource(roundEnvironment);
        dataSourceInjector.inject();
        dataSourceInjector.clear();

        collectDataWatch(roundEnvironment);
        collectWatchInherit(roundEnvironment);
        watcherProxyGenerator.genJavaFile(mFiler);
        watcherProxyGenerator.clear();

        return true;
    }

    private void collectDataSource(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(DataSource.class);
        if (elements == null) {
            return;
        }
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                mLogger.logw(TAG, "Only class can be annotated with @%s", DataSource.class.getSimpleName());
                continue;
            }
            TypeElement typeElement = (TypeElement) element;
            dataSourceInjector.addTypeWithDataSource(typeElement);
        }
    }

    private void collectDataWatch(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(DataWatch.class);
        if (elements == null) {
            return;
        }
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) {
                mLogger.logw(TAG, "Only method can be annotated with @%s", DataWatch.class.getSimpleName());
                continue;
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            watcherProxyGenerator.addExecutableWithDataWatch(executableElement);
        }
    }

    private void collectWatchInherit(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(WatchInherit.class);
        if (elements == null) {
            return;
        }
        for(Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                mLogger.logw(TAG, "Only class can be annotated with @%s", WatchInherit.class.getSimpleName());
                continue;
            }
            TypeElement typeElement = (TypeElement) element;
            watcherProxyGenerator.addTypeWithWatchInherit(typeElement);
        }
    }
}

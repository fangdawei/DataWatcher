package club.fdawei.datawatcher.processor;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;

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
import club.fdawei.datawatcher.annotation.WatchData;
import club.fdawei.datawatcher.annotation.WatchInherit;
import club.fdawei.datawatcher.processor.common.CommonTag;
import club.fdawei.datawatcher.processor.common.UtilProvider;
import club.fdawei.datawatcher.processor.log.Logger;
import club.fdawei.datawatcher.processor.source.DataFieldsGenerator;
import club.fdawei.datawatcher.processor.watcher.WatcherProxyGenerator;

@AutoService(Processor.class)
public class DataWatcherProcessor extends AbstractProcessor {

    private static final String TAG = CommonTag.TAG;

    private Filer mFiler;
    private Elements mElementUtils;
    private Types mTypeUtils;
    private UtilProvider mUtilProvider;
    private Logger mLogger;
    private Trees mTrees;

    private DataFieldsGenerator dataFieldsGenerator = new DataFieldsGenerator();
    private WatcherProxyGenerator watcherProxyGenerator = new WatcherProxyGenerator();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mLogger = new Logger(processingEnvironment.getMessager());
        mTrees = Trees.instance(processingEnvironment);
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
        };
        dataFieldsGenerator.setLogger(mLogger);
        dataFieldsGenerator.setUtilProvider(mUtilProvider);
        watcherProxyGenerator.setLogger(mLogger);
        watcherProxyGenerator.setUtilProvider(mUtilProvider);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(DataSource.class.getCanonicalName());
        types.add(WatchData.class.getCanonicalName());
        types.add(WatchInherit.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mLogger.logi(TAG, "DataWatcherProcessor process start");
        collectDataSource(roundEnvironment);
        dataFieldsGenerator.genJavaFile(mFiler);
        dataFieldsGenerator.clear();

        collectWatchData(roundEnvironment);
        collectWatchInherit(roundEnvironment);
        watcherProxyGenerator.genJavaFile(mFiler);
        watcherProxyGenerator.clear();
        mLogger.logi(TAG, "DataWatcherProcessor process end");

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
            dataFieldsGenerator.addTypeWithDataSource(typeElement);
        }
    }

    private void collectWatchData(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(WatchData.class);
        if (elements == null) {
            return;
        }
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) {
                mLogger.logw(TAG, "Only method can be annotated with @%s", WatchData.class.getSimpleName());
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

package club.fdawei.datawatcher.processor;

import club.fdawei.datawatcher.annotation.DataSource;
import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.annotation.InheritWatch;
import club.fdawei.datawatcher.processor.common.CommonTag;
import club.fdawei.datawatcher.processor.common.IUtilBox;
import club.fdawei.datawatcher.processor.log.Logger;
import club.fdawei.datawatcher.processor.source.DataFieldsGenerator;
import club.fdawei.datawatcher.processor.watcher.WatcherProxyGenerator;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.LinkedHashSet;
import java.util.Set;

@AutoService(Processor.class)
public class DataWatcherProcessor extends AbstractProcessor {

    private static final String TAG = CommonTag.TAG;

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;
    private Types mTypeUtils;
    private IUtilBox mUtilBox;
    private Logger mLogger;

    private DataFieldsGenerator dataFieldsGenerator = new DataFieldsGenerator();
    private WatcherProxyGenerator watcherProxyGenerator = new WatcherProxyGenerator();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mLogger = new Logger(mMessager);
        mUtilBox = new IUtilBox() {
            @Override
            public Elements getElementsUtils() {
                return mElementUtils;
            }

            @Override
            public Types getTypeUtils() {
                return mTypeUtils;
            }
        };
        dataFieldsGenerator.setLogger(mLogger);
        dataFieldsGenerator.setUtilBox(mUtilBox);
        watcherProxyGenerator.setLogger(mLogger);
        watcherProxyGenerator.setUtilBox(mUtilBox);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(DataSource.class.getCanonicalName());
        types.add(DataWatch.class.getCanonicalName());
        types.add(InheritWatch.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        collectDataSource(roundEnvironment);
        dataFieldsGenerator.genJavaFile(mFiler);
        dataFieldsGenerator.clear();

        collectDataWatch(roundEnvironment);
        collectDataWatcher(roundEnvironment);
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
            dataFieldsGenerator.addTypeWithDataSource(typeElement);
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

    private void collectDataWatcher(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(InheritWatch.class);
        if (elements == null) {
            return;
        }
        for(Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                mLogger.logw(TAG, "Only class can be annotated with @%s", InheritWatch.class.getSimpleName());
                continue;
            }
            TypeElement typeElement = (TypeElement) element;
            watcherProxyGenerator.addTypeWithInheritWatch(typeElement);
        }
    }
}

package club.fdawei.datawatcher.processor;

import club.fdawei.datawatcher.annotation.DataSource;
import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.processor.common.CommonTag;
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
        dataFieldsGenerator.setTypeUtils(mTypeUtils);
        dataFieldsGenerator.setLogger(mLogger);
        watcherProxyGenerator.setTypeUtils(mTypeUtils);
        watcherProxyGenerator.setLogger(mLogger);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(DataSource.class.getCanonicalName());
        types.add(DataWatch.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        dataFieldsGenerator.clear();
        collectDataSource(roundEnvironment);
        dataFieldsGenerator.genJavaFile(mFiler);
        watcherProxyGenerator.clear();
        collectDataWatch(roundEnvironment);
        watcherProxyGenerator.genJavaFile(mFiler);
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
            String pkgName = getPkgName(typeElement);
            dataFieldsGenerator.addDataSource(typeElement, pkgName);
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
            watcherProxyGenerator.addExecutableElement(executableElement, getPkgName(executableElement));
        }
    }

    private String getPkgName(Element element) {
        return mElementUtils.getPackageOf(element).getQualifiedName().toString();
    }
}

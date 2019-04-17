package club.fdawei.datawatcher.processor;

import club.fdawei.datawatcher.annotation.DataSource;
import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.processor.log.Logger;
import club.fdawei.datawatcher.processor.source.DataFieldsGenerator;
import club.fdawei.datawatcher.processor.watcher.WatcherProxyGenerator;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class DataWatcherProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;
    private Types mTypeUtils;
    private Logger mLogger;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mLogger = new Logger(mMessager);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
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
        processDataSource(roundEnvironment);
        processDataWatch(roundEnvironment);
        return true;
    }

    private void processDataSource(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(DataSource.class);
        if (elements == null) {
            return;
        }
        DataFieldsGenerator generator = new DataFieldsGenerator();
        generator.setLogger(mLogger);
        for (Element element : elements) {
            if (!(element instanceof TypeElement)) {
                continue;
            }
            TypeElement typeElement = (TypeElement) element;
            String pkgName = getPkgName(typeElement);
            generator.addDataSource(typeElement, pkgName);
        }
        generator.genJavaFile(mFiler);
    }

    private void processDataWatch(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(DataWatch.class);
        if (elements == null) {
            return;
        }
        WatcherProxyGenerator generator = new WatcherProxyGenerator();
        generator.setLogger(mLogger);
        for (Element element : elements) {
            if (!(element instanceof ExecutableElement)) {
                return;
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            generator.addExecutableElement(executableElement, getPkgName(executableElement));
        }
        generator.genJavaFile(mFiler);
    }

    private String getPkgName(Element element) {
        return mElementUtils.getPackageOf(element).getQualifiedName().toString();
    }
}

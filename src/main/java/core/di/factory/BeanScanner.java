package core.di.factory;

import com.google.common.collect.Sets;
import core.annotation.Component;
import core.annotation.WebApplication;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.util.Arrays;
import java.util.Set;

public class BeanScanner {

    public static final Class<WebApplication> WEB_APPLICATION_CLASS = WebApplication.class;

    private Object[] basePackages;
    private Reflections reflections;

    public void initialize() {
        Reflections allReflections = new Reflections("");
        this.basePackages = allReflections.getTypesAnnotatedWith(WEB_APPLICATION_CLASS)
                .stream()
                .map(this::findBasePackages)
                .flatMap(Arrays::stream)
                .toArray();

        if (basePackages.length == 0) {
            throw new IllegalStateException("Base package is not initialized");
        }
    }

    private String[] findBasePackages(Class<?> clazz) {
        final String[] basePackages = clazz.getAnnotation(WEB_APPLICATION_CLASS).basePackages();

        return basePackages.length == 0 ? new String[]{clazz.getPackage().getName()} : basePackages;
    }

    public Set<Class<?>> scan() {
        this.reflections = new Reflections(basePackages, new TypeAnnotationsScanner(), new SubTypesScanner(), new MethodAnnotationsScanner());

        return getTypeAnnotatedWith(Component.class);
    }

    @SafeVarargs
    private final Set<Class<?>> getTypeAnnotatedWith(Class<Component>... annotations) {
        Set<Class<?>> beans = Sets.newHashSet();
        for (Class<Component> annotation : annotations) {
            beans.addAll(reflections.getTypesAnnotatedWith(annotation));
        }
        return beans;
    }
}

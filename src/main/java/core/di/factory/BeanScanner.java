package core.di.factory;

import com.google.common.collect.Sets;
import core.annotation.Component;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.util.Set;

public class BeanScanner {

    private Reflections reflections;
    private final Object[] basePackages;

    public BeanScanner(Object[] basePackages) {
        this.basePackages = basePackages;
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

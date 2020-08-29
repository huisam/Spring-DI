package core.mvc.tobe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import core.annotation.web.Controller;
import core.annotation.web.RequestMapping;
import core.annotation.web.RequestMethod;
import core.di.factory.*;
import core.mvc.HandlerMapping;
import core.mvc.tobe.support.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class AnnotationHandlerMapping implements HandlerMapping {

    private static final List<ArgumentResolver> ARGUMENT_RESOLVERS = Lists.newArrayList(
            new HttpRequestArgumentResolver(),
            new HttpResponseArgumentResolver(),
            new RequestParamArgumentResolver(),
            new PathVariableArgumentResolver(),
            new ModelArgumentResolver()
    );

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new LocalVariableTableParameterNameDiscoverer();
    public static final Class<Controller> HANDLER_ANNOTATION = Controller.class;
    private BeanFactory beanFactory;

    private Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

    public AnnotationHandlerMapping(Object... basePackage) {
        if (ArrayUtils.isEmpty(basePackage)) {
            final ComponentBasePackageScanner componentBasePackageScanner = new ComponentBasePackageScanner(basePackage);
            basePackage = componentBasePackageScanner.scan().toArray();
        }

        BeanScanner beanScanner = new BeanScanner(basePackage);
        final ConfigurationBeanScanner configurationBeanScanner = new ConfigurationBeanScanner(basePackage);
        final BeanScanners beanScanners = new BeanScanners(beanScanner, configurationBeanScanner);

        beanFactory = new BeanFactory(beanScanners);
        beanFactory.initialize();
    }

    public void initialize() {
        log.info("## Initialized Annotation Handler Mapping");
        final Set<Object> controllerInstances = beanFactory.getBeansAnnotatedWith(HANDLER_ANNOTATION);

        Map<HandlerKey, HandlerExecution> handlers = Maps.newHashMap();
        for (Object controller : controllerInstances) {
            addHandlerExecutions(handlers, controller, controller.getClass().getMethods());
        }
        handlerExecutions.putAll(handlers);
    }

    private void addHandlerExecutions(Map<HandlerKey, HandlerExecution> handlers, Object target, Method[] methods) {
        Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(method -> {
                    final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    final HandlerKey handlerKey = new HandlerKey(requestMapping.value(), requestMapping.method());
                    final HandlerExecution handlerExecution = new HandlerExecution(NAME_DISCOVERER, ARGUMENT_RESOLVERS, target, method);
                    handlers.put(handlerKey, handlerExecution);
                    log.info("Add - method: {}, path: {}, HandlerExecution: {}", requestMapping.method(), requestMapping.value(), method.getName());
                });
    }

    public Object getHandler(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        RequestMethod rm = RequestMethod.valueOf(request.getMethod().toUpperCase());
        log.debug("requestUri : {}, requestMethod : {}", requestUri, rm);
        return getHandlerInternal(new HandlerKey(requestUri, rm));
    }

    private HandlerExecution getHandlerInternal(HandlerKey requestHandlerKey) {
        return handlerExecutions.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isMatch(requestHandlerKey))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}

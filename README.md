## Spring DI

Spring 🌱 의 핵심 기술인 `DI`, `IoC` 컨테이너를 구축해보자~!

### Bean Factory 🚧 

Spring에서는 기본적으로 `Bean Factory` 인터페이스를 상속받아서    
`Application Context` 를 구현하고 있어요  

```java
package core.di.factory;


import java.util.List;

public interface BeanFactory {
    void initialize();

    void registerBeanDefinition(Class<?> clazz, BeanDefinition beanDefinition);

    <T> T getBean(Class<T> requiredType);

    List<Class<?>> getBeanClasses();

    List<Object> getBeans();
}
```
하지만 이번 예시에서는 `Application Context`에 `Bean Factory`를 주입받아서 구현할 정도로 할까 해요  


### BeanScanner 🔎

어쩌면 Spring에서 가장 중요한 발전중의 하나인 `Bean Scanning` 방식이 제일 중요하다고 생각해요  
과거 Spring에서는 `xml`을 기반으로 bean config을 등록해서 관리가 많이 힘들었다면,  
지금은 `@Bean / @Component`를 바탕으로 정말 쉽게 관리할 수 있기 때문이죠❗    
```java
package core.di;

import java.util.Set;

public interface Scanner<T> {

    Set<T> scan(Object... basePackage);
}
```

```java
    @Override
    public Set<Object> scan(Object... object) {
        final AnnotationScanner annotationScanner = new AnnotationScanner();
        final Set<Class<? extends Annotation>> scannedAnnotations = annotationScanner.scan(COMPONENT_SCAN_ANNOTATION);

        final Set<Class<?>> classesAnnotatedComponentScan = allReflections.getTypesAnnotatedWith(COMPONENT_SCAN_ANNOTATION, true);

        registerBasePackageOfComponentScan(classesAnnotatedComponentScan);
        registerPackageOfClassesWithOutBasePackage(classesAnnotatedComponentScan);
        registerBasePackageOfAnnotations(scannedAnnotations);

        return new HashSet<>(this.basePackages);
    }
```
가장 핵심은 **Reflection**에서 `getTypesAnnotatedWith` 라는 메서드인데요  
Reflection을 통해 Base Package에 지정한 하위 패키지에 있는 모든 Class들을 가져와서  
`@Component` 라는 어노테이션이 달려진 Class들을 scan 하는 책임을 가지고 있는 메서드입니다~!  
<br>
그래서 위 과정을 통해 우리가 만들어 놓은 `Bean Factory` 에 `Map<Class<?> Object>` 로 객체들을 가지고 있게 되는 거죠.!!  
물론, Bean을 등록하는 과정에서 **순환 참조** 에 대한 검사도 해야됩니다 ㅎㅎ  

```java
private Object registerBean(Class<?> preInstantiateBean) {
        if (beanInitializeHistory.contains(preInstantiateBean)) {
            throw new CircularReferenceException("Circular Reference can't add to Bean Factory: " + preInstantiateBean.getSimpleName());
        }

        if (beans.containsKey(preInstantiateBean)) {
            return beans.get(preInstantiateBean);
        }

        this.beanInitializeHistory.push(preInstantiateBean);
        final Object instance = registerBeanWithInstantiating(preInstantiateBean);
        this.beanInitializeHistory.pop();

        return instance;
    }
```
특정 객체에서부터 참조하고 있는 객체들을 `registerBean`을 시행할 때,  
`beanInitializeHistory`를 통해 객체들이 이미 등록되어 있는지 아닌지 체크 ✅해서  
순환 참조에 대한 **유효성 검사**를 실시하게 되요.!  

이렇게 하면 우리가 `Spring DI`에서 지원하는 핵심적인 기술.!  
1. Bean Scanner
2. Bean Factory
3. Application Context 

의 동작원리를 정확하게 알게 되었습니다  👋  👋  👋 
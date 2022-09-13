package io.github.wynn5a.di;

import static io.github.wynn5a.di.exception.ContainerConfigException.illegalAnnotation;
import static java.util.Arrays.stream;
import static java.util.List.of;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.IllegalQualifierException;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * TDD 过程中无需提前优化和重构，而是等到功能告一段落之后，在进行重构，这样能够避免局部优化导致坏味道藏匿起来
 */
public class ContainerConfig {

  private final Map<InstanceType, InstanceSupplier<?>> instanceSuppliers = new HashMap<>();
  private final Map<Class<?>, ScopeSupplier> scopeSupplier = new HashMap<>();

  public ContainerConfig() {
    scopeSupplier.put(Singleton.class, SingletonInstanceSupplier::new);
  }

  private void bindComponent(Class<?> type, Class<?> implementation, Annotation... annotations) {
    Bindings bindings = Bindings.component(implementation, annotations);
    bind(type, bindings.qualifiers(), supplier(implementation, bindings.scope()));
  }

  private <Type> InstanceSupplier<Type> supplier(Class<Type> implementation, Optional<Annotation> scope) {
    InjectedInstanceSupplier<Type> instanceSupplier = new InjectedInstanceSupplier<>(implementation);
    return scope.map(s -> {
      Class<? extends Annotation> key = s.annotationType();
      if (!scopeSupplier.containsKey(key)) {
        throw new IllegalComponentException("Scope instance supplier is  undefined for scope: " + s);
      }
      return this.scopeSupplier.get(key).create(instanceSupplier);
    }).orElse(instanceSupplier);
  }

  private void bindInstance(Class<?> type, Object instance, Annotation[] annotations) {
    bind(type, Bindings.instance(type, annotations).qualifiers(), container -> instance);
  }

  private void bind(Class<?> type, List<Annotation> qualifiers, InstanceSupplier<?> supplier) {
    if (qualifiers == null || qualifiers.size() == 0) {
      instanceSuppliers.put(new InstanceType(type, null), supplier);
    } else {
      qualifiers.forEach(q -> instanceSuppliers.put(new InstanceType(type, q), supplier));
    }
  }

  public <T> void bind(Class<T> type, T instance, Annotation... annotations) {
    checkAnnotations(annotations);
    List<Annotation> qualifiers = getQualifiers(annotations);
    bind(type, qualifiers, i -> instance);
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType, Annotation... annotations) {
    Map<Class<?>, List<Annotation>> annotationGroups = stream(annotations)
        .collect(groupingBy(this::byType, toList()));
    if (annotationGroups.containsKey(IllegalType.class)) {
      throw new IllegalComponentException("Illegal annotations were found");
    }
    List<Annotation> qualifiers = annotationGroups.getOrDefault(Qualifier.class, of());
    InstanceSupplier<I> supplier = createScopedSupplier(instanceType, annotationGroups.getOrDefault(Scope.class, of()));
    bind(type, qualifiers, supplier);
  }

  private <I> InstanceSupplier<I> createScopedSupplier(Class<I> instanceType, List<Annotation> scopes) {
    List<Annotation> scopesFromType = scopeFrom(instanceType);
    if (scopes.size() > 1 || scopesFromType.size() > 1) {
      throw new IllegalComponentException("Only one scope annotation is supported");
    }
    Optional<Annotation> scope = scopes.stream().findFirst().or(() -> scopesFromType.stream().findFirst());
    return supplier(instanceType, scope);
  }

  private static <I> List<Annotation> scopeFrom(Class<I> instanceType) {
    return stream(instanceType.getDeclaredAnnotations())
        .filter(a -> a.annotationType().isAnnotationPresent(Scope.class))
        .toList();
  }

  private Class<?> byType(Annotation annotation) {
    Class<?> type = annotation.annotationType();
    return Stream.of(Scope.class, Qualifier.class).filter(type::isAnnotationPresent).findFirst()
                 .orElse(IllegalType.class);
  }

  @interface IllegalType {

  }

  private static List<Annotation> getQualifiers(Annotation[] annotations) {
    return stream(annotations).filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).toList();
  }

  private void checkAnnotations(Annotation[] qualifiers) {
    if (stream(qualifiers).map(Annotation::annotationType)
                          .anyMatch(q -> !q.isAnnotationPresent(Qualifier.class)
                              && !q.isAnnotationPresent(Scope.class))) {
      throw new IllegalQualifierException();
    }
  }


  public Container getContainer() {
    instanceSuppliers.keySet().forEach(c -> checkDependencies(c, new Stack<>()));
    return new Container() {

      @Override
      public Optional get(InstanceTypeRef instanceTypeRef) {
        InstanceSupplier<?> instanceSupplier = getSupplier(instanceTypeRef);
        if (instanceTypeRef.isContainerType()) {
          if (Supplier.class == instanceTypeRef.getContainerType()){
            return Optional.ofNullable(instanceSupplier).map(s -> (Supplier<Object>) () -> s.get(this));
          }
          if(Provider.class == instanceTypeRef.getContainerType()){
            return Optional.ofNullable(instanceSupplier).map(s -> (Provider<Object>) () -> s.get(this));
          }
          return Optional.empty();
        }
        return Optional.ofNullable(instanceSupplier).map(s -> (Object) s.get(this));
      }
    };
  }

  private InstanceSupplier<?> getSupplier(InstanceTypeRef instanceTypeRef) {
    return instanceSuppliers.get(instanceTypeRef.instanceType());
  }

  private void checkDependencies(InstanceType component, Stack<InstanceType> visiting) {
    for (InstanceTypeRef dependency : instanceSuppliers.get(component).dependencies()) {
      InstanceType dependencyType = dependency.instanceType();
      if (!instanceSuppliers.containsKey(dependencyType)) {
        throw new DependencyNotFoundException(component, dependencyType);
      }
      if (!dependency.isContainerType()) {
        visiting.push(component);
        if (visiting.contains(dependencyType)) {
          throw new CyclicDependencyFoundException(visiting);
        }
        checkDependencies(dependencyType, visiting);
        visiting.pop();
      }
    }
  }

  public <ScopeType extends Annotation> void scope(Class<ScopeType> scope, ScopeSupplier supplier) {
    scopeSupplier.put(scope, supplier);
  }

  public void from(Config config) {
    new DSL(config).bind();
  }

  static class Bindings {

    Class<?> type;
    Map<Class<?>, List<Annotation>> group;

    public Bindings(Class<?> type, Annotation[] annotations, Class<? extends Annotation>... allowed) {
      this.type = type;
      this.group = parse(type, annotations, allowed);
    }

    public static Bindings component(Class<?> component, Annotation... annotations) {
      return new Bindings(component, annotations, Qualifier.class, Scope.class);
    }

    public static Bindings instance(Class<?> instance, Annotation... annotations) {
      return new Bindings(instance, annotations, Qualifier.class);
    }

    private static Map<Class<?>, List<Annotation>> parse(Class<?> type, Annotation[] annotations,
                                                         Class<? extends Annotation>... allowed) {
      Map<Class<?>, List<Annotation>> annotationGroups = stream(annotations).collect(groupingBy(allow(allowed), toList()));
      if (annotationGroups.containsKey(Illegal.class)) {
        throw illegalAnnotation(type, annotationGroups.get(Illegal.class));
      }
      return annotationGroups;
    }

    private static Function<Annotation, Class<?>> allow(Class<? extends Annotation>... annotations) {
      return annotation -> Stream.of(annotations).filter(annotation.annotationType()::isAnnotationPresent).findFirst()
                                 .orElse(Illegal.class);
    }

    private @interface Illegal {

    }

    Optional<Annotation> scope() {
      List<Annotation> scopes = group.getOrDefault(Scope.class, from(type, Scope.class));
      if (scopes.size() > 1) {
        throw illegalAnnotation(type, scopes);
      }
      return scopes.stream().findFirst();
    }

    List<Annotation> qualifiers() {
      return group.getOrDefault(Qualifier.class, List.of());
    }

    private static List<Annotation> from(Class<?> implementation, Class<? extends Annotation> annotation) {
      return stream(implementation.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(annotation))
                                                    .toList();
    }
  }

  class DSL {

    private Config config;

    public DSL(Config config) {
      this.config = config;
    }

    void bind() {
      for (Declaration declaration : declarations()) {
        declaration.value().ifPresentOrElse(declaration::bindInstance, declaration::bindComponent);
      }
    }

    private List<Declaration> declarations() {
      return stream(config.getClass().getDeclaredFields()).filter(f -> !f.isSynthetic()).map(Declaration::new)
                                                          .toList();
    }

    class Declaration {

      private Field field;

      Declaration(Field field) {
        this.field = field;
      }

      private Optional<Object> value() {
        try {
          return Optional.ofNullable(field.get(config));
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }

      public void bindInstance(Object instance) {
        ContainerConfig.this.bindInstance(type(), instance, annotations());
      }

      private Class<?> type() {
        Config.Export export = field.getAnnotation(Config.Export.class);
        return export != null ? export.value() : field.getType();
      }

      private Annotation[] annotations() {
        return stream(field.getAnnotations()).filter(a -> a.annotationType() != Config.Export.class)
                                             .toArray(Annotation[]::new);
      }

      public void bindComponent() {
        ContainerConfig.this.bindComponent(type(), field.getType(), annotations());
      }
    }
  }
}


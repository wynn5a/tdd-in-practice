package io.github.wynn5a.di;

import static java.util.List.of;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.IllegalQualifierException;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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

  public <T> void bind(Class<T> type, T instance, Annotation... annotations) {
    checkAnnotations(annotations);
    List<Annotation> qualifiers = getQualifiers(annotations);
    if (qualifiers.size() == 0) {
      instanceSuppliers.put(new InstanceType(type, null), c -> instance);
      return;
    }
    qualifiers.forEach(q -> instanceSuppliers.put(new InstanceType(type, q), c -> instance));
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType, Annotation... annotations) {
    Map<Class<?>, List<Annotation>> annotationGroups = Arrays.stream(annotations)
                                                             .collect(Collectors.groupingBy(this::byType, Collectors.toList()));
    if (annotationGroups.containsKey(IllegalType.class)) {
      throw new IllegalComponentException("Illegal annotations were found");
    }
    List<Annotation> qualifiers = annotationGroups.getOrDefault(Qualifier.class, of());
    InstanceSupplier<I> supplier = createScopedSupplier(instanceType, annotationGroups.getOrDefault(Scope.class, of()));
    if (qualifiers.size() == 0) {
      instanceSuppliers.put(new InstanceType(type, null), supplier);
      return;
    }
    qualifiers.forEach(q -> instanceSuppliers.put(new InstanceType(type, q), supplier));
  }

  private <I> InstanceSupplier<I> createScopedSupplier(Class<I> instanceType, List<Annotation> scopes) {
    List<Annotation> scopesFromType = scopeFrom(instanceType);
    if (scopes.size() > 1 || scopesFromType.size() > 1) {
      throw new IllegalComponentException("Only one scope annotation is supported");
    }
    Optional<Annotation> scope = scopes.stream().findFirst().or(() -> scopesFromType.stream().findFirst());
    InjectedInstanceSupplier<I> instanceSupplier = new InjectedInstanceSupplier<>(instanceType);
    return scope.map(s -> {
      Class<? extends Annotation> key = s.annotationType();
      if (!scopeSupplier.containsKey(key)) {
        throw new IllegalComponentException("Scope instance supplier is  undefined for scope: " + s);
      }
      return this.scopeSupplier.get(key).create(instanceSupplier);
    }).orElse(instanceSupplier);
  }

  private static <I> List<Annotation> scopeFrom(Class<I> instanceType) {
    return Arrays.stream(instanceType.getDeclaredAnnotations())
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
    return Arrays.stream(annotations).filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).toList();
  }

  private void checkAnnotations(Annotation[] qualifiers) {
    if (Arrays.stream(qualifiers).map(Annotation::annotationType)
              .anyMatch(q -> !q.isAnnotationPresent(Qualifier.class) && !q.isAnnotationPresent(Scope.class))) {
      throw new IllegalQualifierException();
    }
  }


  public Container getContainer() {
    instanceSuppliers.keySet().forEach(c -> checkDependencies(c, new Stack<>()));
    return new Container() {

      @Override
      public Optional get(InstanceTypeRef instanceTypeRef) {
        if (instanceTypeRef.instanceType().qualifier() != null) {
          return Optional.ofNullable(getSupplier(instanceTypeRef)).map(s -> s.get(this));
        }

        InstanceSupplier<?> instanceSupplier = getSupplier(instanceTypeRef);
        if (instanceTypeRef.isContainerType()) {
          if (instanceTypeRef.getContainerType() != Supplier.class) {
            return Optional.empty();
          }
          return Optional.ofNullable(instanceSupplier).map(s -> (Supplier<Object>) () -> s.get(this));
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

  public <ScopeType extends Annotation> void scope(Class<ScopeType> scope,
                                                   ScopeSupplier supplier) {
    scopeSupplier.put(scope, supplier);
  }
}


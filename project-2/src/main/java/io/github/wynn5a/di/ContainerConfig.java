package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
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
import java.util.function.Function;
import java.util.function.Supplier;

public class ContainerConfig {

  private final Map<InstanceType, InstanceSupplier<?>> instanceSuppliers = new HashMap<>();
  private final Map<Class<?>, Function<InstanceSupplier<?>, InstanceSupplier<?>>> scopes = new HashMap<>();

  public ContainerConfig() {
    scopes.put(Singleton.class, SingletonInstanceSupplier::new);
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
    checkAnnotations(annotations);
    List<Annotation> qualifiers = getQualifiers(annotations);
    Optional<Annotation> scope = getScopeAnnotation(annotations, instanceType);

    InjectedInstanceSupplier<I> injectedInstanceSupplier = new InjectedInstanceSupplier<>(instanceType);
    InstanceSupplier<I> supplier = scope.map(s -> (InstanceSupplier<I>) scopes.get(s.annotationType())
                                                                              .apply(injectedInstanceSupplier))
                                        .orElse(injectedInstanceSupplier);

    if (qualifiers.size() == 0) {
      instanceSuppliers.put(new InstanceType(type, null), supplier);
      return;
    }
    qualifiers.forEach(q ->
        instanceSuppliers.put(new InstanceType(type, q), supplier));
  }

  private Optional<Annotation> getScopeAnnotation(Annotation[] annotations, Class<?> instanceType) {
    return Arrays.stream(annotations).filter(a -> a.annotationType().isAnnotationPresent(Scope.class))
                 .findFirst().or(() -> Arrays.stream(instanceType.getDeclaredAnnotations())
                                             .filter(a -> a.annotationType().isAnnotationPresent(Scope.class))
                                             .findFirst());
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
                                                   Function<InstanceSupplier<?>, InstanceSupplier<?>> supplier) {
    scopes.put(scope, supplier);
  }
}

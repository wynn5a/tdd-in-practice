package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import io.github.wynn5a.di.exception.IllegalQualifierException;
import jakarta.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Supplier;

public class ContainerConfig {

  private final Map<InstanceType, InstanceSupplier<?>> instanceSuppliers = new HashMap<>();

  public <T> void bind(Class<T> type, T instance, Annotation... qualifiers) {
    if (qualifiers.length == 0) {
      instanceSuppliers.put(new InstanceType(type, null), c -> instance);
      return;
    }
    checkQualifiers(qualifiers);
    for (Annotation qualifier : qualifiers) {
      instanceSuppliers.put(new InstanceType(type, qualifier), c -> instance);
    }
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType, Annotation... qualifiers) {
    if (qualifiers.length == 0) {
      instanceSuppliers.put(new InstanceType(type, null), new InjectedInstanceSupplier<>(instanceType));
      return;
    }

    checkQualifiers(qualifiers);
    for (Annotation qualifier : qualifiers) {
      instanceSuppliers.put(new InstanceType(type, qualifier), new InjectedInstanceSupplier<>(instanceType));
    }
  }

  private void checkQualifiers(Annotation[] qualifiers) {
    if (Arrays.stream(qualifiers).anyMatch(q -> !q.annotationType().isAnnotationPresent(Qualifier.class))) {
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
}

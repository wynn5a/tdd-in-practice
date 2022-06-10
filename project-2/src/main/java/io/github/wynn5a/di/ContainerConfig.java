package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Supplier;

public class ContainerConfig {

  private final Map<Class<?>, InstanceSupplier<?>> suppliers = new HashMap<>();
  private final Map<InstanceType, InstanceSupplier<?>> instanceSuppliers = new HashMap<>();

  record InstanceType(Class<?> type, Annotation qualifier) {

  }

  public <T> void bind(Class<T> type, T instance) {
    suppliers.put(type, c -> instance);
  }

  public <T> void bind(Class<T> type, T instance, Annotation... qualifiers) {
    for (Annotation qualifier : qualifiers) {
      instanceSuppliers.put(new InstanceType(type, qualifier), c -> instance);
    }
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType) {
    suppliers.put(type, new InjectedInstanceSupplier<>(instanceType));
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType, Annotation... qualifiers) {
    for (Annotation qualifier : qualifiers) {
      instanceSuppliers.put(new InstanceType(type, qualifier), new InjectedInstanceSupplier<>(instanceType));
    }
  }


  public Container getContainer() {
    suppliers.keySet().forEach(c -> checkDependencies(c, new Stack<>()));

    return new Container() {

      @Override
      public Optional get(Ref ref) {
        if (ref.getQualifier() != null) {
          return Optional.ofNullable(instanceSuppliers.get(new InstanceType(ref.getComponentType(), ref.getQualifier())))
                         .map(s -> s.get(this));
        }

        InstanceSupplier<?> instanceSupplier = suppliers.get(ref.getComponentType());
        if (ref.isContainerType()) {
          if (ref.getContainerType() != Supplier.class) {
            return Optional.empty();
          }
          return Optional.ofNullable(instanceSupplier).map(s -> (Supplier<Object>) () -> s.get(this));
        }
        return Optional.ofNullable(instanceSupplier).map(s -> (Object) s.get(this));
      }
    };
  }

  private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
    for (Ref dependency : suppliers.get(component).dependencies()) {
      Class<?> componentType = dependency.getComponentType();
      if (!suppliers.containsKey(componentType)) {
        throw new DependencyNotFoundException(component, componentType);
      }
      if (!dependency.isContainerType()) {
        visiting.push(component);
        if (visiting.contains(componentType)) {
          throw new CyclicDependencyFoundException(visiting);
        }
        checkDependencies(componentType, visiting);
        visiting.pop();
      }
    }
  }
}

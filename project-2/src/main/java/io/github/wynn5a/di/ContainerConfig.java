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

  private final Map<InstanceType, InstanceSupplier<?>> instanceSuppliers = new HashMap<>();

  public record InstanceType(Class<?> type, Annotation qualifier) {

  }

  public <T> void bind(Class<T> type, T instance) {
    instanceSuppliers.put(new InstanceType(type, null), c -> instance);
  }

  public <T> void bind(Class<T> type, T instance, Annotation... qualifiers) {
    for (Annotation qualifier : qualifiers) {
      instanceSuppliers.put(new InstanceType(type, qualifier), c -> instance);
    }
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType) {
    instanceSuppliers.put(new InstanceType(type, null), new InjectedInstanceSupplier<>(instanceType));
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType, Annotation... qualifiers) {
    for (Annotation qualifier : qualifiers) {
      instanceSuppliers.put(new InstanceType(type, qualifier), new InjectedInstanceSupplier<>(instanceType));
    }
  }


  public Container getContainer() {
    instanceSuppliers.keySet().forEach(c -> checkDependencies(c, new Stack<>()));

    return new Container() {

      @Override
      public Optional get(Ref ref) {
        if (ref.getQualifier() != null) {
          return Optional.ofNullable(instanceSuppliers.get(new InstanceType(ref.getComponentType(), ref.getQualifier())))
                         .map(s -> s.get(this));
        }

        InstanceSupplier<?> instanceSupplier = getSupplier(ref);
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

  private InstanceSupplier<?> getSupplier(Ref ref) {
    return instanceSuppliers.get(new InstanceType(ref.getComponentType(), ref.getQualifier()));
  }

  private void checkDependencies(InstanceType component, Stack<Class<?>> visiting) {
    for (Ref dependency : instanceSuppliers.get(component).dependencies()) {
      InstanceType dependencyType = new InstanceType(dependency.getComponentType(), dependency.getQualifier());
      if (!instanceSuppliers.containsKey(dependencyType)) {
        throw new DependencyNotFoundException(component.type(), dependency.getComponentType());
      }
      if (!dependency.isContainerType()) {
        visiting.push(component.type());
        if (visiting.contains(dependencyType.type())) {
          throw new CyclicDependencyFoundException(visiting);
        }
        checkDependencies(dependencyType, visiting);
        visiting.pop();
      }
    }
  }
}

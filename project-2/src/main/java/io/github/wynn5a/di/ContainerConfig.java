package io.github.wynn5a.di;

import static java.util.List.of;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class ContainerConfig {

  private final Map<Class<?>, InstanceSupplier<?>> suppliers = new HashMap<>();

  public <T> void bind(Class<T> type, T instance) {
    suppliers.put(type, new InstanceSupplier<T>() {
      @Override
      public T get(Container container) {
        return instance;
      }

      @Override
      public List<Class<?>> dependencies() {
        return of();
      }
    });
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType) {
    suppliers.put(type, new InjectedInstanceSupplier<>(instanceType));
  }


  @SuppressWarnings("unchecked")
  public Container getContainer() {
    suppliers.keySet().forEach(c -> checkDependencies(c, new Stack<>()));

    return new Container() {
      @Override
      public <T> Optional<T> get(Class<T> type) {
        return Optional.ofNullable(suppliers.get(type)).map(s -> (T) s.get(this));
      }
    };
  }

  private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
    visiting.push(component);
    for (Class<?> dependency : suppliers.get(component).dependencies()) {
      if (!suppliers.containsKey(dependency)) {
        throw new DependencyNotFoundException(component, dependency);
      }

      if (visiting.contains(dependency)) {
        throw new CyclicDependencyFoundException(visiting);
      }
      checkDependencies(dependency, visiting);
    }
    visiting.pop();
  }

}


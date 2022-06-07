package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Supplier;

public class ContainerConfig {

  private final Map<Class<?>, InstanceSupplier<?>> suppliers = new HashMap<>();

  public <T> void bind(Class<T> type, T instance) {
    suppliers.put(type, c -> instance);
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

      @Override
      public Optional get(ParameterizedType type) {
        if(type.getRawType() != Supplier.class) {
          return Optional.empty();
        }
        Type typeArgument = type.getActualTypeArguments()[0];
        return Optional.ofNullable(suppliers.get(typeArgument)).map(s -> (Supplier<Object>) () -> s.get(this));
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


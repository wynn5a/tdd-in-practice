package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import jakarta.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Container {

  private final Map<Class<?>, Supplier<?>> SUPPLIER_MAP = new ConcurrentHashMap<>();

  public <T> void bind(Class<T> type, T instance) {
    SUPPLIER_MAP.put(type, () -> instance);
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType) {
    Constructor<?> constructor = getInjectedConstructor(instanceType);
    SUPPLIER_MAP.put(type, new InstanceSupplier<>(this, constructor));
  }


  @SuppressWarnings("unchecked")
  private <I> Constructor<I> getInjectedConstructor(Class<I> instanceType) {
    List<Constructor<?>> allConstructors = Arrays.stream(instanceType.getDeclaredConstructors())
                                                 .filter(c -> c.isAnnotationPresent(Inject.class)).toList();
    if (allConstructors.size() > 1) {
      throw new MultiInjectAnnotationFoundException();
    }

    if (allConstructors.size() == 0) {
      try {
        return instanceType.getConstructor();
      } catch (NoSuchMethodException e) {
        throw new IllegalComponentException(e);
      }
    }

    return (Constructor<I>) allConstructors.get(0);
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> get(Class<T> type) {
    return Optional.ofNullable(SUPPLIER_MAP.get(type)).map(s -> (T) s.get());
  }
}

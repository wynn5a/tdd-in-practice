package io.github.wynn5a.di;

import jakarta.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Container {

  private static final Map<Class<?>, Supplier<?>> SUPPLIER_MAP = new ConcurrentHashMap<>();

  public <T> void bind(Class<T> type, T instance) {
    SUPPLIER_MAP.put(type, () -> instance);
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType) {
    SUPPLIER_MAP.put(type, () -> {
      try {
        Constructor<?> constructor = getInjectedConstructor(instanceType);
        Object[] objects = Arrays.stream(constructor.getParameterTypes()).map(this::get).toArray();
        return constructor.newInstance(objects);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private <I> Constructor<?> getInjectedConstructor(Class<I> instanceType) {
    return Arrays.stream(instanceType.getDeclaredConstructors())
        .filter(c -> c.isAnnotationPresent(Inject.class))
        .findFirst()
        .orElseGet(() -> {
          try {
            return instanceType.getConstructor();
          } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
          }
        });
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    return (T) SUPPLIER_MAP.get(type).get();
  }

}

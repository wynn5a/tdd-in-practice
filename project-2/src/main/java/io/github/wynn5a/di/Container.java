package io.github.wynn5a.di;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Container {

  private static final Map<Class<?>, Supplier<?>> CONTAINER = new ConcurrentHashMap<>();

  public <T> void bind(Class<T> type, T instance) {
    CONTAINER.put(type, () -> instance);
  }

  public <T, I extends T> void bind(Class<T> type, Class<I> instanceType) {
    CONTAINER.put(type, () -> {
      try {
        return instanceType.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    return (T) CONTAINER.get(type).get();
  }

}

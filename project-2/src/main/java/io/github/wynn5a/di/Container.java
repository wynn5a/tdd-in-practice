package io.github.wynn5a.di;

import jakarta.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
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
        return getInstanceByInjectedConstructor(instanceType);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private <I> I getInstanceByInjectedConstructor(Class<I> instanceType)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

    List<Constructor<?>> constructors = Arrays.stream(instanceType.getDeclaredConstructors())
        .filter(c -> c.getAnnotation(Inject.class) != null).toList();

    if (constructors.size() == 0) {
      return instanceType.getDeclaredConstructor().newInstance();
    }

    Constructor<?> constructor = constructors.get(0);
    Object[] objects = Arrays.stream(constructor.getParameterTypes()).map(this::get).toArray();
    return (I) constructor.newInstance(objects);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    return (T) SUPPLIER_MAP.get(type).get();
  }

}

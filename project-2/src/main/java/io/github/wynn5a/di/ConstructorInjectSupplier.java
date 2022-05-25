package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import jakarta.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 * @author wynn5a
 * @date 2022/5/23
 */
public class ConstructorInjectSupplier<T> implements InstanceSupplier<T> {

  private final Constructor<T> constructor;
  private final List<Class<?>> dependencies;

  public ConstructorInjectSupplier(Class<T> instanceType) {
    this.constructor = getInjectedConstructor(instanceType);
    this.dependencies = Arrays.asList(constructor.getParameterTypes());
  }

  @SuppressWarnings("unchecked")
  static <I> Constructor<I> getInjectedConstructor(Class<I> instanceType) {
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

  @Override
  public T get(Container container) {
    try {
      Object[] objects = dependencies.stream()
                                     .map(c -> container.get(c).orElse(null))
                                     .toArray();
      return constructor.newInstance(objects);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Class<?>> dependencies() {
    return dependencies;
  }
}

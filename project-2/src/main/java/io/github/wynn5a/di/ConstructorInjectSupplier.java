package io.github.wynn5a.di;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * @author wynn5a
 * @date 2022/5/23
 */
public class ConstructorInjectSupplier<T> implements InstanceSupplier<T> {

  private final Constructor<T> constructor;

  public ConstructorInjectSupplier(Constructor<T> constructor) {
    this.constructor = constructor;
  }

  @Override
  public T get(Container container) {
    try {
      Object[] objects = Arrays.stream(constructor.getParameterTypes())
                               .map(c -> container.get(c).orElse(null))
                               .toArray();
      return constructor.newInstance(objects);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

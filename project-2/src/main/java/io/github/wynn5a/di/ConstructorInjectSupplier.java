package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.IllegalDependencyException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author wynn5a
 * @date 2022/5/23
 */
public class ConstructorInjectSupplier<T> implements Supplier<T> {

  private final Container container;
  private final Class<?> componentType;
  private final Constructor<T> constructor;

  private boolean constructing = false;

  public ConstructorInjectSupplier(Container container, Class<?> componentType, Constructor<T> constructor) {
    this.componentType = componentType;
    this.constructor = constructor;
    this.container = container;
  }

  @Override
  public T get() {
    if (constructing) {
      throw new CyclicDependencyFoundException(constructor.getDeclaringClass().getName());
    }

    try {
      constructing = true;
      Object[] objects = Arrays.stream(constructor.getParameterTypes())
                               .map(p -> container.get(p)
                                                  .orElseThrow(() -> new IllegalDependencyException(componentType, p)))
                               .toArray();
      return constructor.newInstance(objects);
    } catch (CyclicDependencyFoundException e) {
      throw new CyclicDependencyFoundException(constructor.getDeclaringClass().getName(), e);
    }
    catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      constructing = false;
    }
  }
}

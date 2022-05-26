package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import jakarta.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wynn5a
 * @date 2022/5/23
 */
public class InjectedInstanceSupplier<T> implements InstanceSupplier<T> {

  private final Constructor<T> constructor;
  private final List<Field> injectedFields;

  public InjectedInstanceSupplier(Class<T> instanceType) {
    this.constructor = getInjectedConstructor(instanceType);
    this.injectedFields = getInjectedFields(instanceType);
  }

  private static <T> List<Field> getInjectedFields(Class<T> instanceType) {
    List<Field> fields = Arrays.stream(instanceType.getDeclaredFields())
                               .filter(f -> f.isAnnotationPresent(Inject.class))
                               .collect(Collectors.toList());

    for (Field f : fields) {
      if (Modifier.isFinal(f.getModifiers())) {
        throw new IllegalComponentException("Field '" + f.getName() + "' is failed to inject because it is final");
      }
    }

    return fields;
  }

  @SuppressWarnings("unchecked")
  private static <I> Constructor<I> getInjectedConstructor(Class<I> instanceType) {
    List<Constructor<?>> allConstructors = Arrays.stream(instanceType.getDeclaredConstructors())
                                                 .filter(c -> c.isAnnotationPresent(Inject.class)).toList();
    if (allConstructors.size() > 1) {
      throw new MultiInjectAnnotationFoundException();
    }

    if (allConstructors.size() == 0) {
      try {
        return instanceType.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        throw new IllegalComponentException(e);
      }
    }

    return (Constructor<I>) allConstructors.get(0);
  }

  @Override
  public T get(Container container) {
    try {
      Object[] objects = Arrays.stream(constructor.getParameterTypes())
                               .map(c -> container.get(c).orElse(null))
                               .toArray();
      T t = constructor.newInstance(objects);

      injectedFields.forEach(f -> {
        try {
          f.setAccessible(true);
          f.set(t, container.get(f.getType()).orElse(null));
        } catch (IllegalAccessException e) {
          throw new IllegalComponentException(e);
        }
      });

      return t;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Class<?>> dependencies() {
    return Stream.concat(injectedFields.stream().map(Field::getType), Stream.of(constructor.getParameterTypes()))
                 .collect(Collectors.toList());
  }
}

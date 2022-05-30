package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import jakarta.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wynn5a
 * @date 2022/5/23
 */
public class InjectedInstanceSupplier<T> implements InstanceSupplier<T> {

  private final Constructor<T> constructor;
  private final List<Field> injectedFields;
  private final List<Method> injectedMethods;

  public InjectedInstanceSupplier(Class<T> instanceType) {
    this.constructor = getInjectedConstructor(instanceType);
    this.injectedFields = getInjectedFields(instanceType);
    this.injectedMethods = getInjectedMethods(instanceType);
  }

  private static <T> List<Method> getInjectedMethods(Class<T> instanceType) {
    var results = new ArrayList<Method>();
    Class<?> currentType = instanceType;

    while (currentType != Object.class) {
      var methods = currentType.getDeclaredMethods();
      for (var method : methods) {
        if (method.isAnnotationPresent(Inject.class)) {
          //inject will invoke only once if subclass has injected annotation
          if(isOverriddenWithInjected(results, method)){
            continue;
          }
          //inject will be disabled if subclass has not injected annotation
          if(isOverridden(instanceType, method, currentType)){
           continue;
          }
          results.add(method);
        }
      }
      currentType = currentType.getSuperclass();
    }
    Collections.reverse(results);
    return results;
  }

  private static <T> boolean isOverridden(Class<T> instanceType, Method method, Class<?> currentType) {
    try {
      return instanceType.getMethod(method.getName(), method.getParameterTypes()).getDeclaringClass() != currentType;
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isOverriddenWithInjected(ArrayList<Method> results, Method method) {
    return results.stream().anyMatch(m -> m.getName().equals(method.getName())
        && Arrays.equals(m.getParameterTypes(), method.getParameterTypes()));
  }

  private static <T> List<Field> getInjectedFields(Class<T> instanceType) {
    List<Field> result = new ArrayList<>();
    Class<?> currentType = instanceType;
    while (currentType != Object.class) {
      for (Field f : currentType.getDeclaredFields()) {
        if (f.isAnnotationPresent(Inject.class)) {
          if (Modifier.isFinal(f.getModifiers())) {
            throw new IllegalComponentException("Field '" + f.getName() + "' is failed to inject because it is final");
          }
          result.add(f);
        }
      }
      currentType = currentType.getSuperclass();
    }
    return result;
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

      injectedMethods.forEach(m -> {
        try {
          m.setAccessible(true);
          Object[] parameters = Arrays.stream(m.getParameterTypes())
                                      .map(c -> container.get(c).orElse(null))
                                      .toArray();
          m.invoke(t, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
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
    return Stream.of(Arrays.stream(constructor.getParameterTypes()),
                     injectedFields.stream().map(Field::getType),
                     injectedMethods.stream().map(Method::getParameterTypes).flatMap(Arrays::stream))
                 .flatMap(Function.identity())
                 .distinct()
                 .collect(Collectors.toList());
  }
}

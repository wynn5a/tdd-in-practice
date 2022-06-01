package io.github.wynn5a.di;

import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import jakarta.inject.Inject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 代码的重复，很多时候是逻辑重复的体现，寻找逻辑上面的重复，比直接看代码的重复要重要的多，因为有时候重复逻辑是用不同的方式实现的，代码上面并没有明显的重复
 *
 * @author wynn5a
 * @date 2022/5/23
 */
public class InjectedInstanceSupplier<T> implements InstanceSupplier<T> {

  private final Constructor<T> constructor;
  private final List<Field> injectedFields;
  private final List<Method> injectedMethods;

  public InjectedInstanceSupplier(Class<T> instanceType) {
    instanceTypeShouldBeInstantiable(instanceType);
    this.constructor = getInjectedConstructor(instanceType);
    this.injectedFields = getInjectedFields(instanceType);
    injectedFieldShouldNotBeFinal();
    this.injectedMethods = getInjectedMethods(instanceType);
    injectedMethodShouldNotHasTypeParameter();
  }

  private static List<Method> getInjectedMethods(Class<?> instanceType) {
    List<Method> results = traverse(instanceType, (methods, current) -> injectable(current.getDeclaredMethods())
        .filter(m -> !exists(methods, m))
        .filter(m -> !isOverridden(instanceType, m, current))
        .toList());
    Collections.reverse(results);
    return results;
  }

  private static List<Field> getInjectedFields(Class<?> instanceType) {
    return traverse(instanceType, (fields, current) -> injectable(current.getDeclaredFields()).toList());
  }

  @SuppressWarnings("unchecked")
  private static <I> Constructor<I> getInjectedConstructor(Class<I> instanceType) {
    List<Constructor<?>> allConstructors = injectable(instanceType.getDeclaredConstructors()).toList();
    if (allConstructors.size() > 1) {
      throw new MultiInjectAnnotationFoundException();
    }

    return (Constructor<I>) allConstructors.stream().findFirst().orElseGet(() -> getDefaultConstructor(instanceType));
  }

  @Override
  public T get(Container container) {
    try {
      T t = constructor.newInstance(getParameters(container, constructor));
      for (Field f : injectedFields) {
        f.setAccessible(true);
        f.set(t, getParameter(container, f));
      }
      for (Method m : injectedMethods) {
        m.setAccessible(true);
        m.invoke(t, getParameters(container, m));
      }
      return t;
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

  private void injectedFieldShouldNotBeFinal() {
    injectedFields.stream().filter(f -> Modifier.isFinal(f.getModifiers())).findAny().ifPresent(f -> {
      throw new IllegalComponentException("Field '" + f.getName() + "' is failed to inject because it is final");
    });
  }

  private void injectedMethodShouldNotHasTypeParameter() {
    injectedMethods.stream().filter(m -> m.getTypeParameters().length > 0).findAny().ifPresent(m -> {
      throw new IllegalComponentException(
          "Method '" + m.getName() + "' is failed to inject because it has typed parameters");
    });
  }

  private static <T> List<T> traverse(Class<?> instanceType, BiFunction<List<T>, Class<?>, List<T>> finder) {
    List<T> results = new ArrayList<>();
    Class<?> currentType = instanceType;
    while (currentType != Object.class) {
      results.addAll(finder.apply(results, currentType));
      currentType = currentType.getSuperclass();
    }
    return results;
  }

  private static boolean isOverridden(Class<?> instanceType, Method method, Class<?> currentType) {
    try {
      return instanceType.getMethod(method.getName(), method.getParameterTypes()).getDeclaringClass() != currentType;
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean exists(List<Method> results, Method method) {
    return results.stream().anyMatch(m -> m.getName().equals(method.getName())
        && Arrays.equals(m.getParameterTypes(), method.getParameterTypes()));
  }

  private static <T extends AnnotatedElement> Stream<T> injectable(T[] elements) {
    return Arrays.stream(elements).filter(f -> f.isAnnotationPresent(Inject.class));
  }

  private static <I> Constructor<I> getDefaultConstructor(Class<I> instanceType) {
    try {
      return instanceType.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      throw new IllegalComponentException(e);
    }
  }

  private static Object getParameter(Container container, Field f) {
    return container.get(f.getType()).orElse(null);
  }

  private static Object[] getParameters(Container container, Executable m) {
    return Arrays.stream(m.getParameterTypes())
                 .map(c -> container.get(c).orElse(null))
                 .toArray();
  }

  private static <T> void instanceTypeShouldBeInstantiable(Class<T> instanceType) {
    // instance type should not be an interface or an abstract class
    if (Modifier.isAbstract(instanceType.getModifiers())) {
      throw new IllegalComponentException(
          "Class '" + instanceType.getName() + "' is failed to inject because it is abstract");
    }
  }
}

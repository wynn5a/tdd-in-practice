package io.github.wynn5a.di;

import static java.util.Arrays.stream;

import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import jakarta.inject.Inject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
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

  private final Injectable<Constructor<T>> injectConstructor;
  private final List<Injectable<Field>> injectFields;
  private final List<Injectable<Method>> injectMethods;

  public InjectedInstanceSupplier(Class<T> instanceType) {
    instanceTypeShouldBeInstantiable(instanceType);

    injectConstructor = getInjectConstructor(instanceType);
    injectFields = getInjectFields(instanceType);
    injectMethods = getInjectMethods(instanceType);

    injectedFieldShouldNotBeFinal();
    injectedMethodShouldNotHasTypeParameter();
  }

  private static <T> List<Injectable<Method>> getInjectMethods(Class<T> instanceType) {
    List<Method> results = traverse(instanceType, (methods, current) -> injectable(current.getDeclaredMethods())
        .filter(m -> !exists(methods, m))
        .filter(m -> !isOverridden(instanceType, m, current))
        .toList());
    Collections.reverse(results);
    return results.stream().map(Injectable::of).toList();
  }

  private static <T> List<Injectable<Field>> getInjectFields(Class<T> instanceType) {
    return InjectedInstanceSupplier.<Field>traverse(instanceType, (fields, current) -> injectable(current.getDeclaredFields()).toList())
                                   .stream().map(Injectable::of).toList();
  }

  private static <T> Injectable<Constructor<T>> getInjectConstructor(Class<T> instanceType) {
    List<Constructor<?>> allConstructors = injectable(instanceType.getDeclaredConstructors()).toList();
    if (allConstructors.size() > 1) {
      throw new MultiInjectAnnotationFoundException();
    }
    return Injectable.of((Constructor<T>) allConstructors.stream().findFirst()
                                                         .orElseGet(() -> getDefaultConstructor(instanceType)));
  }

  @Override
  public T get(Container container) {
    try {
      T t = injectConstructor.element().newInstance(injectConstructor.getDependencies(container));
      for (Injectable<Field> i : injectFields) {
        Field f = i.element();
        f.setAccessible(true);
        f.set(t, i.getDependencies(container)[0]);
      }
      for (Injectable<Method> i : injectMethods) {
        Method m = i.element();
        m.setAccessible(true);
        m.invoke(t, i.getDependencies(container));
      }
      return t;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<InstanceTypeRef> dependencies() {
    return Stream.of(stream(injectConstructor.required()),
                     injectFields.stream().map(Injectable::required).flatMap(Arrays::stream),
                     injectMethods.stream().map(Injectable::required).flatMap(Arrays::stream))
                 .flatMap(Function.identity())
                 .distinct()
                 .toList();
  }

  private void injectedFieldShouldNotBeFinal() {
    injectFields.stream().map(Injectable::element).filter(f -> Modifier.isFinal(f.getModifiers())).findAny()
                .ifPresent(f -> {
                  throw new IllegalComponentException(
                      "Field '" + f.getName() + "' is failed to inject because it is final");
                });
  }

  private void injectedMethodShouldNotHasTypeParameter() {
    injectMethods.stream().map(Injectable::element).filter(m -> m.getTypeParameters().length > 0).findAny()
                 .ifPresent(m -> {
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
      System.out.println("----start");
      System.out.println(instanceType.getName());
      System.out.println(method.getName());
      System.out.println(stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining("    ")));
      System.out.println(currentType.getName());
      System.out.println("----end");
      Method instanceMethod = instanceType.getDeclaredMethod(method.getName(), method.getParameterTypes());
      return instanceMethod.getDeclaringClass() != currentType;
    } catch (NoSuchMethodException e) {
//      return true;
      throw new RuntimeException(e);
    }
  }

  private static boolean exists(List<Method> results, Method method) {
    return results.stream().anyMatch(m -> m.getName().equals(method.getName())
        && Arrays.equals(m.getParameterTypes(), method.getParameterTypes()));
  }

  private static <T extends AnnotatedElement> Stream<T> injectable(T[] elements) {
    return stream(elements).filter(f -> f.isAnnotationPresent(Inject.class));
  }

  private static <I> Constructor<I> getDefaultConstructor(Class<I> instanceType) {
    try {
      return instanceType.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      throw new IllegalComponentException(e);
    }
  }

  private static <T> void instanceTypeShouldBeInstantiable(Class<T> instanceType) {
    // instance type should not be an interface or an abstract class
    if (Modifier.isAbstract(instanceType.getModifiers())) {
      throw new IllegalComponentException(
          "Class '" + instanceType.getName() + "' is failed to inject because it is abstract");
    }
  }
}

package io.github.wynn5a.di;

import static java.util.Arrays.stream;

import io.github.wynn5a.di.exception.IllegalComponentException;
import jakarta.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wynn5a
 * @date 2022/7/26
 */
public record Injectable<E extends AccessibleObject>(E element, InstanceTypeRef[] required) {

  static <E extends Executable> Injectable<E> of(E constructor) {
    return new Injectable<>(constructor, stream(constructor.getParameters()).map(Injectable::toInstanceTypeRef)
                                                                            .toArray(InstanceTypeRef[]::new));
  }
  public static Injectable<Field> of(Field field) {
    return new Injectable<>(field, new InstanceTypeRef[]{toInstanceTypeRef(field)});
  }

  private static InstanceTypeRef toInstanceTypeRef(Parameter p) {
    return InstanceTypeRef.of(p.getParameterizedType(), getQualifier(p));
  }

  private static InstanceTypeRef toInstanceTypeRef(Field f) {
    return InstanceTypeRef.of(f.getGenericType(), getQualifier(f));
  }

  static Annotation getQualifier(AnnotatedElement annotated) {
    List<Annotation> annotations = stream(annotated.getAnnotations())
        .filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).toList();
    if (annotations.size() == 0) {
      return null;
    }
    if (annotations.size() > 1) {
      String annotationString = annotations.stream().map(Annotation::toString).collect(Collectors.joining(", "));
      throw new IllegalComponentException("multi qualifier found in component: " + annotationString);
    }
    return annotations.get(0);
  }

  Object[] getDependencies(Container container) {
    return Arrays.stream(required).map(r -> container.get(r)).map(Optional::get).toArray();
  }
}

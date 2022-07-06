package io.github.wynn5a.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author wynn5a
 * @date 2022/6/8
 */
public class InstanceTypeRef<T> {

  private Type containerType;

  private InstanceType instanceType;

  private InstanceTypeRef(ParameterizedType type) {
    init(type, null);
  }

  private InstanceTypeRef(Class<T> type) {
    init(type, null);
  }

  public InstanceTypeRef() {
    Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    init(type, null);
  }

  public <E> InstanceTypeRef(Class<E> type, Annotation qualifier) {
    init(type, qualifier);
  }

  private void init(Type type, Annotation qualifier) {
    if (type instanceof ParameterizedType) {
      this.containerType = ((ParameterizedType) type).getRawType();
      this.instanceType = new InstanceType((Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0], qualifier);
    } else {
      this.containerType = null;
      this.instanceType = new InstanceType((Class<T>) type, qualifier);
    }
  }

  public static InstanceTypeRef of(Type type) {
    if (type instanceof ParameterizedType) {
      return new InstanceTypeRef<>((ParameterizedType) type);
    }
    return new InstanceTypeRef<>((Class<?>) type);
  }

  public static <T> InstanceTypeRef<T> of(Class<T> type, Annotation qualifier) {
    return new InstanceTypeRef<>(type, qualifier);
  }

  public Type getContainerType() {
    return containerType;
  }

  public boolean isContainerType() {
    return containerType != null;
  }

  public InstanceType instanceType() {
    return this.instanceType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InstanceTypeRef<?> instanceTypeRef = (InstanceTypeRef<?>) o;
    return Objects.equals(containerType, instanceTypeRef.containerType) && instanceType.equals(instanceTypeRef.instanceType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(containerType, instanceType);
  }
}

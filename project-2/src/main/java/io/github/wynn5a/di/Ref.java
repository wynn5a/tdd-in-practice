package io.github.wynn5a.di;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author wynn5a
 * @date 2022/6/8
 */
public class Ref {

  private final Class<?> componentType;
  private final Type containerType;

  private Ref(ParameterizedType type) {
    this.componentType = (Class<?>) type.getActualTypeArguments()[0];
    this.containerType = type.getRawType();
  }

  private Ref(Class<?> type) {
    this.componentType = type;
    this.containerType = null;
  }

  public static Ref of(Type type) {
    if (type instanceof ParameterizedType) {
      return new Ref((ParameterizedType) type);
    }
    return new Ref((Class<?>) type);
  }

  public Class<?> getComponentType() {
    return componentType;
  }

  public Type getContainerType() {
    return containerType;
  }

  public boolean isContainerType() {
    return containerType != null;
  }
}

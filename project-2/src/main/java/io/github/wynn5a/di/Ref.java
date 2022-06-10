package io.github.wynn5a.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author wynn5a
 * @date 2022/6/8
 */
public class Ref<T> {

  private Annotation qualifier;
  private Class<T> componentType;
  private Type containerType;

  private Ref(ParameterizedType type) {
    init(type);
  }

  private Ref(Class<T> type) {
    init(type);
  }

  public Ref(){
    Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    init(type);
  }

  public <T> Ref(Class<T> type, Annotation qualifier) {
    init(type);
    this.qualifier = qualifier;
  }

  private void init(Type type) {
    if(type instanceof ParameterizedType){
      this.componentType = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
      this.containerType = ((ParameterizedType) type).getRawType();
    }else{
      this.componentType = (Class<T>) type;
      this.containerType = null;
    }
  }

  public static Ref of(Type type) {
    if (type instanceof ParameterizedType) {
      return new Ref<>((ParameterizedType) type);
    }
    return new Ref<>((Class<?>) type);
  }

  public static <T> Ref<T> of(Class<T> type, Annotation qualifier) {
    return new Ref<>(type, qualifier);
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

  public Annotation getQualifier() {
    return qualifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Ref ref = (Ref) o;

    if (!componentType.equals(ref.componentType)) {
      return false;
    }
    return Objects.equals(containerType, ref.containerType);
  }

  @Override
  public int hashCode() {
    int result = componentType.hashCode();
    result = 31 * result + (containerType != null ? containerType.hashCode() : 0);
    return result;
  }
}

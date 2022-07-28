package io.github.wynn5a.di;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.inject.Named;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wynn5a
 * @date 2022/7/9
 */
record NamedQualifier(String value) implements Named {


  @Override
  public Class<? extends Annotation> annotationType() {
    return jakarta.inject.Named.class;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof Named named) {
      return named.value().equals(value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return "value".hashCode() * 127 ^ value.hashCode();
  }
}

record SingletonLiteral() implements Singleton {

  @Override
  public Class<? extends Annotation> annotationType() {
    return Singleton.class;
  }
}

@Scope
@Documented
@Retention(RUNTIME)
@interface Pooled {

}


record PooledLiteral() implements Pooled {

  @Override
  public Class<? extends Annotation> annotationType() {
    return Pooled.class;
  }
}

final class PooledInstanceSupplier<T> implements InstanceSupplier<T> {

  private final InstanceSupplier<T> supplier;
  public static final int MAX = 2;
  private int current;
  private final List<T> pool = new ArrayList<>();

  PooledInstanceSupplier(InstanceSupplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get(Container container) {
    if (pool.size() < MAX) {
      pool.add(supplier.get(container));
    }
    return pool.get(current++ % MAX);
  }

  @Override
  public List<InstanceTypeRef> dependencies() {
    return InstanceSupplier.super.dependencies();
  }
}
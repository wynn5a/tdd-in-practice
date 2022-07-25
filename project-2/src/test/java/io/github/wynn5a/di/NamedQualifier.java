package io.github.wynn5a.di;

import jakarta.inject.Named;
import java.lang.annotation.Annotation;

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

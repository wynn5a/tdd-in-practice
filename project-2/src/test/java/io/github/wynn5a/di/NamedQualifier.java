package io.github.wynn5a.di;

import jakarta.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * @author wynn5a
 * @date 2022/7/9
 */
class NamedQualifier implements Annotation {

  private final String qualifier;

  public NamedQualifier(String qualifier) {
    this.qualifier = qualifier;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return jakarta.inject.Named.class;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if(o instanceof Named named){
      return named.value().equals(qualifier);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(qualifier);
  }
}

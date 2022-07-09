package io.github.wynn5a.di;

import java.lang.annotation.Annotation;

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
}

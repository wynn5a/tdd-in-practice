package io.github.wynn5a.di.exception;

/**
 * @author wynn5a
 * @date 2022/5/13
 */
public class CyclicDependencyFoundException extends RuntimeException {
  private final Class<?> component;

  public CyclicDependencyFoundException(Class<?> component) {
    this.component = component;
  }

  public Class<?> getComponent() {
    return component;
  }
}

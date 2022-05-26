package io.github.wynn5a.di.exception;

/**
 * @author wynn5a
 * @date 2022/5/12
 */
public class DependencyNotFoundException extends RuntimeException {

  private final Class<?> dependency;
  private final Class<?> component;


  public DependencyNotFoundException(Class<?> component, Class<?> dependency) {
    this.dependency = dependency;
    this.component = component;
  }

  public Class<?> getDependency() {
    return dependency;
  }

  public Class<?> getComponent() {
    return component;
  }
}

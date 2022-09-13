package io.github.wynn5a.di.exception;

import io.github.wynn5a.di.InstanceType;
import java.util.StringJoiner;

/**
 * @author wynn5a
 * @date 2022/5/12
 */
public class DependencyNotFoundException extends RuntimeException {

  private final InstanceType dependency;
  private final InstanceType component;


  public DependencyNotFoundException(InstanceType component, InstanceType dependency) {
    this.dependency = dependency;
    this.component = component;
  }

  public InstanceType getDependency() {
    return dependency;
  }

  public InstanceType getComponent() {
    return component;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DependencyNotFoundException.class.getSimpleName() + "[", "]")
        .add("dependency=" + dependency)
        .add("component=" + component)
        .toString();
  }
}

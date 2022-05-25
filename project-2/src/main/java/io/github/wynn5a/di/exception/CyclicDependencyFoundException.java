package io.github.wynn5a.di.exception;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author wynn5a
 * @date 2022/5/13
 */
public class CyclicDependencyFoundException extends RuntimeException {
  private Set<Class<?>> dependencies = new HashSet<>();

  public CyclicDependencyFoundException(Set<Class<?>> dependencies) {
    this.dependencies = dependencies;
  }

  public CyclicDependencyFoundException(Class<?> dependency, CyclicDependencyFoundException e) {
    this.dependencies = e.getDependencies();
    this.dependencies.add(dependency);
  }

  public CyclicDependencyFoundException(Stack<Class<?>> dependencies) {
    this.dependencies.addAll(dependencies);
  }

  public <T> CyclicDependencyFoundException(Class<T> dependency) {
    this.dependencies.add(dependency);
  }

  public Set<Class<?>> getDependencies() {
    return dependencies;
  }
}

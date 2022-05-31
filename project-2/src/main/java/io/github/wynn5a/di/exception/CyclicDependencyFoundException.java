package io.github.wynn5a.di.exception;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author wynn5a
 * @date 2022/5/13
 */
public class CyclicDependencyFoundException extends RuntimeException {
  private final Set<Class<?>> dependencies = new HashSet<>();

  public CyclicDependencyFoundException(Stack<Class<?>> dependencies) {
    this.dependencies.addAll(dependencies);
  }

  public Set<Class<?>> getDependencies() {
    return dependencies;
  }
}

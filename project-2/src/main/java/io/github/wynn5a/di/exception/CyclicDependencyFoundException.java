package io.github.wynn5a.di.exception;

import io.github.wynn5a.di.InstanceType;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @author wynn5a
 * @date 2022/5/13
 */
public class CyclicDependencyFoundException extends RuntimeException {
  private final Set<Class<?>> dependencies = new HashSet<>();

  public CyclicDependencyFoundException(Stack<InstanceType> dependencies) {
    this.dependencies.addAll(dependencies.stream().map(InstanceType::type).collect(Collectors.toSet()));
  }

  public Set<Class<?>> getDependencies() {
    return dependencies;
  }
}

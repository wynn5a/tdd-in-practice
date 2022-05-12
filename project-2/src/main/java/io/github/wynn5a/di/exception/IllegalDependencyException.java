package io.github.wynn5a.di.exception;

/**
 * @author wynn5a
 * @date 2022/5/12
 */
public class IllegalDependencyException extends RuntimeException {

  public IllegalDependencyException(String dependencyTypeName) {
    super(dependencyTypeName);
  }

}

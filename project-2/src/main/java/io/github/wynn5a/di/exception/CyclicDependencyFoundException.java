package io.github.wynn5a.di.exception;

/**
 * @author wynn5a
 * @date 2022/5/13
 */
public class CyclicDependencyFoundException extends RuntimeException {
  private final String dependencies;

  public CyclicDependencyFoundException(String dependencies) {
    this.dependencies = dependencies;
  }

  public CyclicDependencyFoundException(String dependencies, CyclicDependencyFoundException e) {
    this.dependencies  = dependencies + " -> " +  e.getDependencies();
  }

  public String getDependencies() {
    return dependencies;
  }
}

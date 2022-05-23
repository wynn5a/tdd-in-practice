package io.github.wynn5a.di.exception;

/**
 * @author wynn5a
 * @date 2022/5/13
 */
public class CyclicDependencyFoundException extends RuntimeException {


  public CyclicDependencyFoundException(String message) {
    super(message);
  }
}

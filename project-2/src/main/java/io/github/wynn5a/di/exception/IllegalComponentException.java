package io.github.wynn5a.di.exception;

/**
 * @author wynn5a
 * @date 2022/5/12
 */
public class IllegalComponentException extends RuntimeException {

  public IllegalComponentException(Throwable e) {
    super(e);
  }

  public IllegalComponentException(String message) {
    super(message);
  }
}

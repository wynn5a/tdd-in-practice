package io.github.wynn5a.exception;

/**
 * @author wynn5a
 * @date 2022/3/21
 */
public class InsufficientArgumentException extends RuntimeException{
  String option;

  public InsufficientArgumentException(String message) {
    super(message);
    this.option = message;
  }

  public String getOption() {
    return option;
  }
}

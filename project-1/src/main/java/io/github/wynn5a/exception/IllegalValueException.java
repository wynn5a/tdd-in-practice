package io.github.wynn5a.exception;

/**
 * @author wynn5a
 * @date 2022/3/23
 */
public class IllegalValueException extends RuntimeException{
  private String value;

  public IllegalValueException(String value, Throwable t) {
    super(t);
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

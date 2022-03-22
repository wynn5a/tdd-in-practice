package io.github.wynn5a.exception;

/**
 * @author wynn5a
 * @date 2022/3/18
 */
public class UnsupportedTypeException extends RuntimeException {
  private String type;
  public UnsupportedTypeException(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}

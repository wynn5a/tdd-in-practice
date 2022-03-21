package io.github.wynn5a.exception;

/**
 * @author wynn5a
 * @date 2022/3/21
 */
public class IllegalOptionException extends RuntimeException{
  String name;

  public IllegalOptionException(String message) {
    super(message);
    this.name = message;
  }

  public String getName() {
    return name;
  }
}

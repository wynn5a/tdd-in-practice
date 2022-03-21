package io.github.wynn5a.exception;


/**
 * @author wynn5a
 * @date 2022/3/21
 */
public class TooManyArgumentsException extends RuntimeException{

  private String option;
  public TooManyArgumentsException(String message) {
    super(message);
    this.option = message;
  }

  public String getOption() {
    return option;
  }
}

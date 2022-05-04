package io.github.wynn5a.di;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptionalTest {

  private String getString(){
    System.out.println("test");
    return "A";
  }

  @Test
  public void or_else_and_or_else_get(){
    Optional<String> stringOptional = Optional.of("b");
    String s = stringOptional.orElse(getString());
    System.out.println("---");
    String s2 = stringOptional.orElseGet(this::getString);
    Assertions.assertEquals(s, s2);
  }

}

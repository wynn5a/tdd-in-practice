import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.wynn5a.Option;
import io.github.wynn5a.SingleValueOptionParser;
import io.github.wynn5a.exception.InsufficientArgumentException;
import io.github.wynn5a.exception.TooManyArgumentsException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test cases for SingleValueOptionParser
 *
 * @author wynn5a
 * @date 2022/3/21
 */
public class SingleValueOptionParserTest {

  public static Option option(String value) {
    return new Option() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Option.class;
      }

      @Override
      public String value() {
        return value;
      }
    };
  }


  //happy path
  @Test
  public void should_get_parsed_value_from_parser() {
    var parsed = new Object();
    Function<String, Object> parser = i -> parsed;
    var defaultValue = "whatever";
    assertSame(parsed, new SingleValueOptionParser<>(defaultValue, parser).parse(List.of("-p", "8080"), option("p")));
  }

  @Test
  public void should_get_int_value_from_options() {
    Integer option = new SingleValueOptionParser<>(0, Integer::parseInt)
        .parse(List.of("-p", "8080"), option("p"));
    assertEquals(option, 8080);
  }

  @Test
  public void should_get_string_value_from_options() {
    String option = new SingleValueOptionParser<>("", Function.identity())
        .parse(List.of("-d", "/usr/logs"), option("d"));
    assertEquals(option, "/usr/logs");
  }

  //sad path
  @Test
  public void should_not_accept_extra_argument_for_int_option() {
    TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class,
        () -> new SingleValueOptionParser<>(0, Integer::parseInt).parse(List.of("-p", "8080", "8081"), option("p")));
    assertEquals("p", e.getOption());
  }

  @Test
  public void should_not_accept_extra_argument_for_string_option() {
    TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class,
        () -> new SingleValueOptionParser<>("", Function.identity())
            .parse(List.of("-d", "/usr/logs", "/var/logs"), option("d")));
    assertEquals("d", e.getOption());
  }

  @ParameterizedTest
  @ValueSource(strings = {"-d -l", "-d"})
  public void should_not_accept_insufficient_argument_for_single_value_option(String arg) {
    InsufficientArgumentException e = assertThrows(InsufficientArgumentException.class,
        () -> new SingleValueOptionParser<>("", Function.identity())
            .parse(Arrays.stream(arg.split(" ")).toList(), option("d")));
    assertEquals("d", e.getOption());
  }


  //default value
  @Test
  public void should_set_default_value_single_value_option() {
    var defaultValue = "whatever";
    Function<String, Object> whateverParser = i -> null;
    assertSame(defaultValue, new SingleValueOptionParser<>(defaultValue, whateverParser).parse(List.of(), option("p")));
  }
}

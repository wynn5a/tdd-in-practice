import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wynn5a.Args;
import io.github.wynn5a.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/3/17
 */
public class ArgsTest {

  //bool: -l
  @Test
  public void should_parse_boolean_option_to_true_if_flag_l_present() {
    BooleanOption option = Args.parse(BooleanOption.class, "-l");
    Assertions.assertTrue(option.logging());
  }

  @Test
  public void should_parse_boolean_option_to_false_if_flag_l_not_present() {
    BooleanOption option = Args.parse(BooleanOption.class, "-d");
    Assertions.assertFalse(option.logging());
  }

  public record BooleanOption(@Option("l") boolean logging) { }


  //int: -p
  @Test
  public void should_get_int_value_from_options() {
    IntegerOptions options = Args.parse(IntegerOptions.class, "-p", "8080");
    assertEquals(options.port(), 8080);
  }

  public record IntegerOptions(@Option("p") int port) { }

  //-d: string
  @Test
  public void should_get_string_value_from_options() {
    StringOptions options = Args.parse(StringOptions.class, "-d", "/usr/logs");
    assertEquals(options.directory(), "/usr/logs");
  }

  public record StringOptions(@Option("d") String directory) { }


  // -l -p 8080 -d /usr/logs
  @Test
  public void should_parse_multi_args() {
    Options options = Args.parse(Options.class, "-l", "-p", "8080", "-d", "/usr/logs");
    assertTrue(options.logging());
    assertEquals(options.port(), 8080);
    assertEquals(options.directory(), "/usr/logs");
  }

  public record Options(@Option("l") boolean logging, @Option("p") int port, @Option("d") String directory) { }
}

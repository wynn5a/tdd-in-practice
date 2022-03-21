import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.wynn5a.BooleanOptionParser;
import io.github.wynn5a.exception.TooManyArgumentsException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/3/21
 */
public class BooleanOptionParserTest {

  //bool: -l
  @Test
  public void should_parse_boolean_option_to_true_if_flag_present() {
    Assertions.assertTrue(new BooleanOptionParser().parse(List.of("-l"), SingleValueOptionParserTest.option("l")));
  }

  @Test
  public void should_set_default_value_if_flog_not_preset() {
    Assertions.assertFalse(new BooleanOptionParser().parse(List.of("-d"), SingleValueOptionParserTest.option("l")));
  }

  @Test
  public void should_not_accept_extra_argument_for_boolean_option() {
    TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class,
        () -> new BooleanOptionParser().parse(List.of("-l", "90"), SingleValueOptionParserTest.option("l")));
    assertEquals("l", e.getOption());
  }

}

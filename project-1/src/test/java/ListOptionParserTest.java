import static io.github.wynn5a.OptionParserFactory.list;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.wynn5a.exception.IllegalValueException;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/3/22
 */
public class ListOptionParserTest {

  //-g "this" "is" {"this", "is"}
  @Test
  public void should_parse_list_value() {
    String[] parsed = list(String[]::new, Function.identity())
        .parse(List.of("-g", "this", "is", "list"), SingleValueOptionParserTest.option("g"));
    assertArrayEquals(new String[]{"this", "is", "list"}, parsed);
  }

  //default value []
  @Test
  public void should_use_empty_array_as_default_value() {
    String[] parsed = list(String[]::new, Function.identity())
        .parse(List.of(), SingleValueOptionParserTest.option("g"));
    assertEquals(0, parsed.length);
  }

  //-d a throw exception
  @Test
  public void should_raise_exception_when_args_cannot_be_parsed() {
    IllegalValueException e = assertThrows(IllegalValueException.class, () ->
        list(Integer[]::new, Integer::parseInt)
            .parse(List.of("-d", "1", "a"), SingleValueOptionParserTest.option("d")));

    assertEquals("a", e.getValue());
  }

  // -d -3 -2
  @Test
  public void should_accept_negatives_as_value() {
    Integer[] parsed = list(Integer[]::new, Integer::parseInt).parse(List.of("-d", "1", "-2"), SingleValueOptionParserTest.option("d"));
    assertArrayEquals(new Integer[]{1,-2}, parsed);
  }
}

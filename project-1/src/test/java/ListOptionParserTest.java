import static io.github.wynn5a.OptionParserFactory.list;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/3/22
 */
public class ListOptionParserTest {

  // TODO: -g "this" "is" {"this", "is"}
  @Test
  public void should_parse_list_value() {
    String[] parsed = list(String[]::new, Function.identity())
        .parse(List.of("-g", "this", "is", "list"), SingleValueOptionParserTest.option("g"));
    assertArrayEquals(new String[]{"this", "is", "list"}, parsed);
  }

  // TODO: default value []
  @Test
  public void should_use_empty_array_as_default_value() {
    String[] parsed = list(String[]::new, Function.identity())
        .parse(List.of(), SingleValueOptionParserTest.option("g"));
    assertEquals(0, parsed.length);
  }
  // TODO: -d a throw exception
}

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wynn5a.Args;
import io.github.wynn5a.Option;
import io.github.wynn5a.exception.IllegalOptionException;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/3/17
 */
public class ArgsTest {

  // -l -p 8080 -d /usr/logs
  @Test
  public void should_parse_multi_args() {
    Options options = Args.parse(Options.class, "-l", "-p", "8080", "-d", "/usr/logs");
    assertTrue(options.logging());
    assertEquals(options.port(), 8080);
    assertEquals(options.directory(), "/usr/logs");
  }

  public record Options(@Option("l") boolean logging, @Option("p") int port, @Option("d") String directory) {
  }

  @Test
  public void should_throw_illegal_option_exception_when_annotation_not_present(){
    IllegalOptionException illegalOptionException = assertThrows(IllegalOptionException.class,
        () -> Args.parse(OptionWithoutAnnotation.class, "-l", "-p", "8080", "-d", "/usr/logs"));
    assertEquals("port", illegalOptionException.getName());
  }

  public record OptionWithoutAnnotation(@Option("l") boolean logging, int port, @Option("d") String directory){}

}

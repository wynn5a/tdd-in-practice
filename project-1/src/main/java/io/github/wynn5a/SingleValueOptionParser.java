package io.github.wynn5a;

import java.util.List;
import java.util.function.Function;

/**
 * @author wynn5a
 * @date 2022/3/18
 */
record SingleValueOptionParser<T>(Function<String, T> valueParser) implements OptionParser {

  @Override
  public Object parse(List<String> args, Option option) {
    String value = args.get(args.indexOf("-" + option.value()) + 1);
    return parseValue(valueParser, value);
  }

  private Object parseValue(Function<String, T> valueParser, String value) {
    return valueParser.apply(value);
  }
}

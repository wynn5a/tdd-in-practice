package io.github.wynn5a;

import io.github.wynn5a.exception.InsufficientArgumentException;
import io.github.wynn5a.exception.TooManyArgumentsException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author wynn5a
 * @date 2022/3/18
 */
public class OptionParserFactory {

  public static <T> OptionParser<T> unary(T defaultValue, Function<String, T> valueParser) {
    return (args, option) -> values(args, option, 1).map(i -> parseValue(valueParser, i.get(0))).orElse(defaultValue);
  }

  public static OptionParser<Boolean> bool() {
    return (args, option) -> values(args, option, 0).isPresent();
  }

  static Optional<List<String>> values(List<String> args, Option option, int expectedSize) {
    int index = args.indexOf("-" + option.value());
    if (index == -1) {
      return Optional.empty();
    }
    List<String> values = valuesFrom(args, index);
    if (values.size() < expectedSize) {
      throw new InsufficientArgumentException(option.value());
    }
    if (values.size() > expectedSize) {
      throw new TooManyArgumentsException(option.value());
    }

    return Optional.of(values);
  }

  public static List<String> valuesFrom(List<String> args, int index) {
    int followingFlagIndex = IntStream.range(index + 1, args.size())
                                      .filter(i -> args.get(i).startsWith("-"))
                                      .findFirst()
                                      .orElse(args.size());

    return args.subList(index + 1, followingFlagIndex);
  }

  private static <T> T parseValue(Function<String, T> valueParser, String value) {
    return valueParser.apply(value);
  }
}

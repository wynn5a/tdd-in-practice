package io.github.wynn5a;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import io.github.wynn5a.exception.IllegalValueException;
import io.github.wynn5a.exception.InsufficientArgumentException;
import io.github.wynn5a.exception.TooManyArgumentsException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
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

  public static <T> OptionParser<T[]> list(IntFunction<T[]> generator, Function<String, T> valueParser) {
    return (args, option) -> values(args, option)
        .map(i -> i.stream().map(value -> parseValue(valueParser, value)).toArray(generator))
        .orElse(generator.apply(0));
  }

  private static Optional<List<String>> values(List<String> args, Option option, int expectedSize) {
    int index = args.indexOf("-" + option.value());
    if (index == -1) {
      return empty();
    }
    List<String> values = valuesFrom(args, index);
    if (values.size() < expectedSize) {
      throw new InsufficientArgumentException(option.value());
    }
    if (values.size() > expectedSize) {
      throw new TooManyArgumentsException(option.value());
    }

    return of(values);
  }

  private static Optional<List<String>> values(List<String> args, Option option) {
    int index = args.indexOf("-" + option.value());
    return index == -1 ? empty() : of(valuesFrom(args, index));
  }

  private static List<String> valuesFrom(List<String> args, int index) {
    int followingFlagIndex = IntStream.range(index + 1, args.size())
                                      .filter(i -> args.get(i).matches("^-[a-zA-Z]+$"))
                                      .findFirst()
                                      .orElse(args.size());

    return args.subList(index + 1, followingFlagIndex);
  }

  private static <T> T parseValue(Function<String, T> valueParser, String value) {
    try {
      return valueParser.apply(value);
    } catch (Exception e) {
      throw new IllegalValueException(value, e);
    }
  }
}

package io.github.wynn5a;

import io.github.wynn5a.exception.InsufficientArgumentException;
import io.github.wynn5a.exception.TooManyArgumentsException;
import java.util.List;
import java.util.function.Function;

/**
 * @author wynn5a
 * @date 2022/3/18
 */
public record SingleValueOptionParser<T>( T defaultValue, Function<String, T> valueParser) implements OptionParser<T> {

  @Override
  public T parse(List<String> args, Option option) {
    int index = args.indexOf("-" + option.value());
    if(index == -1){
      return defaultValue;
    }

    if(index + 1 >= args.size() || args.get(index +1).startsWith("-")){
      throw new InsufficientArgumentException(option.value());
    }
    String value = args.get(index + 1);
    if(index + 2 < args.size() && !(args.get(index + 2).startsWith("-"))){
      throw new TooManyArgumentsException(option.value());
    }

    return parseValue(valueParser, value);
  }

  private T parseValue(Function<String, T> valueParser, String value) {
    return valueParser.apply(value);
  }
}

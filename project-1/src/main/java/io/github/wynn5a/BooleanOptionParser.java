package io.github.wynn5a;

import io.github.wynn5a.exception.TooManyArgumentsException;
import java.util.List;

/**
 * @author wynn5a
 * @date 2022/3/18
 */
public class BooleanOptionParser implements OptionParser<Boolean> {

  @Override
  public Boolean parse(List<String> args, Option option) {
    int index = args.indexOf("-" + option.value());
    if(index == -1){
      return false;
    }

    if(index + 1 < args.size() && !args.get(index +1).startsWith("-")){
      throw new TooManyArgumentsException(option.value());
    }

    return true;
  }
}

package io.github.wynn5a;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * @author wynn5a
 * @date 2022/3/17
 */
public class Args {

  public static <T> T parse(Class<T> optionsClass, String... args) {
    try {
      Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];
      Parameter[] parameters = constructor.getParameters();
      Object[] values = Arrays.stream(parameters).map(parameter -> {
        Option option = parameter.getDeclaredAnnotation(Option.class);
        String optionValue = option.value();
        Class<?> type = parameter.getType();
        String flag = "-" + optionValue;
        return parseOption(type, flag, args);
      }).toArray();
      return (T) constructor.newInstance(values);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object parseOption(Class<?> type, String flag, String[] args) {
    if (type == int.class) {
      int index = Arrays.asList(args).indexOf(flag);
      return Integer.parseInt(args[index + 1]);
    }

    if (type == boolean.class) {
      return Arrays.asList(args).contains(flag);
    }

    if (type == String.class) {
      int index = Arrays.asList(args).indexOf(flag);
      return args[index + 1];
    }
    return null;
  }
}

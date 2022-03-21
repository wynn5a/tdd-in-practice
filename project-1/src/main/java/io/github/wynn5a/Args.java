package io.github.wynn5a;

import io.github.wynn5a.exception.IllegalOptionException;
import io.github.wynn5a.exception.UnsupportedTypeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author wynn5a
 * @date 2022/3/17
 */
public class Args {

  public static <T> T parse(Class<T> aClass, String... args) {
    try {
      Constructor<?> constructor = aClass.getDeclaredConstructors()[0];
      Parameter[] parameters = constructor.getParameters();
      Object[] values = Arrays.stream(parameters).map(parameter -> parseOption(parameter, Arrays.asList(args))).toArray();
      return (T) constructor.newInstance(values);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object parseOption(Parameter parameter, List<String> args) {
    Option option = parameter.getDeclaredAnnotation(Option.class);
    if (option == null){
      throw new IllegalOptionException(parameter.getName());
    }
    return getOptionParser(parameter.getType()).parse(args, option);
  }

  private static final Map<Class<?>, OptionParser<?>> PARSERS = Map.of(
      int.class, new SingleValueOptionParser<>(0, Integer::parseInt),
      boolean.class, new BooleanOptionParser(),
      String.class, new SingleValueOptionParser<>("", Function.identity()));

  private static OptionParser getOptionParser(Class<?> type) {
    if (!PARSERS.containsKey(type)) {
      throw new UnsupportedTypeException(type.getCanonicalName());
    }
    return PARSERS.get(type);
  }

}

package io.github.wynn5a.di.exception;

import java.awt.Component;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.List;

/**
 * @author wynn5a
 * @date 2022/9/7
 */
public class ContainerConfigException extends RuntimeException {

  public static ContainerConfigException illegalAnnotation(Class<?> type, List<Annotation> annotations) {
    return new ContainerConfigException(MessageFormat.format("Unqualified annotations: {0} of {1}",
        String.join(" , ", annotations.stream()
                                      .map(Object::toString)
                                      .toList()), type));
  }

  static ContainerConfigException unknownScope(Class<? extends Annotation> annotationType) {
    return new ContainerConfigException(MessageFormat.format("Unknown scope: {0}", annotationType));
  }

  static ContainerConfigException duplicated(Component component) {
    return new ContainerConfigException(MessageFormat.format("Duplicated: {0}", component));
  }

  ContainerConfigException(String message) {
    super(message);
  }
}

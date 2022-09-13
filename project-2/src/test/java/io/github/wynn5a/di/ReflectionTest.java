package io.github.wynn5a.di;

import jakarta.inject.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/9/9
 */
public class ReflectionTest {

  @Test
  public void should_get_constructor(){
    for (Constructor<?> declaredConstructor : A.class.getDeclaredConstructors()) {
      System.out.println(declaredConstructor.getParameterCount());
      for (Parameter parameter : declaredConstructor.getParameters()) {
        System.out.println(parameter.getType());
      }
    }
  }

}

class A{
  Provider<Class> provider;
  A(Provider<Class> provider){
    this.provider = provider;
  }
}
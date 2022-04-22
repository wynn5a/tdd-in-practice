import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.lang.annotation.Annotation;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/4/22
 */
public class NamedInjectTest {

  interface Engine {

    String name();
  }

  interface Car {

    Engine getEngine();
  }

  static class CarWithNamedInject implements Car {

    @Inject
    @Named("V8")
    private Engine engine;

    @Override
    public Engine getEngine() {
      return engine;
    }
  }

  record StringNamedType(String value) implements Named {

    @Override
    public Class<? extends Annotation> annotationType() {
      return Named.class;
    }
  }

  static class EngineV8 implements Engine {

    @Override
    public String name() {
      return "V8";
    }
  }

  static class EngineV6 implements Engine {

    @Override
    public String name() {
      return "V6";
    }
  }

  @Test
  public void should_injected_by_named() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Engine.class).annotatedWith(new StringNamedType("V8")).to(EngineV8.class);
        bind(Car.class).to(CarWithNamedInject.class);
      }
    });

    Assertions.assertEquals(new EngineV8().name(), injector.getInstance(Car.class).getEngine().name());
  }

  @Test
  public void should_raise_exception_without_named_binding() {
    assertThrows(Exception.class, () -> Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Engine.class).annotatedWith(new StringNamedType("V6")).to(EngineV6.class);
        bind(Car.class).to(CarWithNamedInject.class);
      }
    }));
  }

}

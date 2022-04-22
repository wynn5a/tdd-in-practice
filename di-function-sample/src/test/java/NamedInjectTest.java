import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import model.Car;
import model.Engine;
import model.EngineV6;
import model.EngineV8;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/4/22
 */
public class NamedInjectTest {

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

  @Qualifier
  @Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Super {

  }

  record SuperEngine() implements Super {

    @Override
    public Class<? extends Annotation> annotationType() {
      return Super.class;
    }
  }

  static class SuperEngineCar implements Car {

    @Inject
    @Super
    Engine engine;

    @Override
    public Engine getEngine() {
      return engine;
    }
  }

  @Test
  public void should_inject_by_special_annotation() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Engine.class).annotatedWith(new SuperEngine()).to(EngineV8.class);
        bind(Car.class).to(SuperEngineCar.class);
      }
    });

    Car car = injector.getInstance(Car.class);
    assertEquals(new EngineV8().name(), car.getEngine().name());
  }

}

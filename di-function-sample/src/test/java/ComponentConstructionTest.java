import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/4/22
 */
public class ComponentConstructionTest {

  interface Car {

    Engine engine();
  }

  interface Engine {

    String name();
  }

  static class V8Engine implements Engine {

    @Override
    public String name() {
      return "V8";
    }
  }

  static class V6Engine implements Engine {

    @Override
    public String name() {
      return "V6";
    }
  }

  static final class CarInjectedByConstructor implements Car {

    private final Engine engine;

    @Inject
    CarInjectedByConstructor(Engine engine) {
      this.engine = engine;
    }

    public Engine engine() {
      return engine;
    }
  }

  static class CarInjectedByField implements Car {

    @Inject
    private Engine engine;

    @Override
    public Engine engine() {
      return engine;
    }
  }

  @Test
  public void inject_by_constructor() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Engine.class).to(V8Engine.class);
        bind(Car.class).to(CarInjectedByConstructor.class);
      }
    });

    Car car = injector.getInstance(Car.class);
    Assertions.assertEquals(new V8Engine().name(), car.engine().name());
  }

  @Test
  public void inject_by_field() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Engine.class).to(V6Engine.class);
        bind(Car.class).to(CarInjectedByField.class);
      }
    });

    Car car = injector.getInstance(Car.class);
    Assertions.assertEquals(new V6Engine().name(), car.engine().name());
  }


}

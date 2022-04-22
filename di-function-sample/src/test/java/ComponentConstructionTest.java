import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javax.inject.Inject;
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
public class ComponentConstructionTest {


  static final class CarInjectedByConstructor implements Car {

    private final Engine engine;

    @Inject
    CarInjectedByConstructor(Engine engine) {
      this.engine = engine;
    }

    public Engine getEngine() {
      return engine;
    }
  }

  static class CarInjectedByField implements Car {

    @Inject
    private Engine engine;

    @Override
    public Engine getEngine() {
      return engine;
    }
  }

  @Test
  public void inject_by_constructor() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Engine.class).to(EngineV8.class);
        bind(Car.class).to(CarInjectedByConstructor.class);
      }
    });

    Car car = injector.getInstance(Car.class);
    Assertions.assertEquals(new EngineV8().name(), car.getEngine().name());
  }

  @Test
  public void inject_by_field() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Engine.class).to(EngineV6.class);
        bind(Car.class).to(CarInjectedByField.class);
      }
    });

    Car car = injector.getInstance(Car.class);
    Assertions.assertEquals(new EngineV6().name(), car.getEngine().name());
  }
}

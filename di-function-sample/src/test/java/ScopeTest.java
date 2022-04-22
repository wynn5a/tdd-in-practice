import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.internal.SingletonScope;
import javax.inject.Inject;
import model.Car;
import model.Engine;
import model.EngineV8;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/4/22
 */
public class ScopeTest {

  static class MotorCar implements Car {

    @Inject
    private Engine engine;

    @Override
    public Engine getEngine() {
      return engine;
    }
  }


  @Test
  public void should_inject_with_scope() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Engine.class).to(EngineV8.class).in(new SingletonScope());
        bind(Car.class).to(MotorCar.class);
      }
    });

    Car car = injector.getInstance(Car.class);
    Car car2 = injector.getInstance(Car.class);
    assertNotSame(car, car2);
    assertSame(car.getEngine(), car2.getEngine());
  }

}

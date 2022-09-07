package io.github.wynn5a.di;

import jakarta.inject.Named;
import junit.framework.Test;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

/**
 * @author wynn5a
 * @date 2022/9/7
 */
public class JakartaInjectTest {

  public static Test suite() {
    ContainerConfig containerConfig = new ContainerConfig();
    containerConfig.from(new Config() {
      @Export(Car.class)
      Convertible car;

      @Drivers
      @Export(Seat.class)
      DriversSeat drivers;

      Seat seat;
      Tire tire;

      @Export(Engine.class)
      V8Engine engine;

      @Named("spare")
      @Export(Tire.class)
      SpareTire spare;

      FuelTank fuelTank;
      SpareTire spareTire;
      Cupholder cupholder;
    });

    Car car = containerConfig.getContainer().get(InstanceTypeRef.of(Car.class)).get();
    return Tck.testsFor(car, false, true);
  }
}

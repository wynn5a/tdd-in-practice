import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import javax.inject.Inject;
import javax.inject.Provider;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/4/22
 */
public class CyclicDependencyTest {

  static class A {

    @Inject
    A(B b) {
    }
  }

  static class B {

    @Inject
    B(A a) {
    }
  }

  @Test
  public void not_working() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(A.class);
        bind(B.class);
      }
    });

    assertThrows(ProvisionException.class, () -> injector.getInstance(A.class));
    assertThrows(Exception.class, () -> injector.getInstance(B.class));
  }


  static class A1 {

    private final Provider<B1> b1Provider;

    @Inject
    public A1(Provider<B1> b1Provider) {
      this.b1Provider = b1Provider;
    }

    public B1 getB() {
      return b1Provider.get();
    }
  }

  static class B1 {

    @Inject
    public B1(Provider<A1> a1Provider) {
      this.a1Provider = a1Provider;
    }

    private final Provider<A1> a1Provider;

    public A1 getA() {
      return a1Provider.get();
    }
  }

  @Test
  public void should_working() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(A1.class);
        bind(B1.class);
      }
    });

    assertDoesNotThrow(() -> injector.getInstance(A1.class));
    B1 instance = injector.getInstance(B1.class);
    assertNotNull(instance.getA());
  }
}

package io.github.wynn5a.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import jakarta.inject.Inject;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @author wynn5a
 * @date 2022/5/31
 */
public class InjectTest {

  ContainerConfig containerConfig;

  @BeforeEach
  public void setup() {
    containerConfig = new ContainerConfig();
  }

  //bind type to instance
  @Test
  public void should_bind_type_to_a_special_instance() {
    Component component = new Component() {
    };

    containerConfig.bind(Component.class, component);

    Component got = containerConfig.getContainer().get(Component.class).orElse(null);
    assertSame(component, got);
  }

  //sad path
  //component not bind
  @Test
  public void should_return_null_if_component_not_bind() {
    Optional<Component> componentOp = containerConfig.getContainer().get(Component.class);
    assertTrue(componentOp.isEmpty());
  }

  static abstract class AbstractComponent implements Component {

    @Inject
    public AbstractComponent() {
    }
  }

  //abstract component
  @Test
  public void should_raise_exception_when_abstract_component_bind() {
    assertThrows(IllegalComponentException.class, () -> containerConfig.bind(Component.class, AbstractComponent.class));
  }

  @Nested
  public class ConstructorInjectTest {

    //default constructor--no args constructor
    @Test
    public void should_bind_type_using_default_constructor() {
      containerConfig.bind(Component.class, SomeComponent.class);
      Component got = containerConfig.getContainer().get(Component.class).orElse(null);
      assertNotNull(got);
      assertTrue(got instanceof SomeComponent);
    }

    //constructor dependency
    @Test
    public void should_bind_component_with_constructor_dependency() {
      containerConfig.bind(Component.class, SomeComponentWithDependency.class);
      Dependency dependency = new Dependency() {
      };
      containerConfig.bind(Dependency.class, dependency);

      Component component = containerConfig.getContainer().get(Component.class).orElse(null);
      assertNotNull(component);
      Dependency got = ((SomeComponentWithDependency) component).getDependency();
      assertSame(dependency, got);
    }

    //a->b->c
    @Test
    public void should_bind_type_with_transitive_dependency() {
      containerConfig.bind(Component.class, SomeComponentWithDependency.class);
      containerConfig.bind(Dependency.class, DependencyInstanceWithDependency.class);
      containerConfig.bind(String.class, "Dependency");

      Component component = containerConfig.getContainer().get(Component.class).orElse(null);
      assertNotNull(component);
      Dependency dependency = ((SomeComponentWithDependency) component).getDependency();
      assertNotNull(dependency);
      String name = ((DependencyInstanceWithDependency) dependency).getName();
      assertEquals("Dependency", name);
    }

    //sad path
    //multi-inject-constructor
    @Test
    public void should_raise_exception_with_multi_injected_constructor() {
      assertThrows(MultiInjectAnnotationFoundException.class, () -> new InjectedInstanceSupplier<>(SomeComponentWithMultiInjected.class));
    }

    //no injected constructor nor default constructor
    @Test
    public void should_raise_exception_if_not_injected_constructor_nor_default_constructor() {
      assertThrows(IllegalComponentException.class, () -> new InjectedInstanceSupplier<>(SomeComponentCannotDecideConstructor.class));
    }

  }

  @Nested
  public class FieldInjectTest {

    @Test
    public void should_bind_type_using_field_inject() {
      containerConfig.bind(Component.class, ComponentWithFieldInject.class);
      DependencyInstance instance = new DependencyInstance();
      containerConfig.bind(Dependency.class, instance);
      Component component = containerConfig.getContainer().get(Component.class).orElse(null);
      assertNotNull(component);
      Dependency dependency = ((ComponentWithFieldInject) component).getDependency();
      assertNotNull(dependency);
      assertSame(instance, dependency);
    }

    @Test
    public void should_bind_type_using_field_inject_in_super_class() {
      containerConfig.bind(Component.class, SubClassOfComponentWithFieldInject.class);
      DependencyInstance instance = new DependencyInstance();
      containerConfig.bind(Dependency.class, instance);
      Component component = containerConfig.getContainer().get(Component.class).orElse(null);
      assertNotNull(component);
      Dependency dependency = ((SubClassOfComponentWithFieldInject) component).getDependency();
      assertNotNull(dependency);
      assertSame(instance, dependency);
    }

    @Test
    public void should_throw_exception_when_injected_field_is_final() {
      IllegalComponentException e = assertThrows(IllegalComponentException.class, () -> new InjectedInstanceSupplier<>(ComponentWithFinalFieldInject.class));
      assertEquals("Field 'dependency' is failed to inject because it is final", e.getMessage());
    }
  }

  @Nested
  public class MethodInjectTest {

    static class ComponentWithMethodInjectWithoutDependency implements Component {

      int called = 0;

      @Inject
      public void setDependency() {
        called++;
      }
    }

    @Test
    public void should_call_inject_method_even_if_no_dependency() {
      containerConfig.bind(Component.class, MethodInjectTest.ComponentWithMethodInjectWithoutDependency.class);
      Component component = containerConfig.getContainer().get(Component.class).orElse(null);
      assertNotNull(component);
      assertEquals(1, ((MethodInjectTest.ComponentWithMethodInjectWithoutDependency) component).called);
    }

    static class SubComponentWithMethodInject extends MethodInjectTest.ComponentWithMethodInjectWithoutDependency {

      int subCall = 0;

      @Inject
      public void setAnotherDependency() {
        subCall = called + 1;
      }
    }

    @Test
    public void should_inject_super_before_sub() {
      containerConfig.bind(MethodInjectTest.SubComponentWithMethodInject.class, MethodInjectTest.SubComponentWithMethodInject.class);
      MethodInjectTest.SubComponentWithMethodInject component = containerConfig.getContainer()
                                                                               .get(MethodInjectTest.SubComponentWithMethodInject.class)
                                                                               .orElse(null);
      assertNotNull(component);
      assertEquals(1, component.called);
      assertEquals(2, component.subCall);
    }

    static class SubWithMethodInjectOverride extends MethodInjectTest.ComponentWithMethodInjectWithoutDependency {

      @Inject
      @Override
      public void setDependency() {
        super.setDependency();
      }
    }

    @Test
    public void should_inject_only_once_if_sub_override_injected_method() {
      containerConfig.bind(MethodInjectTest.SubWithMethodInjectOverride.class, MethodInjectTest.SubWithMethodInjectOverride.class);
      MethodInjectTest.SubWithMethodInjectOverride component = containerConfig.getContainer()
                                                                              .get(MethodInjectTest.SubWithMethodInjectOverride.class)
                                                                              .orElse(null);
      assertNotNull(component);
      assertEquals(1, component.called);
    }

    static class SubWithNonInjectedOverrideMethod extends MethodInjectTest.ComponentWithMethodInjectWithoutDependency {

      @Override
      public void setDependency() {
        super.setDependency();
      }
    }

    @Test
    public void should_not_call_inject_method_if_overridden_by_non_inject_subclass() {
      containerConfig.bind(MethodInjectTest.SubWithNonInjectedOverrideMethod.class, MethodInjectTest.SubWithNonInjectedOverrideMethod.class);
      MethodInjectTest.SubWithNonInjectedOverrideMethod component = containerConfig.getContainer()
                                                                                   .get(MethodInjectTest.SubWithNonInjectedOverrideMethod.class)
                                                                                   .orElse(null);
      assertNotNull(component);
      assertEquals(0, component.called);
    }


    @Test
    public void should_bind_type_using_method_inject() {
      containerConfig.bind(Component.class, ComponentWithMethodInject.class);
      DependencyInstance instance = new DependencyInstance();
      containerConfig.bind(Dependency.class, instance);
      Component component = containerConfig.getContainer().get(Component.class).orElse(null);
      assertNotNull(component);
      Dependency dependency = ((ComponentWithMethodInject) component).getDependency();
      assertNotNull(dependency);
      assertSame(instance, dependency);
    }

    @Test
    public void should_bind_all_dependency_via_method_inject() {
      containerConfig.bind(Component.class, ComponentWithMultiInjectMethod.class);
      DependencyInstance instance = new DependencyInstance();
      containerConfig.bind(Dependency.class, instance);
      String any = "test";
      containerConfig.bind(String.class, any);
      Component component = containerConfig.getContainer().get(Component.class).orElse(null);
      assertNotNull(component);
      Dependency dependency = ((ComponentWithMultiInjectMethod) component).getDependency();
      assertNotNull(dependency);
      assertSame(instance, dependency);
      assertEquals(any, ((ComponentWithMultiInjectMethod) component).getName());
    }

    //inject method cannot declare any typed parameter
    static class TypedParameterInjectMethod {

      @Inject
      public <T> void setDependency() {
      }
    }

    @Test
    public void should_raise_exception_if_inject_method_has_typed_parameter() {
      assertThrows(IllegalComponentException.class, () -> new InjectedInstanceSupplier<>(MethodInjectTest.TypedParameterInjectMethod.class));
    }
  }
}

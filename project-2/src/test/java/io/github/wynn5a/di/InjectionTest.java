package io.github.wynn5a.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
public class InjectionTest {

  private final Dependency dependency = mock(Dependency.class);

  private final Container container = mock(Container.class);

  @BeforeEach
  public void setup() {
    when(container.get(Dependency.class)).thenReturn(Optional.of(dependency));
  }

  @Nested
  public class ConstructorInjectTest {

    @Nested
    class Injection {

      //default constructor--no args constructor
      @Test
      public void should_use_default_constructor_if_no_inject_constructor() {
        Component got = new InjectedInstanceSupplier<>(SomeComponent.class).get(container);
        assertNotNull(got);
      }

      //constructor dependency
      @Test
      public void should_inject_dependency_via_inject_constructor() {
        ComponentWithConstructorDependency component = new InjectedInstanceSupplier<>(ComponentWithConstructorDependency.class).get(container);
        assertNotNull(component);
        Dependency got = component.getDependency();
        assertSame(dependency, got);
      }
    }

    @Nested
    class InvalidConstructorInjection {

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

      //abstract component or interface
      @Test
      public void should_raise_exception_if_component_is_interface() {
        assertThrows(IllegalComponentException.class, () -> new InjectedInstanceSupplier<>(Component.class));
      }

      @Test
      public void should_raise_exception_if_component_is_abstract() {
        assertThrows(IllegalComponentException.class, () -> new InjectedInstanceSupplier<>(AbstractComponent.class));
      }
    }


  }

  @Nested
  public class FieldInjectTest {

    @Nested
    class Injection {

      @Test
      public void should_inject_dependency_via_injected_field() {
        ComponentWithFieldInject component = new InjectedInstanceSupplier<>(ComponentWithFieldInject.class).get(container);
        assertNotNull(component);
        Dependency dependencyGot = component.getDependency();
        assertSame(dependency, dependencyGot);
      }

      @Test
      public void should_inject_dependency_via_injected_field_in_super_class() {
        SubClassOfComponentWithFieldInject component = new InjectedInstanceSupplier<>(SubClassOfComponentWithFieldInject.class).get(container);
        assertNotNull(component);
        Dependency dependencyGot = component.getDependency();
        assertSame(dependency, dependencyGot);
      }
    }

    @Nested
    class InvalidFieldInjection {

      @Test
      public void should_throw_exception_if_injected_field_is_final() {
        IllegalComponentException e = assertThrows(IllegalComponentException.class, () -> new InjectedInstanceSupplier<>(ComponentWithFinalFieldInject.class));
        assertEquals("Field 'dependency' is failed to inject because it is final", e.getMessage());
      }
    }
  }

  @Nested
  public class MethodInjectTest {

    @Nested
    class Injection {

      @Test
      public void should_inject_dependency_via_injected_method() {
        ComponentWithMethodInject component = new InjectedInstanceSupplier<>(ComponentWithMethodInject.class).get(container);
        assertNotNull(component);
        Dependency dependencyGot = component.getDependency();
        assertSame(dependency, dependencyGot);
      }


      @Test
      public void should_call_inject_method_even_if_no_dependency() {
        ComponentWithMethodInjectWithoutDependency component = new InjectedInstanceSupplier<>(ComponentWithMethodInjectWithoutDependency.class).get(container);
        assertNotNull(component);
        assertEquals(1, component.called);
      }

      @Test
      public void should_call_super_injected_method_before_sub() {
        SubComponentWithMethodInject component = new InjectedInstanceSupplier<>(SubComponentWithMethodInject.class).get(container);
        assertNotNull(component);
        assertEquals(1, component.called);
        assertEquals(2, component.subCall);
      }

      @Test
      public void should_inject_only_once_if_sub_override_injected_method() {
        SubWithMethodInjectOverride component = new InjectedInstanceSupplier<>(SubWithMethodInjectOverride.class).get(container);
        assertNotNull(component);
        assertEquals(1, component.called);
      }

      @Test
      public void should_not_call_inject_method_if_overridden_by_non_inject_subclass() {
        SubWithNonInjectedOverrideMethod component = new InjectedInstanceSupplier<>(SubWithNonInjectedOverrideMethod.class).get(container);
        assertNotNull(component);
        assertEquals(0, component.called);
      }

      @Test
      public void should_inject_all_dependency_via_method_inject() {
        String anyString = "any";
        when(container.get(String.class)).thenReturn(Optional.of(anyString));

        ComponentWithMultiInjectMethod component = new InjectedInstanceSupplier<>(ComponentWithMultiInjectMethod.class).get(container);
        assertNotNull(component);
        Dependency dependencyGot = component.getDependency();
        assertSame(dependency, dependencyGot);
        assertEquals(anyString, component.getName());
      }

    }

    @Nested
    class InvalidMethodInjection {

      //inject method cannot declare any typed parameter
      static class TypedParameterInjectMethod {

        @Inject
        public <T> void setDependency() {
        }
      }

      @Test
      public void should_raise_exception_if_inject_method_has_typed_parameter() {
        assertThrows(IllegalComponentException.class, () -> new InjectedInstanceSupplier<>(TypedParameterInjectMethod.class));
      }
    }
  }
}
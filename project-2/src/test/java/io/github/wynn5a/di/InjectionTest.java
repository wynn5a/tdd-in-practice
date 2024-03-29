package io.github.wynn5a.di;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.wynn5a.di.ContainerTest.TypeBinding.Two;
import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author wynn5a
 * @date 2022/5/31
 */
public class InjectionTest {

  private final Dependency dependency = mock(Dependency.class);

  private final Supplier<Dependency> dependencySupplier = mock(Supplier.class);
  private final Provider<Dependency> dependencyProvider = mock(Provider.class);
  private ParameterizedType supplierType;
  private ParameterizedType providerType;

  @BeforeEach
  public void setup() throws NoSuchFieldException {
    supplierType = (ParameterizedType) InjectionTest.this.getClass()
                                                         .getDeclaredField("dependencySupplier")
                                                         .getGenericType();
    when(container.get(eq(InstanceTypeRef.of(supplierType)))).thenReturn(Optional.of(dependencySupplier));
    when(container.get(InstanceTypeRef.of(Dependency.class, null))).thenReturn(Optional.of(dependency));

    providerType = (ParameterizedType) InjectionTest.this.getClass()
                                                         .getDeclaredField("dependencyProvider")
                                                         .getGenericType();
    when(container.get(eq(InstanceTypeRef.of(providerType)))).thenReturn(Optional.of(dependencyProvider));
  }

  private final Container container = mock(Container.class);

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

      @Test
      public void should_inject_by_constructor_supplier() {
        ComponentWithSupplierConstructorDependency component = new InjectedInstanceSupplier<>(ComponentWithSupplierConstructorDependency.class).get(container);
        assertNotNull(component);
        Supplier<Dependency> got = component.getDependency();
        assertSame(dependencySupplier, got);
      }

      @Test
      public void should_inject_by_constructor_provider() {
        ComponentWithProviderDependency component = new InjectedInstanceSupplier<>(ComponentWithProviderDependency.class).get(container);
        assertNotNull(component);
        Provider<Dependency> got = component.dependencyProvider();
        assertSame(dependencyProvider, got);
      }

      @Test
      public void should_include_dependency_in_injected_constructor() {
        InjectedInstanceSupplier<ComponentWithConstructorDependency> supplier = new InjectedInstanceSupplier<>(ComponentWithConstructorDependency.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(Dependency.class, null)}, dependencies.toArray());
      }

      @Test
      public void should_include_supplier_dependency_in_injected_constructor() {
        InjectedInstanceSupplier<ComponentWithSupplierConstructorDependency> supplier = new InjectedInstanceSupplier<>(ComponentWithSupplierConstructorDependency.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(supplierType)}, dependencies.toArray());
      }

      @Test
      public void should_include_dependency_with_qualifier() {
        InjectedInstanceSupplier<ComponentWithQualifierConstructorInjectDependency> supplier = new InjectedInstanceSupplier<>(ComponentWithQualifierConstructorInjectDependency.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{
            InstanceTypeRef.of(Dependency.class, new NamedQualifier("one"))}, dependencies.toArray());
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


    @Nested
    class WithQualifier {

      @BeforeEach
      public void before() {
        Mockito.reset(container);
        when(container.get(InstanceTypeRef.of(Dependency.class, new NamedQualifier("one")))).thenReturn(Optional.ofNullable(dependency));
      }

      @Test
      public void should_get_dependencies_by_constructor_inject_with_qualifier() {
        InjectedInstanceSupplier<ComponentWithQualifierConstructorInjectDependency> supplier = new InjectedInstanceSupplier<>(ComponentWithQualifierConstructorInjectDependency.class);
        ComponentWithQualifierConstructorInjectDependency component = supplier.get(container);
        assertSame(dependency, component.getDependency());
      }

      @Test
      public void should_include_qualifier_when_get_dependencies_by_constructor_inject() {
        InjectedInstanceSupplier<ComponentWithQualifierConstructorInjectDependency> supplier = new InjectedInstanceSupplier<>(ComponentWithQualifierConstructorInjectDependency.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(Dependency.class, new NamedQualifier("one"))},
            dependencies.toArray()
        );
      }

      @Test
      public void should_throw_exception_if_multi_qualifier_provided_when_inject_by_constructor() {
        assertThrows(IllegalComponentException.class,
            () -> new InjectedInstanceSupplier<>(ComponentWithMultiQualifierMethodInject.class));
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

      @Test
      public void should_inject_dependency_via_provider_field() {
        ComponentWithSupplierFieldDependency component = new InjectedInstanceSupplier<>(ComponentWithSupplierFieldDependency.class).get(container);
        assertNotNull(component);
        Supplier<Dependency> dependencyGot = component.getDependency();
        assertSame(dependencySupplier, dependencyGot);
      }

      @Test
      public void should_include_dependency_in_injected_Field() {
        InjectedInstanceSupplier<ComponentWithFieldInject> supplier = new InjectedInstanceSupplier<>(ComponentWithFieldInject.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(Dependency.class, null)}, dependencies.toArray());
      }

      @Test
      public void should_include_supplier_dependency_in_injected_field() {
        InjectedInstanceSupplier<ComponentWithSupplierFieldDependency> supplier = new InjectedInstanceSupplier<>(ComponentWithSupplierFieldDependency.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(supplierType)}, dependencies.toArray());
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

    @Nested
    class WithQualifier {

      @BeforeEach
      public void before() {
        Mockito.reset(container);
        when(container.get(InstanceTypeRef.of(Dependency.class, new NamedQualifier("one")))).thenReturn(Optional.of(dependency));
      }

      @Test
      public void should_get_dependencies_by_field_inject_with_qualifier() {
        InjectedInstanceSupplier<ComponentWithQualifierFieldInject> supplier = new InjectedInstanceSupplier<>(ComponentWithQualifierFieldInject.class);
        ComponentWithQualifierFieldInject component = supplier.get(container);
        assertSame(dependency, component.getDependency());
      }

      @Test
      public void should_include_qualifier_when_get_dependencies_by_field_inject() {
        InjectedInstanceSupplier<ComponentWithQualifierFieldInject> supplier = new InjectedInstanceSupplier<>(ComponentWithQualifierFieldInject.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(Dependency.class, new NamedQualifier("one"))},
            dependencies.toArray()
        );
      }

      @Test
      public void should_throw_exception_if_multi_qualifier_provided_when_inject_by_field() {
        assertThrows(IllegalComponentException.class,
            () -> new InjectedInstanceSupplier<>(ComponentWithMultiQualifierFieldInject.class));
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
        when(container.get(InstanceTypeRef.of(String.class, null))).thenReturn(Optional.of(anyString));

        ComponentWithMultiInjectMethod component = new InjectedInstanceSupplier<>(ComponentWithMultiInjectMethod.class).get(container);
        assertNotNull(component);
        Dependency dependencyGot = component.getDependency();
        assertSame(dependency, dependencyGot);
        assertEquals(anyString, component.getName());
      }

      @Test
      public void should_inject_by_provider_method() {
        ComponentWithSupplierMethodDependency component = new InjectedInstanceSupplier<>(ComponentWithSupplierMethodDependency.class).get(container);
        assertNotNull(component);
        Supplier<Dependency> got = component.getDependency();
        assertSame(dependencySupplier, got);
      }

      @Test
      public void should_include_dependency_in_injected_method() {
        InjectedInstanceSupplier<ComponentWithMethodInject> supplier = new InjectedInstanceSupplier<>(ComponentWithMethodInject.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(Dependency.class, null)}, dependencies.toArray());
      }

      @Test
      public void should_include_supplier_dependency_in_injected_method() {
        InjectedInstanceSupplier<ComponentWithSupplierMethodDependency> supplier = new InjectedInstanceSupplier<>(ComponentWithSupplierMethodDependency.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(supplierType)}, dependencies.toArray());
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

    @Nested
    class WithQualifier {

      @BeforeEach
      public void before() {
        Mockito.reset(container);
        when(container.get(InstanceTypeRef.of(Dependency.class, new NamedQualifier("one")))).thenReturn(Optional.of(dependency));
      }

      @Test
      public void should_get_dependency_when_method_inject_with_qualifier() {
        InjectedInstanceSupplier<ComponentWithQualifierMethodInject> supplier = new InjectedInstanceSupplier<>(ComponentWithQualifierMethodInject.class);
        ComponentWithQualifierMethodInject component = supplier.get(container);
        assertSame(dependency, component.getDependency());
      }

      @Test
      public void should_include_qualifier_when_get_dependencies_by_method_inject() {
        InjectedInstanceSupplier<ComponentWithQualifierMethodInject> supplier = new InjectedInstanceSupplier<>(ComponentWithQualifierMethodInject.class);
        List<InstanceTypeRef> dependencies = supplier.dependencies();
        assertArrayEquals(new InstanceTypeRef[]{InstanceTypeRef.of(Dependency.class, new NamedQualifier("one"))},
            dependencies.toArray()
        );
      }

      @Test
      public void should_throw_exception_if_multi_qualifier_provided_when_inject_by_method() {
        assertThrows(IllegalComponentException.class,
            () -> new InjectedInstanceSupplier<>(ComponentWithMultiQualifierMethodInject.class));
      }
    }
  }
}

class ComponentWithMultiQualifierMethodInject {

  @Inject
  public void install(@Named("one") @Two Dependency dependency) {

  }
}

class ComponentWithProviderDependency{
  Provider<Dependency> dependencyProvider;
  @Inject
  ComponentWithProviderDependency(Provider<Dependency> dependencyProvider){
    this.dependencyProvider = dependencyProvider;
  }
  Provider<Dependency> dependencyProvider(){
    return dependencyProvider;
  }
}

class ComponentWithMultiQualifierFieldInject {

  @Inject
  @Named("one")
  @Two
  Dependency dependency;
}

class ComponentWithMultiQualifierConstructorInject {

  @Inject
  public ComponentWithMultiQualifierConstructorInject(@Named("one") @Two Dependency dependency) {

  }
}

package io.github.wynn5a.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ContainerTest {

  ContainerConfig containerConfig;

  @BeforeEach
  public void setup() {
    containerConfig = new ContainerConfig();
  }

  @AfterEach
  public void teardown() {
    containerConfig = null;
  }


  @Nested
  public class ComponentConstruction {

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
        assertThrows(MultiInjectAnnotationFoundException.class, () -> containerConfig.bind(Component.class, SomeComponentWithMultiInjected.class));
      }

      //no injected constructor nor default constructor
      @Test
      public void should_raise_exception_if_not_injected_constructor_nor_default_constructor() {
        assertThrows(IllegalComponentException.class, () -> containerConfig.bind(Component.class, SomeComponentCannotDecideConstructor.class));
      }

      //dependency not found
      // a -> b(x)
      @Test
      public void should_raise_exception_when_dependency_not_found_in_container() {
        containerConfig.bind(Component.class, SomeComponentWithDependency.class);
        DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> containerConfig.getContainer());
        assertEquals(Dependency.class, exception.getDependency());
        assertEquals(Component.class, exception.getComponent());
      }

      @Test //a->b->c(x)
      public void should_raise_exception_when_transitive_dependency_not_found_in_container() {
        containerConfig.bind(Component.class, SomeComponentWithDependency.class);
        containerConfig.bind(Dependency.class, DependencyDependedOnDependency.class);
        DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> containerConfig.getContainer());
        assertEquals(AnotherDependency.class, exception.getDependency());
        assertEquals(Dependency.class, exception.getComponent());
      }

      // cyclic dependency a->b->a
      @Test
      public void should_raise_exception_when_cyclic_dependency_found() {
        containerConfig.bind(Dependency.class, DependencyDependedOnComponent.class);
        containerConfig.bind(Component.class, SomeComponentWithCyclicDependency.class);
        CyclicDependencyFoundException exception = assertThrows(CyclicDependencyFoundException.class, () -> containerConfig.getContainer());
        Set<Class<?>> dependencies = exception.getDependencies();
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains(Dependency.class));
        assertTrue(dependencies.contains(Component.class));
      }

      @Test // a->b->c->a
      public void should_raise_exception_when_transitive_cyclic_dependency_found() {
        containerConfig.bind(Dependency.class, DependencyDependedOnDependency.class);
        containerConfig.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);
        containerConfig.bind(Component.class, SomeComponentWithCyclicDependency.class);

        CyclicDependencyFoundException exception = assertThrows(CyclicDependencyFoundException.class, () -> containerConfig.getContainer());
        Set<Class<?>> dependencies = exception.getDependencies();
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains(Component.class));
        assertTrue(dependencies.contains(Dependency.class));
        assertTrue(dependencies.contains(AnotherDependency.class));
      }

    }

    @Nested
    public class FieldInjectTest{
      @Test
      public void should_bind_type_using_field_inject(){
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
      public void should_throw_exception_when_injected_field_is_final(){
        IllegalComponentException e = assertThrows(IllegalComponentException.class, () -> containerConfig.bind(Component.class, ComponentWithFinalFieldInject.class));
        assertEquals("Field 'dependency' is failed to inject because it is final", e.getMessage());
      }

      @Test
      public void should_throw_exception_when_injected_dependency_not_found(){
        containerConfig.bind(Component.class, ComponentWithFieldInject.class);
        assertThrows(DependencyNotFoundException.class, () -> containerConfig.getContainer());
      }

      @Test
      public void should_throw_exception_when_cyclic_dependency_injected(){
        containerConfig.bind(Component.class, ComponentWithFieldInject.class);
        containerConfig.bind(Dependency.class, DependencyDependedOnComponentByField.class);
        CyclicDependencyFoundException e = assertThrows(CyclicDependencyFoundException.class, () -> containerConfig.getContainer());
        assertEquals(2, e.getDependencies().size());
        assertTrue(e.getDependencies().contains(Component.class));
        assertTrue(e.getDependencies().contains(Dependency.class));
      }

      @Test
      public void should_throw_exception_when_transitive_cyclic_dependency_injected(){
        containerConfig.bind(Component.class, ComponentWithFieldInject.class);
        containerConfig.bind(Dependency.class, DependencyDependedOnDependency.class);
        containerConfig.bind(AnotherDependency.class, DependencyDependedOnComponentByField.class);
        CyclicDependencyFoundException e = assertThrows(CyclicDependencyFoundException.class, () -> containerConfig.getContainer());
        assertEquals(3, e.getDependencies().size());
        assertTrue(e.getDependencies().contains(Component.class));
        assertTrue(e.getDependencies().contains(Dependency.class));
        assertTrue(e.getDependencies().contains(AnotherDependency.class));
      }
    }
  }


  @Nested
  public class DependenciesSelection {

  }

  @Nested
  public class LifecycleManagement {

  }
}



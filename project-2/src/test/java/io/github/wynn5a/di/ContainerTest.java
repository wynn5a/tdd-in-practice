package io.github.wynn5a.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wynn5a.di.exception.IllegalComponentException;
import io.github.wynn5a.di.exception.IllegalDependencyException;
import io.github.wynn5a.di.exception.MultiInjectAnnotationFoundException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ContainerTest {

  Container container;

  @BeforeEach
  public void setup() {
    container = new Container();
  }

  @AfterEach
  public void teardown() {
    container = null;
  }


  @Nested
  public class ComponentConstruction {

    //bind type to instance
    @Test
    public void should_bind_type_to_a_special_instance() {
      Component component = new Component() {
      };

      container.bind(Component.class, component);

      Component got = container.get(Component.class).orElse(null);
      assertSame(component, got);
    }

    //sad path
    //component not bind
    @Test
    public void should_return_null_if_component_not_bind() {
      Optional<Component> componentOp = container.get(Component.class);
      assertTrue(componentOp.isEmpty());
    }

    @Nested
    public class ConstructorInjectTest {

      //default constructor--no args constructor
      @Test
      public void should_bind_type_using_default_constructor() {
        container.bind(Component.class, SomeComponent.class);
        Component got = container.get(Component.class).orElse(null);
        assertNotNull(got);
        assertTrue(got instanceof SomeComponent);
      }

      //constructor dependency
      @Test
      public void should_bind_component_with_constructor_dependency() {
        container.bind(Component.class, SomeComponentWithDependency.class);
        Dependency dependency = new Dependency() {
        };
        container.bind(Dependency.class, dependency);

        Component component = container.get(Component.class).orElse(null);
        assertNotNull(component);
        Dependency got = ((SomeComponentWithDependency) component).getDependency();
        assertSame(dependency, got);
      }

      //a->b->c
      @Test
      public void should_bind_type_with_transitive_dependency() {
        container.bind(Component.class, SomeComponentWithDependency.class);
        container.bind(Dependency.class, DependencyInstanceWithDependency.class);
        container.bind(String.class, "Dependency");

        Component component = container.get(Component.class).orElse(null);
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
        assertThrows(MultiInjectAnnotationFoundException.class, () -> container.bind(Component.class, SomeComponentWithMultiInjected.class));
      }

      //no injected constructor nor default constructor
      @Test
      public void should_raise_exception_if_not_injected_constructor_nor_default_constructor() {
        assertThrows(IllegalComponentException.class, () -> container.bind(Component.class, SomeComponentCannotDecideConstructor.class));
      }

      //dependency not found
      @Test
      public void should_raise_exception_when_dependency_not_found_in_container() {
        container.bind(Component.class, SomeComponentWithDependency.class);
        IllegalDependencyException exception = assertThrows(IllegalDependencyException.class, () -> container.get(Component.class));
        assertEquals(Dependency.class.getName(), exception.getMessage());
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



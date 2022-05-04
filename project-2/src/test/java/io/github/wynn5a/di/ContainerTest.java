package io.github.wynn5a.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ContainerTest {

  Container container;

  @BeforeEach
  public void setup() {
    container = new Container();
  }


  @Nested
  public class ComponentConstruction {

    //bind type to instance
    @Test
    public void should_bind_type_to_a_special_instance() {
      Component component = new Component() {
      };

      container.bind(Component.class, component);

      Component got = container.get(Component.class);
      assertSame(component, got);
    }

    @Nested
    public class ConstructorInjectTest {

      //default constructor--no args constructor
      @Test
      public void should_bind_type_using_default_constructor() {
        container.bind(Component.class, SomeComponent.class);
        Component got = container.get(Component.class);
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

        Component component = container.get(Component.class);
        assertNotNull(component);
        Dependency got = ((SomeComponentWithDependency) component).getDependency();
        assertSame(dependency, got);
      }

      //a->b->c
      @Test
      public void should_bind_type_with_transitive_dependency(){
        container.bind(Component.class, SomeComponentWithDependency.class);
        container.bind(Dependency.class, DependencyInstanceWithDependency.class);
        container.bind(String.class, "Dependency");

        Component component = container.get(Component.class);
        assertNotNull(component);
        Dependency dependency = ((SomeComponentWithDependency) component).getDependency();
        assertNotNull(dependency);
        String name = ((DependencyInstanceWithDependency) dependency).getName();
        assertEquals("Dependency", name);
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



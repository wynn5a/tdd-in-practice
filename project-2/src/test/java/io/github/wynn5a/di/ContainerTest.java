package io.github.wynn5a.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试重构： 1. 设计决策变化的时候，需要重新设计测试用例 2. TDD 过程中的 TestCase 并不一定是良好的测试用例
 *
 * @author wynn5a
 */
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
  public class DependencyCheckTest {

    //dependency not found
    // a -> b(x)
    @Test
    public void should_raise_exception_when_dependency_not_found_in_container() {
      containerConfig.bind(Component.class, SomeComponentWithDependency.class);
      DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> containerConfig.getContainer());
      assertEquals(Dependency.class, exception.getDependency());
      assertEquals(Component.class, exception.getComponent());
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
  public class DependenciesSelection {

  }

  @Nested
  public class LifecycleManagement {

  }
}



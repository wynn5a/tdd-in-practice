package io.github.wynn5a.di;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wynn5a.di.exception.CyclicDependencyFoundException;
import io.github.wynn5a.di.exception.DependencyNotFoundException;
import io.github.wynn5a.di.exception.IllegalQualifierException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * 测试重构： <br /> 1. 设计决策变化的时候，需要重新设计测试用例 <br /> 2. TDD 过程中的 TestCase 并不一定是良好的测试用例 <br /> 3. 测试用例反应的更多是实现的过程，而不天然是文档 <br
 * /> 4. 测试用例需要经过刻意的整理和组织才能形成良好的文档 <br />
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

  public static Stream<Arguments> allKindComponent() {
    return Stream.of(
        Arguments.of(Named.of("Constructor", ComponentWithConstructorDependency.class)),
        Arguments.of(Named.of("Field", ComponentWithFieldInject.class)),
        Arguments.of(Named.of("Method", ComponentWithMethodInject.class)),
        Arguments.of(Named.of("MethodSupplier", ComponentWithSupplierMethodDependency.class)),
        Arguments.of(Named.of("FieldSupplier", ComponentWithSupplierFieldDependency.class)),
        Arguments.of(Named.of("ConstructorSupplier", ComponentWithSupplierConstructorDependency.class))
    );
  }

  public static Stream<Arguments> componentWithDependency() {
    return Stream.of(
        Arguments.of(Named.of("Constructor", ComponentWithConstructorDependency.class)),
        Arguments.of(Named.of("Field", ComponentWithFieldInject.class)),
        Arguments.of(Named.of("Method", ComponentWithMethodInject.class))
    );
  }

  @Nested
  public class DependencyCheck {

    //dependency not found
    // a -> b(x)
    @ParameterizedTest(name = "injected_by_{0}")
    @MethodSource("io.github.wynn5a.di.ContainerTest#allKindComponent")
    public void should_raise_exception_when_dependency_not_found_in_container(
        Class<? extends ComponentWithDependency> type) {
      containerConfig.bind(ComponentWithDependency.class, type);
      DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> containerConfig.getContainer());
      assertEquals(Dependency.class, exception.getDependency());
      assertEquals(ComponentWithDependency.class, exception.getComponent());
    }

    // cyclic dependency a->b->a
    @ParameterizedTest(name = "check cyclic dependency between injection by {0}, {1}")
    @MethodSource("cyclicDependency")
    public void should_raise_exception_when_cyclic_dependency_found(Class<? extends Component> component,
                                                                    Class<? extends Dependency> dependency) {
      containerConfig.bind(Dependency.class, dependency);
      containerConfig.bind(Component.class, component);
      CyclicDependencyFoundException exception = assertThrows(CyclicDependencyFoundException.class, () -> containerConfig.getContainer());
      Set<Class<?>> dependencies = exception.getDependencies();
      assertEquals(2, dependencies.size());
      assertTrue(dependencies.contains(Dependency.class));
      assertTrue(dependencies.contains(Component.class));
    }

    static Stream<Arguments> cyclicDependency() {
      List<Named<?>> components = List.of(Named.of("constructor", ComponentWithConstructorDependency.class),
          Named.of("field", ComponentWithFieldInject.class),
          Named.of("method", ComponentWithMethodInject.class)
      );

      List<Named<?>> dependencies = List.of(Named.of("constructor", DependencyDependedOnComponent.class),
          Named.of("field", DependencyDependedOnComponentByField.class),
          Named.of("method", DependencyDependedOnComponentByMethod.class)
      );

      List<Arguments> arguments = new ArrayList<>();
      for (var component : components) {
        for (var dependency : dependencies) {
          arguments.add(Arguments.of(component, dependency));
        }
      }
      return arguments.stream();
    }

    @ParameterizedTest(name = "check cyclic dependency between injection by {0}, {1}, {2}") // a->b->c->a
    @MethodSource("transitiveCyclicDependency")
    public void should_raise_exception_when_transitive_cyclic_dependency_found(Class<? extends Component> component,
                                                                               Class<? extends Dependency> dependency,
                                                                               Class<? extends AnotherDependency> anotherDependency) {
      containerConfig.bind(Dependency.class, dependency);
      containerConfig.bind(AnotherDependency.class, anotherDependency);
      containerConfig.bind(Component.class, component);

      CyclicDependencyFoundException exception = assertThrows(CyclicDependencyFoundException.class, () -> containerConfig.getContainer());
      Set<Class<?>> dependencies = exception.getDependencies();
      assertEquals(3, dependencies.size());
      assertTrue(dependencies.contains(Component.class));
      assertTrue(dependencies.contains(Dependency.class));
      assertTrue(dependencies.contains(AnotherDependency.class));
    }

    static Stream<Arguments> transitiveCyclicDependency() {
      List<Named<?>> components = List.of(Named.of("constructor", ComponentWithConstructorDependency.class),
          Named.of("field", ComponentWithFieldInject.class),
          Named.of("method", ComponentWithMethodInject.class)
      );

      List<Named<?>> anotherDependencies = List.of(Named.of("constructor", AnotherDependencyDependedOnComponent.class),
          Named.of("field", AnotherDependencyDependedOnComponentByField.class),
          Named.of("method", AnotherDependencyDependedOnComponentByMethod.class)
      );

      List<Named<?>> dependencies = List.of(Named.of("constructor", DependencyDependedOnDependency.class),
          Named.of("field", DependencyDependedOnDependencyByField.class),
          Named.of("method", DependencyDependedOnDependencyByMethod.class)
      );

      List<Arguments> arguments = new ArrayList<>();
      for (var component : components) {
        for (var dependency : dependencies) {
          for (var anotherDependency : anotherDependencies) {
            arguments.add(Arguments.of(component, dependency, anotherDependency));
          }
        }
      }
      return arguments.stream();
    }

    @ParameterizedTest(name = "supplier cyclic dependency between injection by {0}, {1}")
    @MethodSource("cyclicDependencyWithSupplier")
    public void should_not_throw_exception_when_cyclic_dependency_with_supplier(Class<? extends Component> component,
                                                                                Class<? extends Dependency> dependency) {
      containerConfig.bind(Dependency.class, dependency);
      containerConfig.bind(Component.class, component);
      Container container = containerConfig.getContainer();
      Optional<Component> optionalComponent = container.get(InstanceTypeRef.of(Component.class, null));
      assertTrue(optionalComponent.isPresent());
    }

    static Stream<Arguments> cyclicDependencyWithSupplier() {
      List<Named<?>> components = List.of(Named.of("constructor", ComponentWithSupplierConstructorDependency.class),
          Named.of("field", ComponentWithSupplierFieldDependency.class),
          Named.of("method", ComponentWithSupplierMethodDependency.class)
      );

      List<Named<?>> dependencies = List.of(Named.of("constructor", DependencyDependedOnComponentByConstructorSupplier.class),
          Named.of("field", DependencyDependedOnComponentByFieldSupplier.class),
          Named.of("method", DependencyDependedOnComponentByMethodSupplier.class)
      );

      List<Arguments> arguments = new ArrayList<>();
      for (var component : components) {
        for (var dependency : dependencies) {
          arguments.add(Arguments.of(component, dependency));
        }
      }
      return arguments.stream();
    }

    @Nested
    class WithQualifier{
      @Test
      public void should_raise_exception_if_no_qualified_dependency_found(){
        containerConfig.bind(Dependency.class, new Dependency() {
        });
        containerConfig.bind(ComponentWithDependency.class, ComponentWithQualifierConstructorInjectDependency.class);

        assertThrows(DependencyNotFoundException.class, ()->containerConfig.getContainer());
      }

      @Test
      public void should_raise_exception_if_wrong_qualified_dependency_found(){
        containerConfig.bind(Dependency.class, new Dependency() {
        }, new NamedQualifier("two"));
        containerConfig.bind(ComponentWithDependency.class, ComponentWithQualifierConstructorInjectDependency.class);

        assertThrows(DependencyNotFoundException.class, ()->containerConfig.getContainer());
      }
    }
  }

  @Nested
  public class TypeBinding {

    //bind type to instance
    @Test
    public void should_bind_type_to_a_special_instance() {
      Component component = new Component() {
      };
      containerConfig.bind(Component.class, component);
      Component got = containerConfig.getContainer().get(InstanceTypeRef.of(Component.class, null)).orElse(null);
      assertSame(component, got);
    }

    @java.lang.annotation.Documented
    @java.lang.annotation.Retention(RUNTIME)
    @jakarta.inject.Qualifier
    public @interface Two {
    }

    static class NamedQualifierTwo implements Two {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Two.class;
      }
    }

    @Test
    public void should_bind_type_to_a_special_instance_with_multi_qualifiers() {
      Component component = new Component() {
      };
      NamedQualifier one = new NamedQualifier("one");
      NamedQualifierTwo two = new NamedQualifierTwo();
      containerConfig.bind(Component.class, component, one, two);
      Component got = containerConfig.getContainer().get(InstanceTypeRef.of(Component.class, one)).orElse(null);
      Component gotTwo = containerConfig.getContainer().get(InstanceTypeRef.of(Component.class, two)).orElse(null);
      assertSame(component, got);
      assertSame(component, gotTwo);
    }

    @Test
    public void should_bind_type_to_a_instance_class_with_multi_qualifiers() {
      Dependency dependency = new Dependency() {
      };
      containerConfig.bind(Dependency.class, dependency);
      NamedQualifier one = new NamedQualifier("one");
      NamedQualifierTwo two = new NamedQualifierTwo();
      containerConfig.bind(ComponentWithDependency.class, ComponentWithConstructorDependency.class, one, two);
      ComponentWithDependency got = containerConfig.getContainer().get(InstanceTypeRef.of(ComponentWithDependency.class, one)).orElse(null);
      ComponentWithDependency gotTwo = containerConfig.getContainer().get(InstanceTypeRef.of(ComponentWithDependency.class, two)).orElse(null);
      assertSame(dependency, got.getDependency());
      assertSame(dependency, gotTwo.getDependency());
    }


    static class InvalidQualifier implements Annotation {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Test.class;
      }
    }

    @Test
    public void should_raise_exception_when_illegal_qualifier_provided_to_instance(){
      Dependency dependency = new Dependency() {
      };
      InvalidQualifier bad = new InvalidQualifier();
      assertThrows(IllegalQualifierException.class,()-> containerConfig.bind(Dependency.class, dependency, bad));
    }
    @Test
    public void should_raise_exception_when_illegal_qualifier_provided_to_type(){
      InvalidQualifier bad = new InvalidQualifier();
      assertThrows(IllegalQualifierException.class,()-> containerConfig.bind(ComponentWithDependency.class, ComponentWithConstructorDependency.class, bad));
    }

    @Test
    public void should_return_empty_if_component_not_bind() {
      Optional<Component> componentOp = containerConfig.getContainer().get(InstanceTypeRef.of(Component.class, null));
      assertTrue(componentOp.isEmpty());
    }

    @ParameterizedTest(name = "inject_by_{0}")
    @MethodSource("io.github.wynn5a.di.ContainerTest#componentWithDependency")
    public void should_bind_type_to_a_injectable_instance(Class<? extends ComponentWithDependency> componentClass) {
      containerConfig.bind(ComponentWithDependency.class, componentClass);
      containerConfig.bind(Dependency.class, DependencyInstance.class);
      Optional<ComponentWithDependency> got = containerConfig.getContainer().get(InstanceTypeRef.of(ComponentWithDependency.class, null));
      assertTrue(got.isPresent());
      assertNotNull(got.get().getDependency());
    }

    @Test
    public void should_retrieve_bind_type_as_provider() {
      Component instance = new Component() {
      };
      containerConfig.bind(Component.class, instance);
      Container container = containerConfig.getContainer();
      Supplier<Component> supplier = container.get(new InstanceTypeRef<Supplier<Component>>() {
      }).get();
      assertSame(instance, supplier.get());
    }

    @Test
    public void should_not_retrieve_bind_type_as_provider_if_container_type_is_unsupported() {
      Component instance = new Component() {
      };
      containerConfig.bind(Component.class, instance);
      Container container = containerConfig.getContainer();
      assertFalse(container.get(new InstanceTypeRef<List<Component>>() {}).isPresent());
    }

    @Test
    public void should_get_actual_generic_type_from_type_literal() {
      TypeLiteral<Supplier<Component>> literal = new TypeLiteral<>() {
      };
      ParameterizedType type = literal.getType();
      assertEquals(Supplier.class, type.getRawType());
      assertEquals(Component.class, type.getActualTypeArguments()[0]);
    }

    public static abstract class TypeLiteral<T> {

      ParameterizedType getType() {
        return (ParameterizedType) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
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



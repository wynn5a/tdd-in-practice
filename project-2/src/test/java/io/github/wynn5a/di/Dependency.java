package io.github.wynn5a.di;

import jakarta.inject.Inject;
import java.util.function.Supplier;

public interface Dependency {

}

class DependencyInstance implements Dependency {

}

class DependencyDependedOnComponent implements Dependency {

  @Inject
  public DependencyDependedOnComponent(Component component) {
  }
}

class DependencyDependedOnComponentByField implements Dependency {

  @Inject
  private Component component;
}

class DependencyDependedOnComponentByMethod implements Dependency {

  @Inject
  public void setComponent(Component component) {
  }
}

class DependencyDependedOnDependency implements Dependency {

  @Inject
  public DependencyDependedOnDependency(AnotherDependency dependency) {
  }
}

class DependencyDependedOnDependencyByField implements Dependency {

  @Inject
  private AnotherDependency dependency;
}

class DependencyDependedOnDependencyByMethod implements Dependency {

  @Inject
  public void setAnotherDependency(AnotherDependency dependency) {
  }
}

interface AnotherDependency {

}

class AnotherDependencyDependedOnComponent implements AnotherDependency {

  @Inject
  public AnotherDependencyDependedOnComponent(Component component) {
  }
}

class AnotherDependencyDependedOnComponentByField implements AnotherDependency {

  @Inject
  private Component component;
}

class AnotherDependencyDependedOnComponentByMethod implements AnotherDependency {

  @Inject
  public void setComponent(Component component) {
  }
}

class DependencyDependedOnComponentByConstructorSupplier implements Dependency {

  private final Supplier<Component> component;

  @Inject
  DependencyDependedOnComponentByConstructorSupplier(Supplier<Component> component) {
    this.component = component;
  }
}

class DependencyDependedOnComponentByFieldSupplier implements Dependency {

  @Inject
  private Supplier<Component> component;
}

class DependencyDependedOnComponentByMethodSupplier implements Dependency {

  @Inject
  public void install(Supplier<Component> component) {
  }
}

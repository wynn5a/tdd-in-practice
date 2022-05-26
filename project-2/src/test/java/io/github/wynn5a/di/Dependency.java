package io.github.wynn5a.di;

import jakarta.inject.Inject;

public interface Dependency {

}

class DependencyInstance implements Dependency{

}

class DependencyInstanceWithDependency implements Dependency{
  private final String name;

  @Inject
  public DependencyInstanceWithDependency(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

class DependencyDependedOnComponent implements Dependency{

  @Inject
  public DependencyDependedOnComponent(Component component) {
  }
}

class DependencyDependedOnDependency implements Dependency{
  @Inject
  public DependencyDependedOnDependency(AnotherDependency dependency) {
  }
}

interface AnotherDependency{

}

class AnotherDependencyDependedOnComponent implements AnotherDependency{
  @Inject
  public AnotherDependencyDependedOnComponent(Component component) {
  }
}

class DependencyDependedOnComponentByField implements Dependency, AnotherDependency{
  @Inject
  private Component component;
}

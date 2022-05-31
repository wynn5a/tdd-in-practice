package io.github.wynn5a.di;

import jakarta.inject.Inject;

public interface Dependency {

}

class DependencyInstance implements Dependency {

}

class DependencyDependedOnComponent implements Dependency {

  @Inject
  public DependencyDependedOnComponent(Component component) {
  }
}

class DependencyDependedOnDependency implements Dependency {

  @Inject
  public DependencyDependedOnDependency(AnotherDependency dependency) {
  }
}

interface AnotherDependency {

}

class AnotherDependencyDependedOnComponent implements AnotherDependency {

  @Inject
  public AnotherDependencyDependedOnComponent(Component component) {
  }
}


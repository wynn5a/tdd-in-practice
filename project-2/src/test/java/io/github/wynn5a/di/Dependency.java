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

class DependencyDependedOnComponentByField implements Dependency {

  @Inject
  private Component component;
}

class DependencyDependedOnComponentByMethod implements Dependency {

  @Inject
  public void setComponent(Component component){}
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
  public void setAnotherDependency(AnotherDependency dependency){}
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
  public void setComponent(Component component){}
}



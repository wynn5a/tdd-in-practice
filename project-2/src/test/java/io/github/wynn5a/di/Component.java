package io.github.wynn5a.di;

import jakarta.inject.Inject;

interface Component {

}

class SomeComponent implements Component {

  public SomeComponent() {
  }
}

class SomeComponentWithDependency implements Component {

  private final Dependency dependency;

  @Inject
  public SomeComponentWithDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  public Dependency getDependency() {
    return dependency;
  }
}

class SomeComponentWithMultiInjected implements Component {

  private final Dependency dependency;

  @Inject
  public SomeComponentWithMultiInjected(Dependency dependency) {
    this.dependency = dependency;
  }

  @Inject
  public SomeComponentWithMultiInjected(Dependency dependency, String name) {
    this.dependency = dependency;
  }
}

class SomeComponentCannotDecideConstructor implements Component {

  public SomeComponentCannotDecideConstructor(Dependency dependency) {
  }
}

class SomeComponentWithCyclicDependency implements Component {

  @Inject
  public SomeComponentWithCyclicDependency(Dependency dependency) {
  }
}

class ComponentWithFieldInject implements Component {

  @Inject
  private Dependency dependency;

  public Dependency getDependency() {
    return dependency;
  }
}

class ComponentWithFinalFieldInject implements Component {

  @Inject
  private final Dependency dependency = new DependencyInstance();
}

class SubClassOfComponentWithFieldInject extends ComponentWithFieldInject {

}


class ComponentWithMethodInject implements Component {

  private Dependency dependency;

  @Inject
  public void setDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  public Dependency getDependency() {
    return dependency;
  }
}

class SuperComponentWithMethodInject implements Component {

  @Inject
  public void setDependency(Dependency dependency) {
  }
}

class SubClassOfComponentWithMethodInject extends SuperComponentWithMethodInject {

  private Dependency dependency;

  @Override
  public void setDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  public Dependency getDependency() {
    return dependency;
  }
}

class ComponentWithMultiInjectMethod implements Component {

  private Dependency dependency;
  private String name;

  @Inject
  public void setDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  @Inject
  public void setName(String name) {
    this.name = name;
  }

  public Dependency getDependency() {
    return dependency;
  }

  public String getName() {
    return name;
  }
}
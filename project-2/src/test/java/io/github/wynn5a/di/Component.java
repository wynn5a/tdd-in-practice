package io.github.wynn5a.di;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.function.Supplier;

interface Component {

}

class SomeComponent implements Component {

}

interface ComponentWithDependency extends Component {

  Dependency getDependency();
}

class ComponentWithConstructorDependency implements ComponentWithDependency {

  private final Dependency dependency;

  @Inject
  public ComponentWithConstructorDependency(Dependency dependency) {
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

class ComponentWithFieldInject implements ComponentWithDependency {

  @Inject
  private Dependency dependency;

  public Dependency getDependency() {
    return dependency;
  }
}

class ComponentWithFinalFieldInject implements Component {

  @Inject
  private final Dependency dependency = null;
}

class SubClassOfComponentWithFieldInject extends ComponentWithFieldInject {

}


class ComponentWithMethodInject implements ComponentWithDependency {

  private Dependency dependency;

  @Inject
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

abstract class AbstractComponent implements Component {

  @Inject
  public AbstractComponent() {
  }
}

class ComponentWithMethodInjectWithoutDependency implements Component {

  int called = 0;

  @Inject
  public void setDependency() {
    called++;
  }
}

class SubComponentWithMethodInject extends ComponentWithMethodInjectWithoutDependency {

  int subCall = 0;

  @Inject
  public void setAnotherDependency() {
    subCall = called + 1;
  }
}

class SubWithMethodInjectOverride extends ComponentWithMethodInjectWithoutDependency {

  @Inject
  @Override
  public void setDependency() {
    super.setDependency();
  }
}

class SubWithNonInjectedOverrideMethod extends ComponentWithMethodInjectWithoutDependency {

  @Override
  public void setDependency() {
    super.setDependency();
  }
}

class ComponentWithSupplierConstructorDependency implements Component {

  private final Supplier<Dependency> dependency;

  @Inject
  ComponentWithSupplierConstructorDependency(Supplier<Dependency> dependency) {
    this.dependency = dependency;
  }

  public Supplier<Dependency> getDependency() {
    return dependency;
  }
}

class ComponentWithSupplierMethodDependency implements Component {

  private Supplier<Dependency> dependency;

  @Inject
  public void install(Supplier<Dependency> dependency) {
    this.dependency = dependency;
  }

  public Supplier<Dependency> getDependency() {
    return dependency;
  }
}

class ComponentWithSupplierFieldDependency implements Component {

  @Inject
  private Supplier<Dependency> dependency;

  public Supplier<Dependency> getDependency() {
    return dependency;
  }
}

class ComponentWithQualifierConstructorInjectDependency implements ComponentWithDependency {

  private final Dependency dependency;

  @Inject
  ComponentWithQualifierConstructorInjectDependency(@Named("one") Dependency dependency) {
    this.dependency = dependency;
  }

  @Override
  public Dependency getDependency() {
    return dependency;
  }
}

class ComponentWithQualifierMethodInject implements Component {

  @Inject
  public void setDependency(@Named("one") Dependency dependency) {

  }
}

class ComponentWithQualifierFieldInject implements Component {

  @Inject
  @Named("one")
  Dependency dependency;
}
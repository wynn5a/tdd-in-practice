package io.github.wynn5a.di;

import jakarta.inject.Inject;

interface Component {

}
class SomeComponent implements Component{

  public SomeComponent() {
  }
}

class SomeComponentWithDependency implements Component{
  private final Dependency dependency;

  @Inject
  public SomeComponentWithDependency(Dependency dependency){
    this.dependency = dependency;
  }

  public Dependency getDependency() {
    return dependency;
  }
}

class SomeComponentWithMultiInjected implements Component{
  private final Dependency dependency;

  @Inject
  public SomeComponentWithMultiInjected(Dependency dependency){
    this.dependency = dependency;
  }

  @Inject
  public SomeComponentWithMultiInjected(Dependency dependency, String name) {
    this.dependency = dependency;
  }
}
class SomeComponentCannotDecideConstructor implements Component{
  public SomeComponentCannotDecideConstructor(Dependency dependency){
  }
}

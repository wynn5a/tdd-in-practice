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

package io.github.wynn5a.di;

import java.util.List;

/**
 * @author wynn5a
 * @date 2022/7/28
 */
public class SingletonInstanceSupplier<T> implements InstanceSupplier<T> {

  private final InstanceSupplier<T> supplier;
  private T singleton;

  public SingletonInstanceSupplier(InstanceSupplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get(Container container) {
    if (singleton == null){
      singleton = supplier.get(container);
    }
    return singleton;
  }

  @Override
  public List<InstanceTypeRef> dependencies() {
    return supplier.dependencies();
  }
}

package io.github.wynn5a.di;

import static java.util.List.of;

import java.util.List;

/**
 * @author wynn5a
 * @date 2022/5/25
 */
public interface InstanceSupplier<T> {

  T get(Container container);

  default List<Ref> dependencies() {
    return of();
  }
}

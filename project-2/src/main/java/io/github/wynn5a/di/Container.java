package io.github.wynn5a.di;

import java.util.Optional;

/**
 * @author wynn5a
 * @date 2022/5/25
 */
public interface Container {

  <T> Optional<T> get(InstanceTypeRef<T> instanceTypeRef);
}

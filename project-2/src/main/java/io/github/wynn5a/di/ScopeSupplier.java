package io.github.wynn5a.di;

/**
 * @author wynn5a
 * @date 2022/8/8
 */
public interface ScopeSupplier {

  <I>InstanceSupplier<I> create(InstanceSupplier<I> supplier);
}

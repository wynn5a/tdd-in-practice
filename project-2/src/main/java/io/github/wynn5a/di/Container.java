package io.github.wynn5a.di;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author wynn5a
 * @date 2022/5/25
 */
public interface Container {

  Optional get(Type type);
}

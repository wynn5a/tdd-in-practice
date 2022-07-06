package io.github.wynn5a.di;

import java.lang.annotation.Annotation;

/**
 * @author wynn5a
 * @date 2022/7/6
 */
public record InstanceType(Class<?> type, Annotation qualifier) {

}

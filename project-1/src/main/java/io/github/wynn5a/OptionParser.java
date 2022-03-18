package io.github.wynn5a;

import java.util.List;

/**
 * @author wynn5a
 * @date 2022/3/18
 */
interface OptionParser {

  Object parse(List<String> args, Option option);
}

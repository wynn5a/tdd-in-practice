# 通过项目来进行 TDD 实战 
## project 1： 命令行参数解析
题目源自 Bob 大叔的 Clean Code 第十四章：

>我们中的大多数人都不得不时不时地解析一下命令行参数。  
>如果我们没有一个方便的工具，那么我们就简单地处理一下传入 main 函数的字符串数组。  
> 有很多开源工具可以完成这个任务，但它们可能并不能完全满足我们的要求。  
> 所以我们再写一个吧。　
> - 传递给程序的参数由标志和值组成。  
> - 标志应该是一个字符，前面有一个减号。
> - 每个标志都应该有零个或多个与之相关的值。
> 例如：　`-l -p 8080 -d /usr/logs`　“l”（日志）没有相关的值，它是一个布尔标志，如果存在则为 true，不存在则为 false。“p”（端口）有一个整数值，“d”（目录）有一个字符串值。
> - 标志后面如果存在多个值，则该标志表示一个列表：　`-g this is a list -d 1 2 -3 5`　"g"表示一个字符串列表[“this”, “is”, “a”, “list”]，“d"标志表示一个整数列表[1, 2, -3, 5]。　
> - 如果参数中没有指定某个标志，那么解析器应该指定一个默认值。 例如，false 代表布尔值，`0` 代表数字，`""`代表字符串，`[]`代表列表。
> - 如果给出的参数与模式不匹配，重要的是给出一个好的错误信息，准确地解释什么是错误的。　
> - 确保你的代码是可扩展的，即如何增加新的数值类型是直接和明显的。

### 步骤
1. 实现单个参数解析
2. 实现列表参数解析

## Project 2

第一个场景是支撑 RESTful API 的开发框架，

你可以将它想象成 mini 版本的 Dropwizard 或者 Spring MVC。

功能范围包含一个依赖注入容器（Dependency Injection Container/IoC Container）和一个支持 RESTful API 构建的 Web 框架。

我们会以 Jakarta EE 中的 Jakarta Dependency Injection 和 Jakarta RESTful Web Services 为主要功能参考，并对其适当简化，以完成我们的目标。

### 任务拆分

组件构造部分，分解的任务如下：

- 无需构造的组件——组件实例
- 如果注册的组件不可实例化，则抛出异常
  - 抽象类
  - 接口
- 构造函数注入
  - 无依赖的组件应该通过默认构造函数生成组件实例
  - 有依赖的组件，通过 Inject 标注的构造函数生成组件实例
  - 如果所依赖的组件也存在依赖，那么需要对所依赖的组件也完成依赖注入
  - 如果组件有多于一个 Inject 标注的构造函数，则抛出异常
  - 如果组件需要的依赖不存在，则抛出异常
  - 如果组件间存在循环依赖，则抛出异常
- 字段注入 
  - 通过 Inject 标注将字段声明为依赖组件
  - 如果组件需要的依赖不存在，则抛出异常
  - 如果字段为 final 则抛出异常
  - 如果组件间存在循环依赖，则抛出异常
- 方法注入
  - 通过 Inject 标注的方法，其参数为依赖组件
  - 通过 Inject 标注的无参数方法，会被调用按照子类中的规则，覆盖父类中的 Inject 方法
  - 如果组件需要的依赖不存在，则抛出异常
  - 如果方法定义类型参数，则抛出异常
  - 如果组件间存在循环依赖，则抛出异常

对于依赖选择部分，分解的任务列表如下：

- 对 Provider 类型的依赖
  - 注入构造函数中可以声明对于 Provider 的依赖
  - 注入字段中可以声明对于 Provider 的依赖
  - 注入方法中可声明对于 Provider 的依赖
- 自定义 Qualifier 的依赖
  - 注册组件时，可额外指定 Qualifier
  - 注册组件时，可从类对象上提取 Qualifier
  - 寻找依赖时，需同时满足类型与自定义 Qualifier 标注
  - 支持默认 Qualifier——Named

对于生命周期管理部分，分解的任务列表如下：

- Singleton 生命周期
  - 注册组件时，可额外指定是否为 Singleton
  - 注册组件时，可从类对象上提取 Singleton 标注
  - 对于包含 Singleton 标注的组件，在容器范围内提供唯一实例
  - 容器组件默认不是 Single 生命周期
- 自定义 Scope 标注
  - 可向容器注册自定义 Scope 标注的回调
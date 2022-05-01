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
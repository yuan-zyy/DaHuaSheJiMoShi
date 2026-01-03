# Java 生态中职责链模式实践案例剖析

你想了解Java生态中（JDK原生+主流三方框架）优秀的职责链模式实践案例，我会分别拆解JDK内置实现和三方框架实现，结合源码核心逻辑和设计思想进行详细说明：

## 一、 JDK 原生中的职责链模式案例

### 1.  经典案例：Java `Servlet` 规范中的 `Filter` 链（`javax.servlet.Filter`）

`Servlet Filter` 是JDK（Servlet规范）中职责链模式的典型落地，广泛用于Web请求的预处理/后处理，是最易理解的原生职责链实现。

#### 核心角色对应

|职责链模式角色|Filter 链对应实现|
|---|---|
|抽象处理者（Handler）|`javax.servlet.Filter` 接口|
|具体处理者（ConcreteHandler）|自定义Filter（如编码Filter、登录校验Filter）、框架内置Filter|
|请求对象（Request）|`ServletRequest`（HTTP请求封装）|
|链式管理对象|`FilterChain` 接口（维护Filter链，负责请求传递）|
#### 核心源码与执行逻辑

1. **抽象处理者（Filter接口）**：定义统一的处理方法

```java

public interface Filter {
    // 初始化方法（非核心，仅初始化资源）
    default void init(FilterConfig filterConfig) throws ServletException {}

    // 核心处理方法：处理请求，或传递给下一个Filter
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException;

    // 销毁方法（非核心，释放资源）
    default void destroy() {}
}
```

1. **链式管理（FilterChain接口）**：维护Filter链，提供请求转发能力

```java

public interface FilterChain {
    // 关键方法：将请求传递给链中的下一个Filter，若为最后一个则传递给Servlet
    void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException;
}
```

1. **执行流程（职责链核心体现）**

- 容器（如Tomcat）启动时，会根据`web.xml`或注解`@WebFilter`配置，将所有Filter按优先级排序，构建成一条`FilterChain`；

- 当HTTP请求到达时，容器调用链首Filter的`doFilter`方法；

- 具体Filter在`doFilter`中完成自身逻辑（如编码设置、XSS防护）后，主动调用 `chain.doFilter(request, response)` 将请求传递给下一个Filter；

- 若某个Filter不调用`chain.doFilter()`，则请求被拦截（中断链式传递）；

- 当所有Filter处理完毕后，请求最终传递给目标`Servlet`进行业务处理；

- 响应时，会按**反向顺序**再次经过Filter链（后处理，如响应数据封装）。

1. **代码示例（自定义Filter，体现职责链思想）**

```java

// 具体处理者1：编码过滤Filter
@WebFilter(urlPatterns = "/*")
public class CharsetFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 1. 自身处理逻辑：设置请求/响应编码
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        // 2. 传递给下一个Filter（不调用则请求中断）
        chain.doFilter(request, response);
    }
}

// 具体处理者2：登录校验Filter
@WebFilter(urlPatterns = "/api/*")
public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("token");
        
        // 1. 自身处理逻辑：校验登录状态
        if (token == null || !"valid_token".equals(token)) {
            response.getWriter().write("未登录，拒绝访问");
            return; // 不传递，中断请求
        }
        
        // 2. 校验通过，传递给下一个Filter/Servlet
        chain.doFilter(request, response);
    }
}
```

### 2.  隐性案例：Java 异常处理（`try-catch` 链式结构）

这是JDK中隐性的职责链模式实现，无需手动构建链条，由JVM自动维护异常传递链。

#### 核心角色对应与执行逻辑

- 抽象处理者：`Throwable`（所有异常的父类，定义异常的基础行为）；

- 具体处理者：各类异常捕获块（`catch (NullPointerException e)`、`catch (RuntimeException e)` 等）；

- 请求对象：抛出的异常实例（如`new NullPointerException("空指针")`）；

- 链式传递：异常抛出后，JVM会按`catch`块的**声明顺序**（子类异常在前、父类异常在后）依次匹配，找到第一个能处理该异常的`catch`块并执行，后续`catch`块不再生效；若未找到匹配的`catch`块，异常会向上传播给方法调用方，直至主线程终止。

#### 示例体现

```java

public void exceptionChainDemo() {
    try {
        String str = null;
        str.length(); // 抛出NullPointerException
    } catch (NullPointerException e) { // 具体处理者1：处理子类异常
        System.out.println("捕获空指针异常：" + e.getMessage());
    } catch (RuntimeException e) { // 具体处理者2：处理父类异常
        System.out.println("捕获运行时异常：" + e.getMessage());
    } catch (Exception e) { // 具体处理者3：处理顶级异常
        System.out.println("捕获通用异常：" + e.getMessage());
    }
    // 执行结果：仅第一个catch块生效，体现职责链的“匹配即终止”特性
}
```

## 二、 三方框架中的职责链模式案例

### 1.  Spring MVC：`HandlerInterceptor` 拦截器链

`Spring MVC` 的拦截器链是职责链模式的经典应用，用于对Controller请求进行前置/后置处理，功能类似Servlet Filter，但更贴近业务层。

#### 核心角色对应

|职责链模式角色|HandlerInterceptor 链对应实现|
|---|---|
|抽象处理者|`org.springframework.web.servlet.HandlerInterceptor` 接口|
|具体处理者|自定义Interceptor、Spring内置Interceptor（如LocaleChangeInterceptor）|
|请求对象|`HttpServletRequest` / `ModelAndView`|
|链式管理对象|`HandlerExecutionChain`（Spring内置，维护拦截器链）|
#### 核心源码与执行逻辑

1. **抽象处理者（HandlerInterceptor接口）**

```java

public interface HandlerInterceptor {
    // 前置处理：Controller执行前调用，返回false则中断请求
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return true;
    }

    // 后置处理：Controller执行后、视图渲染前调用
    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
    }

    // 完成处理：视图渲染后调用（用于释放资源等）
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) throws Exception {
    }
}
```

1. **链式管理（HandlerExecutionChain）**

Spring MVC在请求到达时，会通过`HandlerMapping`获取对应的`HandlerExecutionChain`（包含目标Controller和拦截器链），核心逻辑：

- 前置处理：按拦截器顺序调用`preHandle`，若某个拦截器返回`false`，则反向调用已执行拦截器的`afterCompletion`，中断请求；

- 后置处理：Controller执行成功后，按**反向顺序**调用拦截器的`postHandle`；

- 完成处理：视图渲染完成后，按**反向顺序**调用拦截器的`afterCompletion`。

1. **自定义示例（体现职责链思想）**

```java

// 自定义拦截器（具体处理者）
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 前置校验：权限判断
        String userId = request.getParameter("userId");
        if (userId == null || !"1001".equals(userId)) {
            response.getWriter().write("权限不足");
            return false; // 中断链式传递
        }
        return true; // 放行，传递给下一个拦截器
    }
}

// 配置拦截器链
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 构建拦截器链：AuthInterceptor → 其他拦截器（按添加顺序排序）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/user/**");
    }
}
```

### 2.  MyBatis：`Interceptor` 插件链

MyBatis的插件机制基于职责链模式实现，用于拦截MyBatis的核心执行流程（如SQL执行、参数处理、结果集处理等），是框架级职责链的优秀实践。

#### 核心角色对应

|职责链模式角色|MyBatis Interceptor 链对应实现|
|---|---|
|抽象处理者|`org.apache.ibatis.plugin.Interceptor` 接口|
|具体处理者|自定义插件（如分页插件、SQL打印插件）、MyBatis内置插件|
|请求对象|`Invocation`（封装拦截目标、参数、方法等）|
|链式管理对象|MyBatis内置的`InterceptorChain` 类|
#### 核心源码与执行逻辑

1. **抽象处理者（Interceptor接口）**

```java

public interface Interceptor {
    // 核心处理方法：拦截目标方法执行，或传递给下一个拦截器
    Object intercept(Invocation invocation) throws Throwable;

    // 包装目标对象（用于生成代理对象，构建链式调用）
    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    // 设置插件属性（非核心）
    default void setProperties(Properties properties) {
        // NOP
    }
}
```

1. **链式管理（InterceptorChain类）**

```java

public class InterceptorChain {
    // 存储所有注册的拦截器（按注册顺序组成链条）
    private final List<Interceptor> interceptors = new ArrayList<>();

    // 包装目标对象，构建链式代理
    public Object pluginAll(Object target) {
        // 遍历所有拦截器，依次对目标对象进行代理包装，形成职责链
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    // 添加拦截器
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    // 获取所有拦截器
    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }
}
```

1. **执行逻辑**

- MyBatis启动时，所有注册的`Interceptor`会被添加到`InterceptorChain`中；

- 当MyBatis创建核心组件（如`Executor`、`StatementHandler`等）时，会调用`pluginAll`方法，通过动态代理将所有拦截器包装到目标对象上，形成链式代理；

- 当目标方法（如`Executor.query()`）执行时，会按拦截器的包装顺序（反向即为执行顺序）依次调用`intercept`方法；

- 在`intercept`方法中，调用 `invocation.proceed()` 即可将请求传递给下一个拦截器，最终执行目标方法。

1. **经典示例：SQL打印插件（具体处理者）**

```java

@Intercepts({@Signature(
        type = StatementHandler.class,
        method = "prepare",
        args = {Connection.class, Integer.class}
)})
public class SqlPrintInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1. 自身处理逻辑：获取并打印SQL
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        System.out.println("MyBatis执行SQL：" + sql);
        
        // 2. 传递给下一个拦截器（最终执行目标方法）
        return invocation.proceed();
    }
}
```

## 三、 核心总结

1. **JDK原生经典实现**：`Servlet Filter` 链（显性职责链，手动调用`chain.doFilter`传递请求）、`try-catch` 异常处理（隐性职责链，JVM自动匹配传递）；

2. **三方框架经典实现**：Spring MVC `HandlerInterceptor` 链（业务层拦截，支持前置/后置处理）、MyBatis `Interceptor` 链（框架层插件，基于动态代理构建链式代理）；

3. **共性设计思想**：
        

    - 都有明确的“抽象处理者”接口，定义统一处理方法；

    - 都有“链式管理对象”（FilterChain/HandlerExecutionChain/InterceptorChain），维护处理者顺序；

    - 都通过“显式调用传递方法”（`chain.doFilter`/`invocation.proceed`）实现请求链式传递，支持中断链条；

    - 核心价值：解耦请求发送者与处理者，灵活扩展（新增/删除处理者无需修改原有逻辑）。
> （注：文档部分内容可能由 AI 生成）
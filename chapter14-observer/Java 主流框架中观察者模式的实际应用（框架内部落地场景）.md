# Java 主流框架中观察者模式的实际应用（框架内部落地场景）

本文聚焦观察者模式在各类 Java 框架**内部的原生应用**（非开发者基于框架的二次实现），拆解 Spring 生态、Guava、Netty 等主流框架的内部落地场景，解析其核心用途、实现原理，并补充核心源码片段辅助理解。

## 一、 Spring 框架（核心生态）：观察者模式的深度落地

Spring 框架内部大量使用观察者模式实现**事件驱动、生命周期回调、状态变更通知**等核心功能，是框架解耦的关键支撑。

### 1.  核心应用1：Spring 容器生命周期事件（ApplicationContextEvent）

Spring 容器从启动到销毁的整个生命周期中，会自动发布一系列标准事件，内部组件和开发者可监听这些事件完成初始化、资源加载等操作，这是观察者模式的典型落地。

#### （1） 核心事件类型（均继承 `ApplicationEvent`）

|事件类型|触发时机|框架内部用途|
|---|---|---|
|`ContextRefreshedEvent`|Spring 容器初始化完成（所有 Bean 加载完毕）|框架内部组件完成后置初始化、缓存加载|
|`ContextStartedEvent`|容器启动（调用 `start()` 方法）|激活容器内的生命周期 Bean|
|`ContextStoppedEvent`|容器停止（调用 `stop()` 方法）|暂停容器内的生命周期 Bean、释放临时资源|
|`ContextClosedEvent`|容器销毁（调用 `close()` 方法）|释放容器所有资源、销毁 Bean 实例、关闭连接池|
|`RequestHandledEvent`|Web 容器中 HTTP 请求处理完成|记录请求日志、统计请求耗时（Spring MVC 内部）|
#### （2） 框架内部应用场景

**场景1：容器初始化后置处理**

Spring 容器加载完所有 Bean 后，会发布 `ContextRefreshedEvent` 事件。框架内部的 `BeanFactoryPostProcessor`、`BeanPostProcessor` 等后置处理器，以及一些内置组件（如 `DefaultAdvisorAutoProxyCreator` 自动代理创建器），会监听该事件完成最终的初始化工作，确保所有 Bean 已就绪后再执行依赖注入和代理生成。

**场景2：Web 应用请求监控**

Spring MVC 内部会监听 `RequestHandledEvent` 事件，收集 HTTP 请求的 URL、处理时长、状态码等信息，用于日志输出和性能监控，无需侵入业务控制器代码，实现了监控逻辑与业务逻辑的解耦。

#### （3） 核心实现原理

- 被观察者：`AbstractApplicationContext`（Spring 容器的抽象实现，负责发布生命周期事件）

- 观察者：框架内部各类 `ApplicationListener` 实现类（如 `ContextRefreshedListener`、`RequestHandledListener`）

- 通知方式：同步推送（容器生命周期事件要求强一致性，需确保事件处理完成后再执行后续流程）

- 核心方法：`AbstractApplicationContext.publishEvent(ApplicationEvent event)` 用于发布事件，`ApplicationListener.onApplicationEvent(E event)` 用于处理事件

#### （4） 核心源码片段

##### 源码1：容器发布 `ContextRefreshedEvent` 事件（`AbstractApplicationContext` 核心方法）

```java

// Spring 容器抽象实现：AbstractApplicationContext
@Override
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
        // ... 省略容器刷新前置逻辑（加载配置、创建 BeanFactory 等）

        // 最后一步：发布容器刷新完成事件（ContextRefreshedEvent）
        finishRefresh();
    }
}

// 完成容器刷新并发布事件
protected void finishRefresh() {
    // 初始化生命周期处理器
    initLifecycleProcessor();
    // 触发生命周期处理器的 onRefresh 方法
    getLifecycleProcessor().onRefresh();
    // 核心：发布 ContextRefreshedEvent 事件
    publishEvent(new ContextRefreshedEvent(this));
    // 注册 MBean 监控
    LiveBeansView.registerApplicationContext(this);
}

// 事件发布核心方法
@Override
public void publishEvent(ApplicationEvent event) {
    publishEvent(event, null);
}

// 重载方法：实际执行事件发布逻辑
protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
    Assert.notNull(event, "Event must not be null");
    // 包装事件对象
    ApplicationEvent applicationEvent;
    if (event instanceof ApplicationEvent) {
        applicationEvent = (ApplicationEvent) event;
    } else {
        applicationEvent = new PayloadApplicationEvent<>(this, event);
        if (eventType == null) {
            eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
        }
    }
    // 提前发布事件（给早期监听器）
    if (this.earlyApplicationEvents != null) {
        this.earlyApplicationEvents.add(applicationEvent);
    } else {
        // 核心：获取事件多播器，分发事件给所有观察者
        getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
    }
    // 父容器也发布该事件
    if (this.parent != null && this.parent instanceof AbstractApplicationContext) {
        ((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
    }
}
```

##### 源码2：事件多播器分发事件（`SimpleApplicationEventMulticaster` 核心方法）

```java

// Spring 默认事件多播器：负责将事件分发给所有注册的观察者
@Override
public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
    ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
    // 同步分发（默认）：遍历所有观察者，执行 onApplicationEvent 方法
    for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
        Executor executor = getTaskExecutor();
        if (executor != null) {
            // 异步分发（若配置了线程池）
            executor.execute(() -> invokeListener(listener, event));
        } else {
            // 同步分发：核心调用观察者的事件处理方法
            invokeListener(listener, event);
        }
    }
}

// 调用观察者的 onApplicationEvent 方法
protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
    ErrorHandler errorHandler = getErrorHandler();
    if (errorHandler != null) {
        try {
            doInvokeListener(listener, event);
        } catch (Throwable err) {
            errorHandler.handleError(err);
        }
    } else {
        doInvokeListener(listener, event);
    }
}

@SuppressWarnings({"rawtypes", "unchecked"})
private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
    try {
        // 最终回调观察者的 onApplicationEvent 方法
        listener.onApplicationEvent(event);
    } catch (ClassCastException ex) {
        // 省略异常处理逻辑
    }
}
```

### 2.  核心应用2：Spring Bean 生命周期回调（基于观察者模式扩展）

Spring Bean 从创建到销毁的生命周期中，观察者模式支撑了**非侵入式的回调通知**，典型场景包括：

**场景1：`InitializingBean` 与 `DisposableBean`**

本质是观察者模式的简化实现：Spring 容器（被观察者）在 Bean 初始化完成后，会主动回调 `InitializingBean.afterPropertiesSet()` 方法；在 Bean 销毁前，回调 `DisposableBean.destroy()` 方法，Bean 本身作为观察者，无需主动感知容器状态。

**场景2：`@PostConstruct` 与 `@PreDestroy` 注解**

Spring 内部通过 `CommonAnnotationBeanPostProcessor` 监听 Bean 生命周期事件，当检测到 Bean 带有这两个注解时，会在对应时机执行注解标记的方法，实现了 Bean 生命周期回调与容器的解耦。

#### 核心源码片段

##### 源码1：`InitializingBean` 回调触发（`AbstractAutowireCapableBeanFactory` 核心方法）

```java

// Spring Bean 工厂：创建 Bean 并触发初始化回调
protected void invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd)
        throws Throwable {
    // 1. 判断 Bean 是否实现了 InitializingBean 接口
    boolean isInitializingBean = (bean instanceof InitializingBean);
    if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
        if (logger.isTraceEnabled()) {
            logger.trace("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
        }
        if (System.getSecurityManager() != null) {
            // 权限控制下的回调
            try {
                AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                    ((InitializingBean) bean).afterPropertiesSet();
                    return null;
                }, getAccessControlContext());
            } catch (PrivilegedActionException pae) {
                throw pae.getException();
            }
        } else {
            // 核心：回调 InitializingBean.afterPropertiesSet() 方法
            ((InitializingBean) bean).afterPropertiesSet();
        }
    }
    // 2. 触发 @PostConstruct 或 xml 配置的 init-method 方法（省略后续逻辑）
    if (mbd != null && bean.getClass() != NullBean.class) {
        String initMethodName = mbd.getInitMethodName();
        if (StringUtils.hasLength(initMethodName) &&
                !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
                !mbd.isExternallyManagedInitMethod(initMethodName)) {
            invokeCustomInitMethod(beanName, bean, mbd);
        }
    }
}
```

##### 源码2：`@PostConstruct` 注解处理（`CommonAnnotationBeanPostProcessor` 核心代码）

```java

// 继承自 InitDestroyAnnotationBeanPostProcessor，处理 @PostConstruct 和 @PreDestroy 注解
public class CommonAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor
        implements InstantiationAwareBeanPostProcessor, BeanFactoryAware, Serializable {

    public CommonAnnotationBeanPostProcessor() {
        // 注册 @PostConstruct 注解为初始化注解
        setInitAnnotationType(PostConstruct.class);
        // 注册 @PreDestroy 注解为销毁注解
        setDestroyAnnotationType(PreDestroy.class);
        // ... 省略其他初始化逻辑
    }
}

// 父类 InitDestroyAnnotationBeanPostProcessor：处理初始化和销毁注解
@Override
public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    // 查找 Bean 中带有 @PostConstruct 注解的方法
    LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
    try {
        // 触发 @PostConstruct 注解方法执行
        metadata.invokeInitMethods(bean, beanName);
    } catch (InvocationTargetException ex) {
        throw new BeanCreationException(beanName, "Invocation of init method failed", ex.getTargetException());
    } catch (Throwable ex) {
        throw new BeanCreationException(beanName, "Failed to invoke init method", ex);
    }
    return bean;
}
```

### 3.  核心应用3：Spring MVC 事件驱动（请求处理流程）

Spring MVC 内部通过观察者模式实现了**请求处理的扩展点**，典型场景：

**场景：`ServletRequestHandledEvent` 事件**

当 DispatcherServlet 完成一次 HTTP 请求处理后，会发布该事件，框架内部的监听器会监听该事件，记录请求的处理时间、响应状态码、客户端 IP 等信息，用于日志统计和问题排查，无需修改 `Controller` 代码即可实现监控功能。

#### 核心源码片段

##### 源码：DispatcherServlet 发布请求处理完成事件

```java

// Spring MVC 核心 Servlet：DispatcherServlet
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    HttpServletRequest processedRequest = request;
    HandlerExecutionChain mappedHandler = null;
    boolean multipartRequestParsed = false;

    WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

    try {
        ModelAndView mv = null;
        Exception dispatchException = null;

        try {
            // 1. 检查是否为文件上传请求
            processedRequest = checkMultipart(request);
            multipartRequestParsed = (processedRequest != request);

            // 2. 获取请求对应的处理器（Controller 方法）
            mappedHandler = getHandler(processedRequest);
            if (mappedHandler == null) {
                noHandlerFound(processedRequest, response);
                return;
            }

            // 3. 获取处理器适配器
            HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

            // 4. 执行拦截器前置逻辑 + 处理器方法（核心业务逻辑）
            // ... 省略中间核心逻辑

            // 5. 执行拦截器后置逻辑
            // ... 省略中间核心逻辑

            // 6. 渲染视图
            // ... 省略视图渲染逻辑
        } catch (Exception ex) {
            dispatchException = ex;
        } catch (Throwable err) {
            dispatchException = new NestedServletException("Handler dispatch failed", err);
        }
        // 7. 处理请求结果（异常处理等）
        processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
    } catch (Exception ex) {
        // 执行拦截器异常逻辑
        triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
    } catch (Throwable err) {
        // 执行拦截器异常逻辑
        triggerAfterCompletion(processedRequest, response, mappedHandler,
                new NestedServletException("Handler processing failed", err));
    } finally {
        // 异步请求处理
        if (asyncManager.isConcurrentHandlingStarted()) {
            if (mappedHandler != null) {
                mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
            }
        } else {
            // 清理文件上传请求资源
            if (multipartRequestParsed) {
                cleanupMultipart(processedRequest);
            }
        }
    }
}

// 处理请求结果后，发布 ServletRequestHandledEvent 事件
private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
        @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
        @Nullable Exception exception) throws Exception {

    boolean errorView = false;

    // 1. 处理异常（渲染错误视图）
    if (exception != null) {
        if (exception instanceof ModelAndViewDefiningException) {
            mv = ((ModelAndViewDefiningException) exception).getModelAndView();
        } else {
            Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
            mv = processHandlerException(request, response, handler, exception);
            errorView = (mv != null);
        }
    }

    // 2. 渲染视图（若有）
    if (mv != null && !mv.wasCleared()) {
        render(mv, request, response);
        if (errorView) {
            WebUtils.clearErrorRequestAttributes(request);
        }
    } else {
        if (logger.isTraceEnabled()) {
            logger.trace("No view rendering, null ModelAndView returned.");
        }
    }

    // 3. 执行拦截器最终逻辑
    if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
        return;
    }

    // 4. 触发拦截器 afterCompletion 方法
    if (mappedHandler != null) {
        mappedHandler.triggerAfterCompletion(request, response, null);
    }

    // 5. 核心：发布 ServletRequestHandledEvent 事件
    if (this.publishEvents && !request.isAsyncStarted()) {
        // 构造请求处理事件（包含 URL、耗时、状态码等信息）
        ServletRequestHandledEvent event = new ServletRequestHandledEvent(this,
                request.getRequestURI(), request.getRemoteAddr(),
                request.getMethod(), getServletConfig().getServletName(),
                WebUtils.getSessionId(request), getUsernameForRequest(request),
                (System.currentTimeMillis() - request.getAttribute(START_TIME_ATTRIBUTE)));
        // 发布事件
        getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        if (wac != null) {
            wac.publishEvent(event);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info(event.toString());
            }
        }
    }
}
```

## 二、 Guava 框架：EventBus 自身的设计与应用

Guava 的 `EventBus` 本身是观察者模式的**高级封装实现**，同时 Guava 内部也使用该机制完成核心功能的解耦，典型场景如下：

### 1.  核心应用：Guava Cache 缓存失效事件

Guava Cache（本地缓存）内部使用观察者模式实现**缓存失效通知**，当缓存条目被移除（过期、被淘汰、手动移除）时，会主动通知注册的监听器，方便开发者做缓存预热、数据同步等操作。

#### （1） 框架内部实现

- 被观察者：`LocalCache`（Guava Cache 的核心实现，维护缓存条目状态）

- 观察者：`RemovalListener`（缓存移除监听器，框架内部可默认实现，也支持开发者自定义）

- 事件：`RemovalNotification`（承载缓存移除的键、值、移除原因（`RemovalCause`））

- 核心逻辑：当缓存条目被移除时，`LocalCache` 会同步调用 `RemovalListener.onRemoval(RemovalNotification<K, V> notification)` 方法，推送移除事件

#### （2） 框架内部用途

- 框架内部：用于统计缓存失效频率、分析缓存淘汰策略的有效性

- 开发者扩展：用于缓存失效后的自动预热（如移除后立即从数据库加载最新数据放入缓存）、数据一致性同步（如缓存失效后更新其他存储介质）

#### （3） 核心源码片段

##### 源码1：`LocalCache` 触发缓存移除通知

```java

// Guava Cache 核心实现：LocalCache
// 缓存条目移除时的核心方法
void notifyRemoval(Entry<K, V> entry, RemovalCause cause) {
    // 获取缓存配置的移除监听器
    RemovalListener<? super K, ? super V> removalListener = this.removalListener;
    if (removalListener != null) {
        try {
            // 构造移除通知事件（包含键、值、移除原因）
            RemovalNotification<K, V> notification = new RemovalNotification<>(
                    entry.getKey(), entry.getValue(), cause);
            // 核心：回调观察者（RemovalListener）的 onRemoval 方法
            removalListener.onRemoval(notification);
        } catch (Throwable t) {
            // 异常处理：使用监听器异常处理器
            LocalCache.this.listenerExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    throw new RuntimeException(t);
                }
            });
        }
    }
}

// 缓存条目过期淘汰时，触发移除通知
void expireEntries(long now) {
    // 省略过期条目遍历逻辑
    for (Iterator<Entry<K, V>> iterator = entrySet().iterator(); iterator.hasNext(); ) {
        Entry<K, V> entry = iterator.next();
        if (entry.isExpired(now)) {
            iterator.remove();
            // 触发移除通知
            notifyRemoval(entry, RemovalCause.EXPIRED);
        }
    }
}

// 手动移除缓存条目时，触发移除通知
@Nullable
public V remove(Object key) {
    Preconditions.checkNotNull(key);
    Lock lock = stripLock(key);
    lock.lock();
    try {
        Entry<K, V> entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        V value = entry.getValue();
        // 移除条目
        removeEntry(entry, RemovalCause.EXPLICIT);
        // 触发移除通知
        notifyRemoval(entry, RemovalCause.EXPLICIT);
        return value;
    } finally {
        lock.unlock();
    }
}
```

##### 源码2：`RemovalNotification` 事件载体

```java

// 缓存移除通知事件：承载移除相关数据
public final class RemovalNotification<K, V> implements Map.Entry<K, V> {
    private final K key;
    private final V value;
    private final RemovalCause cause;

    // 构造方法：初始化键、值、移除原因
    public RemovalNotification(@Nullable K key, @Nullable V value, RemovalCause cause) {
        this.key = key;
        this.value = value;
        this.cause = Preconditions.checkNotNull(cause);
    }

    // 获取移除原因
    public RemovalCause getCause() {
        return cause;
    }

    // 省略 Map.Entry 接口实现方法（getKey、getValue 等）
    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("RemovalNotification is immutable");
    }
}
```

### 2.  核心特性：同步/异步事件总线的设计

Guava 内部通过 `EventBus`（同步）和 `AsyncEventBus`（异步）实现了两种通知模式，支撑不同场景的需求：

- 同步事件总线：用于需要强一致性的场景（如缓存失效通知，需确保监听器处理完成后再执行后续逻辑）

- 异步事件总线：基于 `ExecutorService` 实现，用于耗时操作（如日志持久化、远程数据同步），避免阻塞缓存核心逻辑

#### 核心源码片段

##### 源码1：`EventBus` 同步事件分发

```java

// Guava 同步事件总线：EventBus
public class EventBus {
    private final String identifier;
    private final Executor executor;
    private final SubscriberExceptionHandler exceptionHandler;
    private final SubscriberRegistry subscribers = new SubscriberRegistry(this);

    // 构造方法：默认使用直接执行器（同步执行）
    public EventBus() {
        this("default");
    }

    public EventBus(String identifier) {
        this(identifier, MoreExecutors.directExecutor(), SubscriberExceptionHandlers.DEFAULT);
    }

    // 事件发布核心方法
    public void post(Object event) {
        // 获取该事件对应的所有订阅者（观察者）
        Set<Subscriber> subscribers = this.subscribers.getSubscribers(event);
        for (Subscriber subscriber : subscribers) {
            // 分发事件给订阅者
            dispatchEvent(event, subscriber);
        }
    }

    // 分发事件：同步执行订阅者方法
    private void dispatchEvent(Object event, Subscriber subscriber) {
        try {
            // 核心：执行订阅者的 @Subscribe 标记方法
            subscriber.dispatchEvent(event);
        } catch (InvocationTargetException e) {
            // 异常处理
            this.exceptionHandler.handleException(e.getCause(), new SubscriberExceptionContext(
                    this, event, subscriber));
        }
    }

    // 省略注册/移除订阅者的方法
}
```

##### 源码2：`AsyncEventBus` 异步事件分发

```java

// Guava 异步事件总线：继承自 EventBus
public class AsyncEventBus extends EventBus {
    private final Executor executor;

    // 构造方法：传入自定义线程池
    public AsyncEventBus(String identifier, Executor executor) {
        super(identifier, executor, SubscriberExceptionHandlers.DEFAULT);
        this.executor = Preconditions.checkNotNull(executor);
    }

    // 重写事件分发方法：异步执行
    @Override
    void dispatchEvent(Object event, Subscriber subscriber) {
        // 使用线程池异步执行订阅者方法，避免阻塞
        this.executor.execute(() -> {
            try {
                subscriber.dispatchEvent(event);
            } catch (InvocationTargetException e) {
                getExceptionHandler().handleException(e.getCause(), new SubscriberExceptionContext(
                        this, event, subscriber));
            }
        });
    }
}
```

##### 源码3：`Subscriber` 执行订阅者方法

```java

// Guava 订阅者封装：对应观察者的方法
class Subscriber {
    private final EventBus bus;
    private final Object target;
    private final Method method;
    private final Executor executor;

    // 分发事件：反射调用 @Subscribe 标记的方法
    void dispatchEvent(Object event) throws InvocationTargetException {
        try {
            // 核心：反射执行观察者的订阅方法
            method.invoke(target, event);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }
}
```

## 三、 Netty 框架：网络事件驱动的核心支撑

Netty（高性能网络通信框架）的核心架构是**Reactor 模式**，而观察者模式是 Reactor 模式的基础，支撑了所有网络事件的分发与处理。

### 1.  核心应用1：Channel 事件监听（`ChannelHandler`）

Netty 中所有网络事件（连接建立、数据读取、连接关闭等）的处理，均基于观察者模式实现：

#### （1） 核心组件对应关系

|观察者模式角色|Netty 对应组件|作用说明|
|---|---|---|
|抽象被观察者|`Channel`（通道）|维护网络连接状态，发布各类网络事件|
|具体被观察者|`NioSocketChannel`/`NioServerSocketChannel`|对应 TCP 客户端/服务端通道，实际发布事件|
|抽象观察者|`ChannelHandler`（处理器）|定义网络事件的处理接口（如 `channelRead`、`channelActive`）|
|具体观察者|`SimpleChannelInboundHandler`/`ChannelInboundHandlerAdapter`|实现具体的事件处理逻辑（如数据解码、业务处理）|
|事件分发器|`ChannelPipeline`（管道）|维护 `ChannelHandler` 链表，负责将事件分发给对应观察者|
#### （2） 框架内部应用场景

**场景1：连接建立事件（`channelActive`）**

当客户端与服务端建立 TCP 连接后，`NioServerSocketChannel` 会发布连接建立事件，`ChannelPipeline` 会将该事件分发给链路上的 `ChannelHandler`，处理器可在该事件中完成初始化（如分配会话 ID、记录连接日志）。

**场景2：数据读取事件（`channelRead`）**

当 Netty 从 Socket 缓冲区读取到数据后，会发布数据读取事件，`ChannelHandler`（如 `StringDecoder` 字符串解码器）会监听该事件，完成数据解码后传递给下一个处理器，最终到达业务处理器。

**场景3：连接关闭事件（`channelInactive`）**

当 TCP 连接关闭后，Netty 会发布连接关闭事件，处理器可在该事件中释放资源（如关闭数据库连接、清理会话缓存）。

#### （3） 核心源码片段

##### 源码1：`ChannelPipeline` 事件分发（核心逻辑）

```java

// Netty 通道管道：维护 ChannelHandler 链表，分发事件
public interface ChannelPipeline {
    // 入站事件：数据读取
    ChannelPipeline fireChannelRead(Object msg);

    // 入站事件：连接建立
    ChannelPipeline fireChannelActive();

    // 入站事件：连接关闭
    ChannelPipeline fireChannelInactive();

    // 出站事件：写入数据
    ChannelPipeline write(Object msg);

    // 省略其他事件方法
}

// 默认实现：DefaultChannelPipeline
public class DefaultChannelPipeline implements ChannelPipeline {
    private final Channel channel;
    private final AbstractChannelHandlerContext head;
    private final AbstractChannelHandlerContext tail;

    // 构造方法：初始化头节点和尾节点
    public DefaultChannelPipeline(Channel channel) {
        this.channel = Preconditions.checkNotNull(channel, "channel");
        this.head = new HeadContext(this);
        this.tail = new TailContext(this);
        head.next = tail;
        tail.prev = head;
    }

    // 分发连接建立事件（fireChannelActive）
    @Override
    public final ChannelPipeline fireChannelActive() {
        // 从 head 节点开始，向后传递事件
        AbstractChannelHandlerContext.invokeChannelActive(head);
        return this;
    }

    // 分发数据读取事件（fireChannelRead）
    @Override
    public final ChannelPipeline fireChannelRead(Object msg) {
        // 从 head 节点开始，向后传递事件
        AbstractChannelHandlerContext.invokeChannelRead(head, msg);
        return this;
    }

    // 静态方法：触发 ChannelHandler 的 channelActive 方法
    static void invokeChannelActive(final AbstractChannelHandlerContext next) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            // 事件循环线程内：直接调用
            next.invokeChannelActive();
        } else {
            // 非事件循环线程：提交任务执行
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeChannelActive();
                }
            });
        }
    }

    // 静态方法：触发 ChannelHandler 的 channelRead 方法
    static void invokeChannelRead(final AbstractChannelHandlerContext next, final Object msg) {
        ObjectUtil.checkNotNull(msg, "msg");
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            // 事件循环线程内：直接调用
            next.invokeChannelRead(msg);
        } else {
            // 非事件循环线程：提交任务执行
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeChannelRead(msg);
                }
            });
        }
    }
}
```

##### 源码2：`AbstractChannelHandlerContext` 调用 `ChannelHandler` 方法

```java

// Netty 处理器上下文：封装 ChannelHandler，负责方法调用
abstract class AbstractChannelHandlerContext implements ChannelHandlerContext, ResourceLeakHint {
    // 调用 channelActive 方法
    private void invokeChannelActive() {
        if (invokeHandler()) {
            try {
                // 核心：调用 ChannelInboundHandler 的 channelActive 方法
                ((ChannelInboundHandler) handler()).channelActive(this);
            } catch (Throwable t) {
                notifyHandlerException(t);
            }
        } else {
            // 传递给下一个处理器
            fireChannelActive();
        }
    }

    // 调用 channelRead 方法
    private void invokeChannelRead(Object msg) {
        if (invokeHandler()) {
            try {
                // 核心：调用 ChannelInboundHandler 的 channelRead 方法
                ((ChannelInboundHandler) handler()).channelRead(this, msg);
            } catch (Throwable t) {
                notifyHandlerException(t);
            }
        } else {
            // 传递给下一个处理器
            fireChannelRead(msg);
        }
    }

    // 调用 channelInactive 方法
    private void invokeChannelInactive() {
        if (invokeHandler()) {
            try {
                // 核心：调用 ChannelInboundHandler 的 channelInactive 方法
                ((ChannelInboundHandler) handler()).channelInactive(this);
            } catch (Throwable t) {
                notifyHandlerException(t);
            }
        } else {
            // 传递给下一个处理器
            fireChannelInactive();
        }
    }
}
```

##### 源码3：`ChannelInboundHandlerAdapter` 默认实现

```java

// Netty 入站处理器适配器：默认实现 ChannelInboundHandler 接口
public class ChannelInboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelInboundHandler {
    // 连接建立事件：默认传递给下一个处理器
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    // 数据读取事件：默认传递给下一个处理器
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    // 连接关闭事件：默认传递给下一个处理器
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    // 省略其他事件方法
}
```

### 2.  核心应用2：`Future` 与 `Promise` 异步结果通知

Netty 的 `ChannelFuture` 内部使用观察者模式实现**异步操作结果的回调通知**，避免同步阻塞：

- 被观察者：`DefaultChannelPromise`（异步操作的载体，维护操作状态）

- 观察者：`GenericFutureListener`（异步结果监听器）

- 核心逻辑：当异步操作（如通道绑定、数据写入）完成（成功/失败）时，`DefaultChannelPromise` 会发布结果事件，`GenericFutureListener` 会监听该事件并执行回调逻辑（如 `operationComplete` 方法），实现了异步操作与结果处理的解耦

#### 核心源码片段

##### 源码1：`DefaultChannelPromise` 添加监听器并触发回调

```java

// Netty 异步操作结果载体：DefaultChannelPromise
public class DefaultChannelPromise extends DefaultPromise<Void> implements ChannelPromise {
    private final Channel channel;

    // 添加异步结果监听器
    @Override
    public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    // 异步操作成功时，触发监听器回调
    @Override
    public Promise<Void> setSuccess(Void result) {
        super.setSuccess(result);
        return this;
    }

    // 异步操作失败时，触发监听器回调
    @Override
    public Promise<Void> setFailure(Throwable cause) {
        super.setFailure(cause);
        return this;
    }
}

// 父类 DefaultPromise：维护监听器并触发回调
public class DefaultPromise<V> implements Promise<V> {
    private final EventExecutor executor;
    private volatile Object result;
    private volatile List<GenericFutureListener<? extends Future<V>>> listeners;

    // 添加监听器
    @Override
    public Promise<V> addListener(GenericFutureListener<? extends Future<V>> listener) {
        ObjectUtil.checkNotNull(listener, "listener");
        synchronized (this) {
            addListener0(listener);
        }
        // 若异步操作已完成，立即触发回调
        if (isDone()) {
            notifyListeners();
        }
        return this;
    }

    // 添加监听器到列表
    private void addListener0(GenericFutureListener<? extends Future<V>> listener) {
        if (listeners == null) {
            listeners = new ArrayList<>(1);
        }
        listeners.add(listener);
    }

    // 通知所有监听器
    private void notifyListeners() {
        if (listeners == null) {
            return;
        }
        // 遍历监听器，执行回调
        for (GenericFutureListener<? extends Future<V>> listener : listeners) {
            notifyListener0(this, listener);
        }
        // 清空监听器列表
        listeners = null;
    }

    // 触发单个监听器的回调
    private static <V> void notifyListener0(Future<V> future, GenericFutureListener<? extends Future<V>> listener) {
        try {
            // 核心：调用监听器的 operationComplete 方法
            listener.operationComplete(future);
        } catch (Throwable t) {
            // 异常处理
            logger.warn("An exception was thrown by " + listener.getClass().getName() + ".operationComplete()", t);
        }
    }
}
```

##### 源码2：`GenericFutureListener` 监听器接口

```java

// Netty 异步结果监听器接口：观察者
public interface GenericFutureListener<F extends Future<?>> extends EventListener {
    // 异步操作完成时的回调方法（核心）
    void operationComplete(F future) throws Exception;
}
```

## 四、 MyBatis 框架：配置加载与插件触发

MyBatis（持久层框架）内部使用观察者模式实现**配置变更通知**和**插件生命周期管理**，典型场景如下：

### 1.  核心应用1：配置加载事件（`Configuration` 初始化）

MyBatis 在加载配置文件（`mybatis-config.xml`、Mapper 映射文件）时，会通过观察者模式通知内部组件完成初始化：

- 被观察者：`Configuration`（MyBatis 核心配置类，维护所有配置信息）

- 观察者：`MapperRegistry`（Mapper 注册器）、`TypeHandlerRegistry`（类型处理器注册器）等

- 核心逻辑：当 `Configuration` 加载完 Mapper 接口或类型处理器配置后，会发布配置完成事件，对应观察者会监听该事件，完成 Mapper 代理对象的创建、类型处理器的注册，确保配置生效

#### 核心源码片段

##### 源码1：`Configuration` 注册 Mapper 接口（通知 `MapperRegistry`）

```java

// MyBatis 核心配置类：Configuration
public class Configuration {
    // Mapper 注册器（观察者）
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
    // 类型处理器注册器（观察者）
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    // 插件链（观察者）
    protected final InterceptorChain interceptorChain = new InterceptorChain();

    // 注册 Mapper 接口：通知 MapperRegistry 完成注册
    public <T> void addMapper(Class<T> type) {
        // 核心：调用 MapperRegistry 的 addMapper 方法
        mapperRegistry.addMapper(type);
    }

    // 获取 Mapper 代理对象：从 MapperRegistry 中获取
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    // 注册类型处理器：通知 TypeHandlerRegistry 完成注册
    public <T> void registerTypeHandler(Class<T> type, TypeHandler<? extends T> typeHandler) {
        typeHandlerRegistry.register(type, typeHandler);
    }

    // 注册插件：通知 InterceptorChain 完成注册
    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }
}
```

##### 源码2：`MapperRegistry` 注册 Mapper 并创建代理

```java

// MyBatis Mapper 注册器：观察者，处理 Mapper 配置事件
public class MapperRegistry {
    private final Configuration config;
    // 存储 Mapper 接口与代理工厂的映射
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    // 注册 Mapper 接口（核心方法）
    public <T> void addMapper(Class<T> type) {
        // 仅处理接口
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                // 创建 Mapper 代理工厂并放入缓存
                knownMappers.put(type, new MapperProxyFactory<>(type));
                // 解析 Mapper 注解（如 @Select、@Insert 等）
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();
                loadCompleted = true;
            } finally {
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }

    // 获取 Mapper 代理对象
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            // 创建 Mapper 代理实例
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }
}
```

### 2.  核心应用2：插件拦截器的触发（`Interceptor`）

MyBatis 的插件机制本质是观察者模式的扩展，通过动态代理结合事件通知实现：

- 被观察者：`Executor`（执行器）、`StatementHandler`（SQL 语句处理器）等核心组件

- 观察者：`Interceptor`（插件拦截器，开发者可自定义）

- 核心逻辑：当 MyBatis 执行 SQL 操作（如查询、更新）时，核心组件会发布执行事件，`Interceptor` 会监听该事件，在方法执行前后插入自定义逻辑（如分页处理、日志记录、权限校验），无需修改 MyBatis 核心源码即可扩展功能

#### 核心源码片段

##### 源码1：`InterceptorChain` 插件链（管理所有拦截器）

```java

// MyBatis 插件链：维护所有注册的 Interceptor（观察者）
public class InterceptorChain {
    // 存储所有拦截器
    private final List<Interceptor> interceptors = new ArrayList<>();

    // 对目标对象（Executor/StatementHandler 等）生成代理
    public Object pluginAll(Object target) {
        // 遍历所有拦截器，依次生成代理
        for (Interceptor interceptor : interceptors) {
            // 核心：调用拦截器的 plugin 方法，生成代理对象
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

##### 源码2：`Executor` 代理创建（触发插件拦截）

```java

// MyBatis 会话工厂：创建 SqlSession 时，为 Executor 创建代理
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final Configuration configuration;

    // 省略构造方法

    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;
        try {
            final Environment environment = configuration.getEnvironment();
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            // 创建原始 Executor
            final Executor executor = configuration.newExecutor(tx, execType);
            // 返回 SqlSession，其中的 Executor
```
> （注：文档部分内容可能由 AI 生成）
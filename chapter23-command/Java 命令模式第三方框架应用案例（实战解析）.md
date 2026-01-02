# Java 命令模式第三方框架应用案例（实战解析）

你希望了解命令模式在第三方框架中的实际应用，这能帮助你更深入地理解命令模式的落地价值。命令模式在主流Java框架中有着广泛的隐含或显式应用，下面将重点解析**Spring框架**（最常用）和**Apache Commons Exec**（命令执行工具）中的命令模式实践，包含原理分析和可运行代码示例。

## 一、 核心框架应用1：Spring 框架中的命令模式

Spring框架多处采用命令模式的设计思想，其中最典型的有两个场景：`CommandLineRunner`/`ApplicationRunner`（应用启动命令执行）、`JdbcTemplate`（数据库操作命令封装），下面重点解析并提供实战代码。

### 场景1.1：Spring `CommandLineRunner`（应用启动后自动执行命令）

#### 原理分析（命令模式角色映射）

`CommandLineRunner` 是Spring提供的接口，本质是**命令接口（Command）**，其核心方法 `run(String... args)` 对应命令模式的 `execute()` 方法。

|命令模式角色|Spring `CommandLineRunner` 对应实现|
|---|---|
|命令接口（Command）|`CommandLineRunner` 接口（统一命令执行规范）|
|具体命令（ConcreteCommand）|自定义实现 `CommandLineRunner` 的Bean（封装具体启动任务）|
|调用者（Invoker）|Spring容器（应用启动完成后，自动遍历并执行所有 `CommandLineRunner` Bean）|
|接收者（Receiver）|自定义业务组件（如Service、Mapper等，真正执行业务逻辑）|
Spring容器在启动完成后（刷新上下文后），会自动获取所有实现 `CommandLineRunner` 接口的Bean，并调用其 `run()` 方法，无需手动触发，完美体现了命令模式的“封装请求、统一执行”思想。

#### 实战代码示例

1. 项目依赖（Spring Boot 核心依赖，确保可直接运行）

```xml

<!-- pom.xml 核心依赖 -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <version>2.7.10</version>
    </dependency>
</dependencies>
```

1. 自定义业务接收者（Service，真正执行业务逻辑）

```java

import org.springframework.stereotype.Service;

/**
 * 接收者（Receiver）：自定义业务服务，真正处理核心逻辑
 */
@Service
public class StartupBusinessService {
    // 业务方法1：初始化缓存
    public void initCache() {
        System.out.println("【业务服务】执行缓存初始化操作，加载系统配置到本地缓存");
    }

    // 业务方法2：校验系统资源
    public void checkSystemResource() {
        System.out.println("【业务服务】执行系统资源校验，检查数据库连接、文件权限");
    }

    // 业务方法3：同步初始化数据
    public void syncInitData() {
        System.out.println("【业务服务】执行初始化数据同步，从远程服务拉取基础数据");
    }
}
```

1. 具体命令（ConcreteCommand）：实现 `CommandLineRunner`

```java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 具体命令1：缓存初始化命令（优先级1，先执行）
 */
@Component
@Order(1) // 控制命令执行顺序（数字越小，执行优先级越高）
public class CacheInitCommand implements CommandLineRunner {

    // 注入接收者（业务服务）
    @Autowired
    private StartupBusinessService businessService;

    @Override
    public void run(String... args) throws Exception {
        // 执行具体业务（调用接收者方法）
        System.out.println("\n执行命令：CacheInitCommand（缓存初始化）");
        businessService.initCache();
    }
}

/**
 * 具体命令2：资源校验命令（优先级2，中间执行）
 */
@Component
@Order(2)
public class ResourceCheckCommand implements CommandLineRunner {

    @Autowired
    private StartupBusinessService businessService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n执行命令：ResourceCheckCommand（资源校验）");
        businessService.checkSystemResource();
    }
}

/**
 * 具体命令3：数据同步命令（优先级3，最后执行）
 */
@Component
@Order(3)
public class DataSyncCommand implements CommandLineRunner {

    @Autowired
    private StartupBusinessService businessService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n执行命令：DataSyncCommand（数据同步）");
        businessService.syncInitData();
    }
}
```

1. Spring Boot 启动类（调用者：Spring容器自动执行命令）

```java

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 * （Spring容器作为调用者，启动完成后自动执行所有 CommandLineRunner 命令）
 */
@SpringBootApplication
public class SpringCommandPatternApplication {
    public static void main(String[] args) {
        System.out.println("=== 开始启动Spring Boot应用 ===");
        // 启动Spring容器，自动触发 CommandLineRunner 命令执行
        SpringApplication.run(SpringCommandPatternApplication.class, args);
        System.out.println("=== Spring Boot应用启动完成 ===");
    }
}
```

#### 运行结果

```text

=== 开始启动Spring Boot应用 ===

执行命令：CacheInitCommand（缓存初始化）
【业务服务】执行缓存初始化操作，加载系统配置到本地缓存

执行命令：ResourceCheckCommand（资源校验）
【业务服务】执行系统资源校验，检查数据库连接、文件权限

执行命令：DataSyncCommand（数据同步）
【业务服务】执行初始化数据同步，从远程服务拉取基础数据

=== Spring Boot应用启动完成 ===
```

#### 核心亮点

- 解耦：Spring容器（调用者）无需知道具体启动任务（命令）的实现，只需通过 `CommandLineRunner` 接口交互。

- 可扩展：新增启动任务时，只需新增 `CommandLineRunner` 实现类（加 `@Component`），无需修改原有代码（符合开闭原则）。

- 可排序：通过 `@Order` 注解控制命令执行顺序，灵活适配业务需求。

### 场景1.2：Spring `JdbcTemplate`（数据库操作命令封装）

#### 原理分析

`JdbcTemplate` 是Spring JDBC的核心类，其底层大量使用命令模式：将数据库查询、更新等操作封装为 `PreparedStatementCreator`、`ResultSetExtractor` 等命令对象，实现数据库操作与连接管理的解耦。

- 命令接口：`PreparedStatementCreator`（封装SQL预编译命令）、`ResultSetExtractor`（封装结果集解析命令）。

- 具体命令：自定义的 `PreparedStatementCreator` 实现类（封装具体SQL和参数）。

- 调用者：`JdbcTemplate`（负责执行命令，管理数据库连接、异常处理）。

- 接收者：数据库（真正执行SQL操作）。

#### 简化实战代码

```java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * JdbcTemplate 中的命令模式实践
 */
@Component
public class JdbcCommandDemo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 执行数据库插入命令（封装为 PreparedStatementCreator 命令对象）
    public void insertUser(String username, Integer age) {
        // 具体命令：PreparedStatementCreator 实现（匿名内部类，封装SQL命令）
        PreparedStatementCreator psc = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                // 封装SQL和参数（命令的具体内容）
                String sql = "INSERT INTO t_user (username, age) VALUES (?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, username);
                ps.setInt(2, age);
                return ps;
            }
        };

        // 调用者：JdbcTemplate 执行命令（无需关心连接管理、异常处理）
        jdbcTemplate.update(psc);
        System.out.println("用户插入成功（命令模式执行数据库操作）");
    }
}
```

## 二、 核心框架应用2：Apache Commons Exec（外部命令执行框架）

Apache Commons Exec 是Java用于执行外部系统命令（如Windows的 `cmd` 命令、Linux的 `sh` 命令）的开源框架，其核心设计完全基于命令模式，是显式应用命令模式的典型案例。

### 依赖引入

```xml

<!-- pom.xml 引入 Apache Commons Exec -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-exec</artifactId>
    <version>1.3</version>
</dependency>
```

### 原理分析（命令模式角色映射）

|命令模式角色|Apache Commons Exec 对应实现|
|---|---|
|命令接口（Command）|`CommandLine`（封装外部命令及参数）|
|具体命令（ConcreteCommand）|自定义构建的 `CommandLine` 对象（如“dir”命令、“ls”命令）|
|调用者（Invoker）|`DefaultExecutor`（负责执行命令，管理执行流程、输出处理）|
|接收者（Receiver）|操作系统（真正执行外部命令）|
### 实战代码示例（执行系统命令）

1. Windows 系统（执行 `dir` 命令，查看目录列表）

```java

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Apache Commons Exec 命令模式实战（Windows 系统）
 */
public class CommonsExecWindowsDemo {
    public static void main(String[] args) throws IOException {
        // 1. 构建具体命令（ConcreteCommand）：dir 命令（查看当前目录）
        CommandLine cmdLine = new CommandLine("cmd");
        // 添加命令参数（/c 表示执行后关闭cmd窗口）
        cmdLine.addArgument("/c");
        cmdLine.addArgument("dir");

        // 2. 构建调用者（Invoker）：DefaultExecutor（执行命令）
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0); // 设置正常退出值

        // 3. 捕获命令输出结果
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);

        // 4. 调用者执行命令（无需关心操作系统底层实现）
        System.out.println("=== 执行Windows命令：dir ===");
        executor.execute(cmdLine);

        // 5. 输出命令执行结果
        String result = outputStream.toString("GBK"); // Windows 编码为GBK
        System.out.println("命令执行结果：\n" + result);
    }
}
```

1. Linux/Mac 系统（执行 `ls -l` 命令，查看目录详情）

```java

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Apache Commons Exec 命令模式实战（Linux/Mac 系统）
 */
public class CommonsExecLinuxDemo {
    public static void main(String[] args) throws IOException {
        // 1. 构建具体命令（ConcreteCommand）：ls -l 命令
        CommandLine cmdLine = new CommandLine("ls");
        cmdLine.addArgument("-l");

        // 2. 构建调用者（Invoker）：DefaultExecutor
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);

        // 3. 捕获输出结果
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);

        // 4. 执行命令
        System.out.println("=== 执行Linux/Mac命令：ls -l ===");
        executor.execute(cmdLine);

        // 5. 输出结果（UTF-8 编码）
        String result = outputStream.toString("UTF-8");
        System.out.println("命令执行结果：\n" + result);
    }
}
```

### 运行结果（Windows示例）

```text

=== 执行Windows命令：dir ===
命令执行结果：
 驱动器 C 中的卷是 OS
 卷的序列号是 1234-5678

 C:\project\spring-command-demo 的目录

2026-01-03  10:00    <DIR>          .
2026-01-03  09:00    <DIR>          ..
2026-01-03  09:30               524 pom.xml
2026-01-03  10:00    <DIR>          src
               1 个文件            524 字节
               3 个目录  100,234,567,890 字节可用
```

### 核心亮点

- 完全遵循命令模式：`CommandLine` 封装命令，`DefaultExecutor` 执行命令，与操作系统（接收者）解耦。

- 跨平台兼容：只需构建对应系统的 `CommandLine` 对象，`DefaultExecutor` 无需修改，即可跨Windows/Linux/Mac执行。

- 功能强大：支持命令输出捕获、超时控制、异常处理等，避免原生 `Runtime.exec()` 的缺陷。

## 三、 其他框架中的命令模式补充

1. **Netty 框架**：`ChannelHandler` 本质是命令接口，自定义 `ChannelHandler` 实现类是具体命令，`EventLoop` 是调用者，负责执行网络事件处理命令。

2. **Quartz 任务调度框架**：`Job` 接口是命令接口，`JobDetail` 封装具体任务命令，`Scheduler` 是调用者，负责调度执行任务命令。

3. **GUI 框架（Swing/JavaFX）**：按钮 `ActionListener` 是命令接口，自定义 `ActionListener` 实现类是具体命令，按钮（`JButton`）是调用者，点击时触发命令执行。

## 四、 总结

1. 命令模式在第三方框架中主要以**显式（Apache Commons Exec）**和**隐含（Spring）**两种形式存在，核心都是“封装请求为对象，解耦调用者与接收者”。

2. 核心框架案例：
        

    - Spring：`CommandLineRunner`（应用启动命令）、`JdbcTemplate`（数据库操作命令），是日常开发中最常接触的命令模式实践。

    - Apache Commons Exec：完全显式遵循命令模式，专门用于执行外部系统命令，跨平台兼容性强。

3. 框架应用的共性优势：符合开闭原则（易扩展）、低耦合（组件独立）、统一执行规范（便于管理和维护）。

4. 理解框架中的命令模式，有助于更好地使用框架API，同时也能在自定义业务框架时借鉴其设计思想。
> （注：文档部分内容可能由 AI 生成）
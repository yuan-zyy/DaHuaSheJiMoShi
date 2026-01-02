## 第23章 命令模式

### 23.1 命令模式
**命令模式(Command Pattern)**

#### 一、核心定义与核心角色
##### 1. 核心定义
**命令模式(Command Pattern)**将一个请求封装为一个对象，使你能以不同的请求对客户进行参数化；对请求排队或记录请求日志，以及支持可撤销的操作

##### 2. 四大核心角色
命令模式包含 4 个不可缺少的角色，各自承担明确职责  

| 角色名称                  | 核心职责                                                 |
|-----------------------|------------------------------------------------------|
| 命令接口(Command)         | 定义执行请求的统一方法(通常是 execute()，所有具体命令都需实现该接口              |
| 具体命令(ConcreteCommand) | 实现命令接口，持有接收者对象的引用，在 execute() 方法中调用接收者的具体业务方法，完成请求处理 |      |
| 接收者(Receiver)         | 真正执行业务逻辑的对象，包含具体的业务操作(如 “打开文件” “发送消息” 等)，命令对象仅负责转发请求 |
| 调用者/请求者(Invoker)      | 持有命令对象的引用，负责触发命令执行(不直接调用接收者的方法)，只与命令接口交互，不关心具体命令实现   |

#### 二、完整 Java 代码示例
下面通过 “遥控器控制电灯” 的场景，实现命令模式，清晰展示各角色的协作：
##### 1. 步骤1 定义命令接口(Command)
统一命令执行的规范，仅声明 execute() 方法
```java
/**
 * 命令接口: 定义执行请求的统一方法
 */
public interface Command {
    // 执行命令的核心方法
    void execute();
}
```

##### 2. 步骤2 实现接收者(Receiver)
真正执行业务逻辑的电灯类，包括开灯、关灯的具体操作
```java
/**
 * 接收者: 电灯(真正执行开灯/关灯业务的场景)
 */
public class Light {
    // 开灯业务逻辑
    public void turnOn() {
        System.out.println("电灯打开了...");
    }
    
    // 关灯业务逻辑
    public void turnOff() {
        System.out.println("电灯关闭了...");
    }
}
```

##### 3. 步骤3 实现具体命令(ConcreteCommand)
分别实现 "开灯命令" 和 "关灯命令，持有电灯(接收者)引用，并转发请求
```java
/**
 * 具体命令: 开灯命令
 */
public class LightOnCommand implements Command {
    // 持有接收者对象的引用
    private Light light;
    // 通过构造方法注入
    public LightOnCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        // 调用接收者的具体业务方法（转发请求）
        light.turnOn();
    }
}

/**
 * 具体命令: 关灯命令
 */
public class LightOffCommand implements Command {
    // 持有接收者对象的引用
    private Light light;
    // 通过构造方法注入
    public LightOffCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        // 调用接收者的具体业务方法（转发请求）
        light.turnOff();
    }
}
```

##### 4. 步骤4 实现调用者/请求者(Invoker)
遥控器作为调用者，持有命令对象，负责触发命令执行
```java
/**
 * 调用者：遥控器（触发命令执行，不关心具体业务逻辑）
 */
public class RemoteControl {
    // 持有命令对象（可灵活替换不同命令）
    private Command command;

    // 设置命令对象（动态切换命令）
    public void setCommand(Command command) {
        this.command = command;
    }

    // 触发命令执行（按钮按下事件）
    public void pressButton() {
        System.out.println("遥控器：按下功能按钮");
        command.execute(); // 调用命令的执行方法
    }
}
```

##### 5. 步骤5 测试代码
客户端负责组装各角色，无需关心底层业务逻辑的执行细节
```java
/**
 * 客户端：使用命令模式的入口
 */
public class CommandClient {
    public static void main(String[] args) {
        // 1. 创建接收者（电灯）
        Light light = new Light();

        // 2. 创建具体命令（开灯命令、关灯命令），并绑定接收者
        Command lightOnCommand = new LightOnCommand(light);
        Command lightOffCommand = new LightOffCommand(light);

        // 3. 创建调用者（遥控器）
        RemoteControl remote = new RemoteControl();

        // 4. 遥控器设置并执行“开灯命令”
        System.out.println("=== 第一次操作 ===");
        remote.setCommand(lightOnCommand);
        remote.pressButton();

        // 5. 遥控器切换并执行“关灯命令”
        System.out.println("\n=== 第二次操作 ===");
        remote.setCommand(lightOffCommand);
        remote.pressButton();
    }
}
```

##### 6. 运行结果：
```text
=== 第一次操作 ===
遥控器：按下功能按钮
电灯：已打开，房间变亮

=== 第二次操作 ===
遥控器：按下功能按钮
电灯：已关闭，房间变暗
```

#### 三、命令模式的核心特点与适用场景
##### 1. 核心优点
- **解耦性**：调用者（遥控器）与接收者（电灯）完全解耦，调用者无需知道接收者的类名、方法名，仅通过命令接口交互。
- **灵活性**：可动态切换、添加、删除命令（如新增 “调节亮度命令”，无需修改遥控器和电灯的代码）。
- **支持扩展功能**：轻松实现命令队列（批量执行命令）、日志记录（记录命令执行轨迹）、可撤销操作（新增undo()方法）

##### 2. 适用场景
- 需要将请求发送者与接收者解耦的场景（如 GUI 按钮操作、远程接口调用）。
- 需要动态添加、删除或切换命令的场景（如任务调度系统、批量处理工具）。
- 需要实现命令队列、日志记录、可撤销操作的场景（如编辑器的撤销 / 重做、事务管理）

##### 3. 潜在缺点
- 类数量膨胀：每个具体请求都需要对应一个具体命令类，复杂场景下会产生大量命令类。
- 系统复杂度提升：增加了命令接口、具体命令等层级，一定程度上提升了系统的理解成本

#### 总结
1. 命令模式是行为型模式，核心是**将请求封装为独立对象**，核心角色为：命令接口、具体命令、接收者、调用者。
2. 关键协作流程：客户端组装命令与接收者 → 调用者持有命令 → 调用者触发execute() → 具体命令转发请求给接收者执行。
3. 核心价值：解耦调用者与接收者，提升系统灵活性，支持队列化、日志、可撤销等扩展功能，适用于需要灵活管理请求的场景


### n.2 

#### 一、

#### 二、

#### 总结

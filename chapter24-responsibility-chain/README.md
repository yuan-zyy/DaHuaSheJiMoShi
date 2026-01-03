## 第24章 职责链模式 

### 24.1 
你想了解Java中的职责链模式，我会从核心概念、结构、实现示例到应用场景全面为你讲解。

#### 一、核心概念

职责链模式（Chain of Responsibility Pattern）是一种**行为型设计模式**，其核心思想是：**将请求的发送者和接收者解耦，把多个可以处理该请求的对象连接成一条链式结构**。当请求发出后，会沿着这条链依次传递，直到链中某个对象能够处理该请求并终止传递，或请求遍历完整个链仍未被处理。

这种模式的核心价值在于解耦请求方与处理方，提高代码的灵活性和可扩展性，无需请求方知晓具体的处理者。

#### 二、核心角色

职责链模式包含3个核心角色，每个角色各司其职，构成完整的链式处理流程：

1.  **抽象处理者（Handler）**：定义处理请求的统一接口（通常包含处理方法和设置下一个处理者的方法），声明了处理请求的规范，同时持有下一个抽象处理者的引用，用于构建链式结构。

2.  **具体处理者（ConcreteHandler）**：实现抽象处理者的接口，是职责链中的实际处理节点。每个具体处理者会先判断自己是否能处理当前请求：若能处理，则自行处理并终止请求传递；若不能，则将请求转发给链中的下一个处理者。

3.  **请求对象（Request）**：封装了需要被处理的请求信息（如请求类型、请求内容、优先级等），供处理者判断和处理。

#### 三、完整代码实现

下面以“员工请假审批”为场景实现职责链模式：员工请假天数不同，审批权限不同（小组长：≤1天；部门经理：≤3天；总经理：≤7天；超过7天无人审批）。

##### 1. 定义请求对象（Request）

封装请假的核心信息，供处理者判断是否有权限审批。

```java

/**
 * 请求对象：封装请假请求信息
 */
public class LeaveRequest {
    // 请假人姓名
    private String empName;
    // 请假天数
    private int leaveDays;
    // 请假原因
    private String reason;

    // 构造方法
    public LeaveRequest(String empName, int leaveDays, String reason) {
        this.empName = empName;
        this.leaveDays = leaveDays;
        this.reason = reason;
    }

    // getter方法（供处理者获取请求信息）
    public String getEmpName() {
        return empName;
    }

    public int getLeaveDays() {
        return leaveDays;
    }

    public String getReason() {
        return reason;
    }
}
```

##### 2. 定义抽象处理者（Handler）

统一审批接口，持有下一个处理者的引用，构建链式结构。

```java

/**
 * 抽象处理者：请假审批者
 */
public abstract class LeaveApprover {
    // 下一个审批者（职责链的下一个节点）
    protected LeaveApprover nextApprover;
    // 审批者名称
    protected String approverName;

    public LeaveApprover(String approverName) {
        this.approverName = approverName;
    }

    // 设置下一个审批者（构建职责链）
    public void setNextApprover(LeaveApprover nextApprover) {
        this.nextApprover = nextApprover;
    }

    // 抽象审批方法（子类必须实现）
    public abstract void approve(LeaveRequest leaveRequest);
}
```

##### 3. 定义具体处理者（ConcreteHandler）

实现不同权限的审批逻辑，判断自身能否处理，不能则转发给下一个处理者。

###### （1）小组长（处理≤1天请假）

```java

/**
 * 具体处理者1：小组长（审批≤1天请假）
 */
public class GroupLeader extends LeaveApprover {
    public GroupLeader(String approverName) {
        super(approverName);
    }

    @Override
    public void approve(LeaveRequest leaveRequest) {
        if (leaveRequest.getLeaveDays() <= 1) {
            // 自身能处理，直接处理并终止传递
            System.out.printf("小组长【%s】审批：员工【%s】请假【%d】天，原因：%s → 审批通过%n",
                    approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveDays(), leaveRequest.getReason());
        } else {
            // 自身不能处理，转发给下一个审批者
            if (nextApprover != null) {
                System.out.printf("小组长【%s】无权限审批【%d】天假期，转发给下一个审批者%n",
                        approverName, leaveRequest.getLeaveDays());
                nextApprover.approve(leaveRequest);
            } else {
                // 无下一个处理者，请求未被处理
                System.out.printf("小组长【%s】无权限审批，且无后续审批者，请假请求失败%n", approverName);
            }
        }
    }
}
```

###### （2）部门经理（处理≤3天请假）

```java

/**
 * 具体处理者2：部门经理（审批≤3天请假）
 */
public class DepartmentManager extends LeaveApprover {
    public DepartmentManager(String approverName) {
        super(approverName);
    }

    @Override
    public void approve(LeaveRequest leaveRequest) {
        if (leaveRequest.getLeaveDays() <= 3) {
            // 自身能处理，直接处理
            System.out.printf("部门经理【%s】审批：员工【%s】请假【%d】天，原因：%s → 审批通过%n",
                    approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveDays(), leaveRequest.getReason());
        } else {
            // 自身不能处理，转发给下一个审批者
            if (nextApprover != null) {
                System.out.printf("部门经理【%s】无权限审批【%d】天假期，转发给下一个审批者%n",
                        approverName, leaveRequest.getLeaveDays());
                nextApprover.approve(leaveRequest);
            } else {
                System.out.printf("部门经理【%s】无权限审批，且无后续审批者，请假请求失败%n", approverName);
            }
        }
    }
}
```

###### （3）总经理（处理≤7天请假）

```java

/**
 * 具体处理者3：总经理（审批≤7天请假）
 */
public class GeneralManager extends LeaveApprover {
    public GeneralManager(String approverName) {
        super(approverName);
    }

    @Override
    public void approve(LeaveRequest leaveRequest) {
        if (leaveRequest.getLeaveDays() <= 7) {
            // 自身能处理，直接处理
            System.out.printf("总经理【%s】审批：员工【%s】请假【%d】天，原因：%s → 审批通过%n",
                    approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveDays(), leaveRequest.getReason());
        } else {
            // 自身不能处理，无更高权限审批者，请求未被处理
            if (nextApprover != null) {
                nextApprover.approve(leaveRequest);
            } else {
                System.out.printf("总经理【%s】无权限审批【%d】天假期，且无后续审批者，请假请求失败%n",
                        approverName, leaveRequest.getLeaveDays());
            }
        }
    }
}
```

##### 4. 客户端测试（构建职责链并发送请求）

```java

/**
 * 客户端：构建职责链，发送请假请求
 */
public class Client {
    public static void main(String[] args) {
        // 1. 创建各个具体处理者
        LeaveApprover groupLeader = new GroupLeader("张三");
        LeaveApprover deptManager = new DepartmentManager("李四");
        LeaveApprover generalManager = new GeneralManager("王五");

        // 2. 构建职责链：小组长 → 部门经理 → 总经理（设置下一个处理者）
        groupLeader.setNextApprover(deptManager);
        deptManager.setNextApprover(generalManager);

        // 3. 发送不同的请假请求
        System.out.println("===== 测试1：请假1天 =====");
        LeaveRequest request1 = new LeaveRequest("小明", 1, "感冒发烧");
        groupLeader.approve(request1);

        System.out.println("\n===== 测试2：请假2天 =====");
        LeaveRequest request2 = new LeaveRequest("小红", 2, "家中有事");
        groupLeader.approve(request2);

        System.out.println("\n===== 测试3：请假5天 =====");
        LeaveRequest request3 = new LeaveRequest("小刚", 5, "探亲");
        groupLeader.approve(request3);

        System.out.println("\n===== 测试4：请假10天 =====");
        LeaveRequest request4 = new LeaveRequest("小丽", 10, "旅游");
        groupLeader.approve(request4);
    }
}
```

##### 5. 运行结果

```Plain Text

===== 测试1：请假1天 =====
小组长【张三】审批：员工【小明】请假【1】天，原因：感冒发烧 → 审批通过

===== 测试2：请假2天 =====
小组长【张三】无权限审批【2】天假期，转发给下一个审批者
部门经理【李四】审批：员工【小红】请假【2】天，原因：家中有事 → 审批通过

===== 测试3：请假5天 =====
小组长【张三】无权限审批【5】天假期，转发给下一个审批者
部门经理【李四】无权限审批【5】天假期，转发给下一个审批者
总经理【王五】审批：员工【小刚】请假【5】天，原因：探亲 → 审批通过

===== 测试4：请假10天 =====
小组长【张三】无权限审批【10】天假期，转发给下一个审批者
部门经理【李四】无权限审批【10】天假期，转发给下一个审批者
总经理【王五】无权限审批【10】天假期，且无后续审批者，请假请求失败
```

#### 四、关键特点与调用逻辑

1.  **链式传递逻辑**：请求从链头（第一个处理者）开始，依次传递，每个处理者执行“判断自身能否处理 → 能则处理终止，不能则转发下一个”的逻辑。

2.  **请求终止条件**：① 某个具体处理者成功处理请求；② 请求遍历完整个链，无任何处理者能处理。

3.  **灵活性**：可动态调整职责链的节点（新增、删除、调整顺序），无需修改请求方和现有处理者的代码，符合“开闭原则”。

4.  **解耦性**：请求发送者（客户端）仅需知晓链头处理者，无需知晓具体的处理节点和处理流程，降低了组件间的耦合度。

#### 五、典型应用场景

1.  **权限审批系统**：如请假审批、报销审批、公文审批等，不同层级角色拥有不同审批权限。

2.  **请求过滤/拦截**：如Java Web中的Filter链（Servlet Filter），多个过滤器依次处理HTTP请求（编码过滤、登录验证过滤、权限过滤等）。

3.  **异常处理链**：多层级异常处理，不同异常处理器处理特定类型的异常，无法处理则向上传递。

4.  **消息分发系统**：消息沿着处理器链传递，直到找到能处理该消息的处理器。

#### 总结

1.  职责链模式是行为型模式，核心是**链式处理请求，解耦发送者与接收者**。

2.  三大核心角色：抽象处理者（定义规范+持有下一个节点）、具体处理者（实际处理/转发）、请求对象（封装请求信息）。

3.  核心逻辑：每个处理者自行判断是否处理请求，不能则转发给下一个，直到请求被处理或遍历完链。

4.  典型场景：审批系统、过滤器链、异常处理链等，具有良好的灵活性和可扩展性。
> （注：文档部分内容可能由 AI 生成）
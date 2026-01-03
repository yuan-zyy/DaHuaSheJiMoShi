# Java 职责链模式（Chain of Responsibility Pattern）

你想了解Java中的职责链模式，我会从核心概念、结构、实现示例到应用场景为你全面解析。

## 一、核心概念

职责链模式是一种**行为型设计模式**，其核心思想是：**将请求的发送者和接收者解耦，把多个可以处理该请求的对象连接成一条链式结构**。当请求发出后，会沿着这条链依次传递，直到链中某个对象能够处理该请求并终止传递，或请求遍历完整个链仍未被处理。

这种模式的核心价值在于消除请求发送者与多个接收者之间的直接依赖，提高系统的灵活性和可扩展性。

## 二、核心角色

职责链模式包含3个关键角色，它们共同构成链式处理结构：

1. **抽象处理者（Handler）**：定义处理请求的统一接口（通常包含处理方法和设置下一个处理者的方法），声明一个持有下一个处理者的引用，用于构建链式结构。

2. **具体处理者（ConcreteHandler）**：实现抽象处理者的接口，负责判断自身是否能处理当前请求：
        

    - 若能处理，则自行处理并终止请求传递；

    - 若不能处理，则将请求转发给链中的下一个处理者。

3. **请求对象（Request）**：封装需要被处理的请求信息（如请求类型、请求内容、优先级等），供处理者判断和处理。

## 三、完整Java代码实现

下面以“员工请假审批”场景为例，实现职责链模式：

### 1. 定义请求对象（Request）

封装请假的员工姓名、天数、类型等信息，作为处理者的处理依据

```java

/**
 * 请求对象：封装请假请求信息
 */
public class LeaveRequest {
    // 员工姓名
    private String empName;
    // 请假天数
    private int leaveDays;
    // 请假类型（事假/病假等）
    private String leaveType;

    // 构造方法
    public LeaveRequest(String empName, int leaveDays, String leaveType) {
        this.empName = empName;
        this.leaveDays = leaveDays;
        this.leaveType = leaveType;
    }

    // getter方法（供处理者获取请求信息）
    public String getEmpName() {
        return empName;
    }

    public int getLeaveDays() {
        return leaveDays;
    }

    public String getLeaveType() {
        return leaveType;
    }
}
```

### 2. 定义抽象处理者（Handler）

统一处理接口，包含下一个处理者的引用和链式构建方法

```java

/**
 * 抽象处理者：请假审批处理者
 */
public abstract class LeaveApprover {
    // 下一个处理者（维护链式结构的核心引用）
    protected LeaveApprover nextApprover;
    // 处理者名称
    protected String approverName;

    public LeaveApprover(String approverName) {
        this.approverName = approverName;
    }

    // 设置下一个处理者（构建职责链）
    public void setNextApprover(LeaveApprover nextApprover) {
        this.nextApprover = nextApprover;
    }

    // 抽象处理方法：由具体处理者实现
    public abstract void processRequest(LeaveRequest leaveRequest);
}
```

### 3. 定义具体处理者（ConcreteHandler）

实现不同层级的审批逻辑（组长、部门经理、总经理）

```java

/**
 * 具体处理者1：组长（处理1天以内事假）
 */
public class GroupLeader extends LeaveApprover {
    public GroupLeader(String approverName) {
        super(approverName);
    }

    @Override
    public void processRequest(LeaveRequest leaveRequest) {
        // 判断自身是否能处理该请求
        if ("事假".equals(leaveRequest.getLeaveType()) && leaveRequest.getLeaveDays() <= 1) {
            System.out.printf("【%s】审批通过：员工%s，%s%d天%n",
                    approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveType(), leaveRequest.getLeaveDays());
        } else {
            // 不能处理则转发给下一个处理者
            if (nextApprover != null) {
                nextApprover.processRequest(leaveRequest);
            } else {
                System.out.printf("【%s】无后续审批人，请假申请驳回：员工%s，%s%d天%n",
                        approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveType(), leaveRequest.getLeaveDays());
            }
        }
    }
}

/**
 * 具体处理者2：部门经理（处理3天以内事假/2天以内病假）
 */
public class DepartmentManager extends LeaveApprover {
    public DepartmentManager(String approverName) {
        super(approverName);
    }

    @Override
    public void processRequest(LeaveRequest leaveRequest) {
        boolean canHandle = false;
        if ("事假".equals(leaveRequest.getLeaveType()) && leaveRequest.getLeaveDays() <= 3) {
            canHandle = true;
        } else if ("病假".equals(leaveRequest.getLeaveType()) && leaveRequest.getLeaveDays() <= 2) {
            canHandle = true;
        }

        if (canHandle) {
            System.out.printf("【%s】审批通过：员工%s，%s%d天%n",
                    approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveType(), leaveRequest.getLeaveDays());
        } else {
            if (nextApprover != null) {
                nextApprover.processRequest(leaveRequest);
            } else {
                System.out.printf("【%s】无后续审批人，请假申请驳回：员工%s，%s%d天%n",
                        approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveType(), leaveRequest.getLeaveDays());
            }
        }
    }
}

/**
 * 具体处理者3：总经理（处理7天以内所有类型请假）
 */
public class GeneralManager extends LeaveApprover {
    public GeneralManager(String approverName) {
        super(approverName);
    }

    @Override
    public void processRequest(LeaveRequest leaveRequest) {
        if (leaveRequest.getLeaveDays() <= 7) {
            System.out.printf("【%s】审批通过：员工%s，%s%d天%n",
                    approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveType(), leaveRequest.getLeaveDays());
        } else {
            if (nextApprover != null) {
                nextApprover.processRequest(leaveRequest);
            } else {
                System.out.printf("【%s】无后续审批人，请假申请驳回：员工%s，%s%d天（超过7天上限）%n",
                        approverName, leaveRequest.getEmpName(), leaveRequest.getLeaveType(), leaveRequest.getLeaveDays());
            }
        }
    }
}
```

### 4. 客户端测试（构建职责链并发送请求）

```java

/**
 * 客户端：构建职责链并发起请假请求
 */
public class Client {
    public static void main(String[] args) {
        // 1. 创建各个具体处理者
        LeaveApprover groupLeader = new GroupLeader("组长-张三");
        LeaveApprover deptManager = new DepartmentManager("部门经理-李四");
        LeaveApprover generalManager = new GeneralManager("总经理-王五");

        // 2. 构建职责链：组长 → 部门经理 → 总经理（设置下一个处理者）
        groupLeader.setNextApprover(deptManager);
        deptManager.setNextApprover(generalManager);

        // 3. 发起不同的请假请求
        LeaveRequest request1 = new LeaveRequest("小明", 1, "事假"); // 组长处理
        LeaveRequest request2 = new LeaveRequest("小红", 2, "事假"); // 部门经理处理
        LeaveRequest request3 = new LeaveRequest("小刚", 5, "病假"); // 总经理处理
        LeaveRequest request4 = new LeaveRequest("小丽", 10, "事假"); // 超出上限，驳回

        // 4. 统一向链首（组长）提交请求
        groupLeader.processRequest(request1);
        groupLeader.processRequest(request2);
        groupLeader.processRequest(request3);
        groupLeader.processRequest(request4);
    }
}
```

### 5. 运行结果

```text

【组长-张三】审批通过：员工小明，事假1天
【部门经理-李四】审批通过：员工小红，事假2天
【总经理-王五】审批通过：员工小刚，病假5天
【总经理-王五】无后续审批人，请假申请驳回：员工小丽，事假10天（超过7天上限）
```

## 四、关键执行流程

1. **构建链条**：客户端通过`setNextApprover`方法，将各个具体处理者按业务逻辑顺序串联成职责链（链首→链中→链尾）；

2. **发起请求**：客户端仅需将请求发送给**链首的处理者**，无需关注后续的处理节点；

3. **链式传递**：请求从链首开始，依次经过各个处理者：
        

    - 若当前处理者能处理请求，处理后终止传递；

    - 若不能处理，将请求转发给下一个处理者；

4. **终止条件**：要么请求被某个处理者处理，要么遍历完整个链（无后续处理者）后终止（通常驳回请求）。

## 五、优缺点

### 优点

1. 解耦性强：请求发送者无需知晓请求的处理者是谁，也无需知晓处理流程，仅需面向抽象处理者编程；

2. 灵活性高：可灵活新增、删除或调整处理者的顺序，无需修改原有业务逻辑，符合“开闭原则”；

3. 责任明确：每个具体处理者仅需处理自身职责范围内的请求，职责单一，便于维护。

### 缺点

1. 性能损耗：若职责链过长，请求遍历链的时间会增加，影响系统性能；

2. 请求可能丢失：若职责链构建不完整（如链尾未处理边界情况），或处理者逻辑异常，可能导致请求未被处理且无提示；

3. 调试难度增加：请求的处理路径是动态的，排查问题时需要跟踪整个链条的传递过程。

## 六、典型应用场景

1. 审批流程系统：如请假审批、报销审批、合同审批等（不同层级角色处理不同额度/类型的审批）；

2. 异常处理机制：Java的异常捕获（`try-catch`链式结构，父类异常可以捕获子类异常，本质是职责链）；

3. 过滤器/拦截器链：如Servlet的`Filter`链、Spring MVC的`Interceptor`链（依次对请求进行过滤/拦截处理）；

4. 日志分级输出：如DEBUG、INFO、WARN、ERROR级别日志，不同日志处理器处理对应级别的日志信息。

### 总结

1. 职责链模式的核心是**链式解耦**，请求发送者与接收者无直接依赖，请求沿链传递直至被处理；

2. 核心角色为抽象处理者、具体处理者、请求对象，通过`setNextApprover`构建链条；

3. 客户端仅需向链首提交请求，处理者自行判断是否处理或转发，流程灵活可扩展；

4. 适用于审批、过滤、异常处理等需要多级递进处理的场景，需注意控制链条长度以避免性能问题。
> （注：文档部分内容可能由 AI 生成）
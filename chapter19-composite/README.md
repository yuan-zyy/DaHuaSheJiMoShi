## 第19章 组合模式

### 19.1 组合模式
**组合模式（Composite Pattern）**

#### 一、组合模式核心定义
**组合模式（Composite Pattern）**是一种**结构型设计模式**，它的核心作用是: **将对象组合成树形结构以表示 “部分-整体” 的层次结构**，使得客户端对单个对象（叶子节点）和组合对象（容器节点）的使用具有一致性（客户端无需区分两者，统一调用相同的接口方法）

简单来说，组合模式让 “单个元素” 和 “元素集合“ 在客户端看来没有区别，极大简化了层次化结构的操作逻辑

#### 二、组合模式的核心角色
组合模式包含 3 个关键角色，三者协同构成树形层次结构
##### 1. 抽象组件(Component)
定义所有对象（叶子节点 + 容器节点）的统一接口，声明了叶子对象和组合对象共同需要实现的方法（如业务方法、添加/移除子组件的方法，其中添加/移除方法可在叶子节点中默认抛出异常或空实现），是客户端统一调用的基础

##### 2. 叶子节点(Leaf)
树形结构中的最小单元，**没有子节点**，实现了抽象组件定义的核心业务方法，对于添加/移除子组件的方法无需实现（可直接抛出不支持操作的异常或空实现）

##### 3. 容器节点(Composite)
又称组合对象，**包含子组件（可以是叶子节点，也可以是其他容器节点）**，它实现了抽象组件的所有方法：一方面实现业务方法时，会递归调用其子组件的对应业务方法，另一方面实现添加（add）、移除（remove）、获取（getChild）子组件的方法，用于管理子组件

#### 三、完整 Java 代码实现
下面以 “公司组织架构” 为例（公司 -> 部门 -> 成员，其中公司和部门都是容器节点，成员是叶子节点），实现组合模式
##### 1. 抽象组件(Component) - 组织组件
```java
/**
 * 抽象组件: 定义组织节点的统一接口
 */
public abstract class OrganizationComponent {
    // 节点名称
    protected String name;
    // 节点描述
    protected String desc;
    
    public OrganizationComponent(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }
    
    // 添加子组件（默认实现，叶子节点可重写抛出异常）
    public void add(OrganizationComponent organizationComponent) {
        throw new UnsupportedOperationException("当前节点不支持添加子组件操作");
    }
    
    // 移除子组件（默认实现，叶子节点可重写抛出异常）
    public void remove(OrganizationComponent organizationComponent) {
        throw new UnsupportedOperationException("当前节点不支持移除子组件操作");
    }
    
    // 获取指定索引的子组件（默认实现，叶子节点可重写抛出异常）
    public OrganizationComponent getChild(int index) { 
        throw new UnsupportedOperationException("当前节点不支持获取子组件操作");
    }
    
    // 核心业务方法: 打印组织信息（所有节点必须实现）
    public abstract void print();
}
```

##### 2. 叶子节点(Leaf) - 员工
```java
/**
 * 叶子节点: 员工（没有子节点）
 */
public class Employee extends OrganizationComponent {
    // 员工职位
    private String position;
    
    public Employee(String name, String desc, String position) {
        super(name, desc);
        this.position = position;
    }
    
    // 实现核心业务方法: 打印员工信息
    @Override
    public void print() {
        System.out.println("|___员工: " + name + ", 描述: " + desc + ", 职位: " + position);
    }
    
    // 叶子节点无需重写 add/remove/getChild 方法，直接使用父类的异常实现即可
}
```

##### 3. 容器节点(Composite) - 部门/公司（统一实现，因为都包含子节点）
```java
/**
 * 容器节点: 组织（公司/部门，包含子节点）
 */
public class Organization extends OrganizationComponent {
    // 存储子组件（可以是 Organization，也可以是 Employee）
    private List<OrganizationComponent> children = new ArrayList<>();
    
    public Organization(String name, String desc) {
        super(name, desc);
    }
    
    // 实现添加子组件的方法
    @Override
    public void add(OrganizationComponent organizationComponent) {
        children.add(organizationComponent);
    }
    
    // 实现移除子组件的方法
    @Override
    public void remove(OrganizationComponent organizationComponent) {
        children.remove(organizationComponent);
    }
    
    // 实现获取子组件的方法
    @Override
    public OrganizationComponent getChild(int index) {
        if (index < 0 || index >= children.size()) {
            throw new IndexOutOfBoundsException("子组件索引越界");
        }
        return children.get(index);
    }
    
    // 实现核心业务方法: 递归打印自身及所有子组件信息
    @Override
    public void print() {
        // 打印当前组织信息
        System.out.println("组织: " + name + ", 描述: " + desc);
        // 递归打印所有子组件
        for (OrganizationComponent child : children) {
            child.print();
        }
    }
}
```

##### 4. 客户端测试类
```java
/**
 * 客户端：统一操作单个对象（员工）和组合对象（公司/部门）
 */
public class CompositeClient {
    public static void main(String[] args) { 
        // 1. 创建根容器: 总公司
        OrganizationComponent rootCompany = new Organization("XX科技有限公司", "国内最牛的科技公司");
        
        // 2. 创建一级子容器: 技术部、市场部
        OrganizationComponent techDepartment = new Organization("技术部", "负责产品研发和技术维护");
        OrganizationComponent marketingDepartment = new Organization("市场部", "负责产品推广和客户对接");
        
        // 3. 创建叶子节点: 技术部员工、市场部员工
        Employee techEmployee1 = new Employee("张三", "资深研发工程师", "Java");
        Employee techEmployee2 = new Employee("李四", "高级研发工程师", "Web");
        Employee marketingEmployee1 = new Employee("王五", "市场经理", "客户扩展");
        Employee marketingEmployee2 = new Employee("赵六", "市场专员", "活动执行");
        
        // 4. 组装树形结构
        techDepartment.add(techEmployee1);
        techDepartment.add(techEmployee2);
        marketingDepartment.add(marketingEmployee1);
        marketingDepartment.add(marketingEmployee2);
        
        rootCompany.add(techDepartment);
        rootCompany.add(marketingDepartment);
        
        // 5. 客户端统一调用 print 方法，无需区分是容器节点还是叶子节点
        System.out.println("=== 打印完整组织架构 ===");
        rootCompany.print();
        
        // 6. 单独操作叶子节点（员工）
        System.out.println("=== 单独打印员工信息 ===");
        techEmployee1.print();
        
        // 7. 操作容器节点的子组件
        System.out.println("=== 获取技术部分第一个员工信息 ===");
        OrganizationComponent firstTechEmployee = techDepartment.getChild(0);
        firstTechEmployee.print();
    }
}
```

##### 5. 运行结果
```text
=== 打印完整组织架构 ===
组织：XX科技有限公司，描述：国内领先的互联网科技公司
组织：技术部，描述：负责产品研发和技术维护
└── 员工：张三，描述：资深研发工程师，职位：后端开发
└── 员工：李四，描述：高级研发工程师，职位：前端开发
组织：市场部，描述：负责产品推广和客户对接
└── 员工：王五，描述：市场经理，职位：客户拓展
└── 员工：赵六，描述：市场专员，职位：活动执行

=== 单独打印员工信息 ===
└── 员工：张三，描述：资深研发工程师，职位：后端开发

=== 获取技术部第一个员工信息 ===
└── 员工：张三，描述：资深研发工程师，职位：后端开发
```

#### 四、关键特性说明
1. **树形结构与递归操作**：容器节点的业务方法（如print()）通过**递归调用其子组件的对应方法**，实现了对整个树形结构的遍历和操作，这是组合模式处理层次结构的核心技巧。
2. **客户端一致性**：客户端仅需面向OrganizationComponent抽象接口编程，无需判断当前对象是叶子节点（Employee）还是容器节点（Organization），统一调用print()、add()等方法，极大简化了客户端代码。
3. **灵活性拓展**：新增叶子节点（如 “实习生”）或容器节点（如 “分公司”）时，无需修改现有客户端代码和抽象组件，仅需新增对应实现类，符合 “开闭原则”

#### 五、组合模式的适用场景
1. 当需要表示对象的 “部分 - 整体” 层次结构（如树形结构：组织架构、文件系统、菜单导航、树形控件等）时；
2. 当希望客户端统一处理单个对象和组合对象，无需区分两者类型时；
3. 当需要灵活拓展层次结构，新增节点类型不影响现有系统时

**典型案例**
- **文件系统**：文件夹（容器节点，包含文件 / 子文件夹）、文件（叶子节点）；
- **菜单系统**：主菜单（容器节点，包含子菜单 / 菜单项）、菜单项（叶子节点）；
- **树形控件**：树形列表的父节点（容器）、子节点（叶子）

#### 总结
1. 组合模式是结构型模式，核心是构建树形结构表示 “部分 - 整体” 关系，实现客户端对单个对象和组合对象的统一操作；
2. 三大核心角色：抽象组件（Component）、叶子节点（Leaf，无子节点）、容器节点（Composite，有子节点 + 递归操作）；
3. 关键特性：树形结构、递归遍历、客户端一致性、符合开闭原则；
4. 核心优势：简化客户端代码，灵活拓展层次化结构，统一操作逻辑


### n.2 

#### 一、

#### 二、

#### 总结

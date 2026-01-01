## 第18章 备忘录模式

### 18.1 备忘录模式
***备忘录模式（Memento Pattern）***

#### 一、核心概述
***备忘录模式（Memento Pattern）***是一种***行为型设计模式***，其核心目的是：在不破坏封装性的前提下，捕获一个对象的内部状态，并在该对象之外保存这个状态，以便后续需要时能对该对象恢复到原先保存的状态。

简单来说，备忘录模式就是为对象提供 “存档” 和 “读档” 的能力，比如游戏中的存档功能、办公软件的撤销操作（Ctrl+Z）都是备忘录模式的典型应用。

#### 二、备忘录模式的三个核心角色
备忘录模式包含三个必不可少的角色，三者分工明确、相互配合完成状态的保存与恢复

##### 1. 原发器(Originator)
- 核心职责: 创建备忘绿对象，用于保存自身当前的内部状态；同时也能通过备忘录对象恢复自身之前的状态
- 特色: 自身持有业务数据和状态，是需要被存档/恢复的目标对象

##### 2. 备忘录(Memento)
- 核心职责: 专门用于存储原发器的内部状态，相当于 “存档文件”
- 特色: 为了保证原发器的封装性，备忘录一般只对原发器开放访问权限，对其他对象隐藏内部细节（通常通过访问权限控制实现）

##### 3. 管理者/负责人(Caretaker)
- 核心职责: 负责保存备忘录对象，但 “不能修改或访问备忘录的内部状态”（仅做 “保管” 工作，不做 “使用” 操作）
- 特点: 可以存储多个备忘录对象(比如游戏的多个存档位)，提供备忘录的存取接口

#### 三、完整Java代码实现
下面以 “用户信息存档与恢复” 为例，实现备忘录模式，清晰展示三个角色的协作
##### 1. 原发器(Originator) - UserInfo(用户信息类)
```java
/**
 * 原发器: 用户信息类(需要被存档和恢复的对象)
 */
public class UserInfo {
    // 内部状态: 用户核心数据
    private String userId;
    private String userName;
    private String userPhone;
    
    public UserInfo(String userId, String userName, String userPhone) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
    }
    
    // 核心方法1: 创建备忘录，保存当前内部状态
    public UserMemento createMemento() {
        // 将当前对象的所有状态传入备忘录，完成存档
        return new UserMemento(userId, userName, userPhone);
    }
    
    // 核心方法2: 通过备忘录对象，恢复之前的状态
    public void restoreMemento(UserMemento memento) {
        // 从备忘录中读取存档状态，赋值给当前对象
        userId = memento.getUserId();
        userName = memento.getUserName();
        userPhone = memento.getUserPhone();
    }
    
    // 业务方法: 修改用户信息(用于演示状态变化)
    public void updateUserInfo(String userName, String userPhone) {
        this.userName = userName;
        this.userPhone = userPhone;
    }
    
    // 打印用户信息(用户验证状态)
    @Override
    public String toString() {
        return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                '}';
    }
    
    // getter (仅用于备忘录读取，外部无需直接访问)
    public String getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getUserPhone() {
        return userPhone;
    }
}
```

##### 2. 备忘录(Memento) - UserMemento(用户信息备忘录)
```java
/**
 * 备忘录: 存储用户信息的存档对象
 * 仅对原发器开放访问（通过包访问权限或私有构造 + 原发器友元实现，此处用包访问权限简化）
 */
public class UserMemento {
    // 存储原发器的内部状态(与原发器状态一一对应)
    private String userId;
    private String userName;
    private String userPhone;
    
    // 构造方法: 仅允许原发器调用，封装状态赋值
    public UserMemento(String userId, String userName, String userPhone) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
    }
    
    // 仅提供 getter 方法，不提供 setter 方法，保证状态不可修改
    public String getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getUserPhone() {
        return userPhone;
    }
}
```

##### 3. 管理者/负责人(Caretaker) - UserMementoCaretaker (用户信息备忘录管理者)
```java
/**
 * 管理者: 负责保管备忘录对象，不访问其内部状态
 */
public class UserMementoCaretaker {
    // 存储多个备忘录(支持多存档，比如用户的历史信息版本)
    private List<UserMemento> mementoList = new ArrayList<>();
    
    // 保存备忘录
    public void saveMemento(UserMemento memento) {
        mementoList.add(memento);
        System.out.println("已保存用户存档，当前存档数量: " + mementoList.size());
    }
    
    // 获取指定索引的备忘录（恢复指定存档）
    public UserMemento getMemento(int index) {
        if (index < 0 || index >= mementoList.size()) {
            System.out.println("无效存档索引");
            return null;
        }
        return mementoList.get(index);
    }
    
    // 删除指定备忘录
    public void removeMemento(int index) {
        if (index < 0 || index >= mementoList.size()) {
            System.out.println("无效存档索引");
            return;
        }
        mementoList.remove(index);
        System.out.println("已删除用户存档，当前存档数量: " + mementoList.size());
    }
}
```

##### 4. 测试类(验证备忘录供能)
```java
public class MementoPatternTest {
    public static void main(String[] args) {
        // 1. 创建原发器: 初始用户信息
        UserInfo userInfo = new UserInfo("1001", "张三", "13800000001");
        System.out.println("初始用户信息: " + userInfo);
        
        // 2. 创建管理者: 用于管理用户信息存档
        UserMementoCaretaker caretaker = new UserMementoCaretaker();
        
        // 3. 保存初始状态(存档)
        caretaker.saveMemento(userInfo.createMemento());
        
        // 4. 修改用户信息(状态变化)
        userInfo.updateUserInfo("张三-修改", "13800000002");
        System.out.println("修改后的用户信息: " + userInfo);
        
        // 5. 在保存一次修改后的状态(第二个存档)
        caretaker.saveMemento(userInfo.createMemento());
        
        // 6. 再次修改用户信息(状态再次变化)
        userInfo.updateUserInfo("张三-修改-再次", "13800000003");
        System.out.println("再次修改后的用户信息: " + userInfo);
        
        // 7. 恢复到初始状态(读取第一个存档，索引0)
        UserMemento initMemento = caretaker.getMemento(0);
        if (initMemento != null) {
            userInfo.restoreMemento(initMemento);
            System.out.println("恢复到初始状态: " + userInfo);
        }
        
        // 8. 恢复到第一次修改后的状态(读取第二个存档，索引1)
        UserMemento firstMemento = caretaker.getMemento(1);
        if (firstMemento != null) {
            userInfo.restoreMemento(firstMemento);
            System.out.println("恢复到第一次修改后的状态: " + userInfo);
        }
    }
}
```

##### 5. 运行结果
```text
初始状态：用户信息：{用户ID='U001', 用户名='张三', 手机号='13800138000'}
已保存用户存档，当前存档数量：1
修改后状态：用户信息：{用户ID='U001', 用户名='张三三', 手机号='13900139000'}
已保存用户存档，当前存档数量：2
再次修改后状态：用户信息：{用户ID='U001', 用户名='张三三三', 手机号='13700137000'}
恢复到初始状态：用户信息：{用户ID='U001', 用户名='张三', 手机号='13800138000'}
恢复到第一次修改后的状态：用户信息：{用户ID='U001', 用户名='张三三', 手机号='13900139000'}
```

#### 四、备忘录模式的适用场景
当你遇到以下场景时，适合使用备忘录模式
1. 需要保存对象的内部状态，以便后续恢复到该状态（如游戏存档、撤销操作、数据备份）。
2. 不希望暴露对象的内部实现细节（备忘录封装了状态，仅原发器可访问，保证封装性）。
3. 需要避免使用 “全局变量” 或 “静态变量” 存储状态，防止状态污染和线程安全问题。
4. 状态保存的频率不高，且状态数据量适中（若状态过大，会增加内存开销）

#### 五、备忘录的优缺点
##### 1. 优点
- 保证对象封装性：备忘录封装了原发器的内部状态，外部对象（除管理者外）无法访问，符合封装原则。
- 实现状态的灵活管理：可以保存多个历史状态，支持恢复到任意指定状态，扩展性强。
- 职责单一：原发器负责业务逻辑和状态创建 / 恢复，管理者负责备忘录保管，备忘录负责状态存储，符合单一职责原则

##### 2. 缺点
- 内存开销较大：若原发器状态数据量大，或需要保存大量备忘录对象（如频繁存档），会占用较多内存。
- 原发器状态更新成本高：若原发器的状态字段发生变化，备忘录类也需要同步修改，增加了维护成本。
- 可能影响性能：频繁创建和保存备忘录对象，会带来一定的性能开销

#### 总结
1. 备忘录模式是行为型模式，核心是不破坏封装性的前提下保存和恢复对象状态。
2. 三大核心角色：原发器（创建 / 恢复状态）、备忘录（存储状态）、管理者（保管备忘录）。
3. 核心价值：实现对象的 “存档 / 读档”，典型应用为撤销操作、游戏存档。
4. 取舍点：灵活的状态管理与内存 / 性能开销之间的平衡，适用于状态保存频率不高、数据量适中的场景


### n.2 

#### 一、

#### 二、

#### 总结

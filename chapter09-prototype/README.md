## 第9章 原型模式

### 9.1 原型模式
***原型模式（Prototype Pattern）***

#### 一、核心概述
##### 1. 核心定义
***原型模式（Prototype Pattern）***是一种***创建型设计模式***，其核心思想是: ***用一个已经创建的实例作为原型，通过复制(克隆)该原型来创建一个和原型相同或相似的新对象***，而无需重新通过构造函数初始化对象，简化对象创建流程，

##### 2. 核心解决问题
- 避免频发创建复杂对象时的性能损耗(如对象初始化需要大量IO操作、数据库查询等)
- 简化对象创建逻辑，无需关注对象的构造细节
- 解决 "创建相似对象时，重复代码过多" 的问题

##### 3. 核心角色
- ***抽象原型(Prototype)***: 定义克隆自身的方法接口(通常是Java中的 Cloneable 接口，或则自定义接口)
- ***具体原型(Concrete Prototype)***: 实现抽象原型的克隆方法，是被克隆的具体对象
- ***客户端(Client)***: 通过调用具体原型的克隆方法，创建新的对象

#### 二、Java 实现原型模式的核心基础
Java 中实现原型模式的核心是 Coneable 接口 和 Object.clone() 方法，二者缺一不可
1. Cloneable 接口：是一个标记接口（无任何抽象方法），用于标记该类的对象可以被克隆。如果一个类未实现Cloneable接口，调用clone()方法会抛出 CloneNotSupportedException 异常
2. Object.clone() 方法：Java 中所有类的父类Object提供的本地方法（native），用于实现对象的浅克隆，返回该对象的一个副本
3. 克隆方法的重写：具体原型类需要重写Object.clone()方法，指定返回值类型，并处理CloneNotSupportedException异常

#### 三、原型模式的两种实现：浅克隆 vs 深克隆
##### 1. 浅克隆(Shallow Clone)
***定义***</br>
浅克隆是默认的克隆方式，克隆对象时，***只复制对象本身（基本数据类型成员变量），而对于引用数据类型成员变量，仅复制其引用地址，不复制引用指向的实际对象***。因此，原型对象和克隆对象的引用类型成员变量会指向同一个内存地址，修改其中一个会影响另一个。

浅克隆代码示例:
```java
import java.util.ArrayList;
import java.util.List;

// 具体原型类：实现Cloneable接口，重写clone方法
class User implements Cloneable {
    // 基本数据类型成员变量
    private Integer id;
    private String username;
    // 引用数据类型成员变量
    private List<String> hobbies;

    // 构造方法
    public User(Integer id, String username, List<String> hobbies) {
        this.id = id;
        this.username = username;
        this.hobbies = hobbies;
    }

    // 重写clone方法，实现浅克隆
    @Override
    protected User clone() throws CloneNotSupportedException {
        // 调用Object的clone方法，返回克隆对象
        return (User) super.clone();
    }

    // getter/setter 省略
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getHobbies() { return hobbies; }
    public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', hobbies=" + hobbies + "}";
    }
}

// 客户端测试类
public class ShallowCloneTest {
    public static void main(String[] args) throws CloneNotSupportedException {
        // 1. 创建原型对象
        List<String> hobbies = new ArrayList<>();
        hobbies.add("篮球");
        hobbies.add("编程");
        User prototypeUser = new User(1, "张三", hobbies);

        // 2. 克隆原型对象（浅克隆）
        User cloneUser = prototypeUser.clone();

        // 3. 打印原型对象和克隆对象
        System.out.println("原型对象：" + prototypeUser);
        System.out.println("克隆对象：" + cloneUser);
        System.out.println("原型对象 == 克隆对象？" + (prototypeUser == cloneUser)); // false（对象本身不同）

        // 4. 修改引用类型成员变量（验证浅克隆）
        cloneUser.getHobbies().add("阅读");
        System.out.println("\n修改克隆对象的引用类型成员变量后：");
        System.out.println("原型对象：" + prototypeUser); // 原型对象的hobbies也被修改了
        System.out.println("克隆对象：" + cloneUser);
    }
}
```

***运行结果***
```text
原型对象：User{id=1, username='张三', hobbies=[篮球, 编程]}
克隆对象：User{id=1, username='张三', hobbies=[篮球, 编程]}
原型对象 == 克隆对象？false

修改克隆对象的引用类型成员变量后：
原型对象：User{id=1, username='张三', hobbies=[篮球, 编程, 阅读]}
克隆对象：User{id=1, username='张三', hobbies=[篮球, 编程, 阅读]}
```
##### 2. 深克隆(Deep Clone)
***定义***
深克隆时，***不仅复制对象本身和基本数据类型成员变量，还会复制所有引用数据类型成员变量指向的实际对象***。原型对象和克隆对象的引用类型成员变量指向不同的内存地址，修改其中一个不会影响另一个，实现完全独立的对象复制

***Java 实现深克隆的常用方式*** </br>
***方式1: 重写 clone() 方法，手动克隆引用类型成员变量*** </br>
在浅克隆的基础上，对引用类型成员变量单独进行克隆，实现深克隆
```java
import java.util.ArrayList;
import java.util.List;

class UserDeepClone implements Cloneable {
    private Integer id;
    private String username;
    private List<String> hobbies;

    public UserDeepClone(Integer id, String username, List<String> hobbies) {
        this.id = id;
        this.username = username;
        this.hobbies = hobbies;
    }

    // 重写clone方法，实现深克隆
    @Override
    protected UserDeepClone clone() throws CloneNotSupportedException {
        // 1. 先执行浅克隆，得到对象本身的副本
        UserDeepClone cloneUser = (UserDeepClone) super.clone();

        // 2. 手动克隆引用类型成员变量（hobbies），实现深克隆
        List<String> newHobbies = new ArrayList<>();
        if (this.hobbies != null) {
            newHobbies.addAll(this.hobbies); // 复制hobbies中的元素，创建新的List对象
        }
        cloneUser.setHobbies(newHobbies);

        return cloneUser;
    }

    // getter/setter 省略
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getHobbies() { return hobbies; }
    public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }

    @Override
    public String toString() {
        return "UserDeepClone{id=" + id + ", username='" + username + "', hobbies=" + hobbies + "}";
    }
}

// 客户端测试类
public class DeepCloneTest {
    public static void main(String[] args) throws CloneNotSupportedException {
        // 1. 创建原型对象
        List<String> hobbies = new ArrayList<>();
        hobbies.add("篮球");
        hobbies.add("编程");
        UserDeepClone prototypeUser = new UserDeepClone(1, "张三", hobbies);

        // 2. 克隆原型对象（深克隆）
        UserDeepClone cloneUser = prototypeUser.clone();

        // 3. 打印原型对象和克隆对象
        System.out.println("原型对象：" + prototypeUser);
        System.out.println("克隆对象：" + cloneUser);
        System.out.println("原型对象 == 克隆对象？" + (prototypeUser == cloneUser)); // false

        // 4. 修改克隆对象的引用类型成员变量（验证深克隆）
        cloneUser.getHobbies().add("阅读");
        System.out.println("\n修改克隆对象的引用类型成员变量后：");
        System.out.println("原型对象：" + prototypeUser); // 原型对象的hobbies未被修改
        System.out.println("克隆对象：" + cloneUser);
    }
}
```

***方式 2：通过序列化（Serializable）实现深克隆*** </br>
将对象序列化到字节流中，再从字节流中反序列化出一个新对象，这种方式会自动实现深克隆（所有引用类型成员变量需同时实现Serializable接口）
```java
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// 实现Serializable接口，支持序列化
class UserSerializable implements Serializable {
    private static final long serialVersionUID = 1L; // 序列化版本号
    private Integer id;
    private String username;
    private List<String> hobbies; // List本身实现了Serializable

    public UserSerializable(Integer id, String username, List<String> hobbies) {
        this.id = id;
        this.username = username;
        this.hobbies = hobbies;
    }

    // 实现深克隆的方法（序列化+反序列化）
    public UserSerializable deepClone() throws IOException, ClassNotFoundException {
        // 1. 序列化对象到字节数组输出流
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);

        // 2. 反序列化字节数组，得到新对象
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (UserSerializable) ois.readObject();
    }

    // getter/setter 省略
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getHobbies() { return hobbies; }
    public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }

    @Override
    public String toString() {
        return "UserSerializable{id=" + id + ", username='" + username + "', hobbies=" + hobbies + "}";
    }
}

// 测试类
public class SerializableDeepCloneTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // 1. 创建原型对象
        List<String> hobbies = new ArrayList<>();
        hobbies.add("篮球");
        hobbies.add("编程");
        UserSerializable prototypeUser = new UserSerializable(1, "张三", hobbies);

        // 2. 深克隆对象
        UserSerializable cloneUser = prototypeUser.deepClone();

        // 3. 验证深克隆
        cloneUser.getHobbies().add("阅读");
        System.out.println("原型对象：" + prototypeUser);
        System.out.println("克隆对象：" + cloneUser);
    }
}
```

***运行结果***
```text
原型对象：UserSerializable{id=1, username='张三', hobbies=[篮球, 编程]}
克隆对象：UserSerializable{id=1, username='张三', hobbies=[篮球, 编程, 阅读]}
```

#### 四、原型模式的优缺点
1. 优点
   - 性能优异：克隆对象比通过new关键字创建对象（尤其是复杂对象）效率更高，避免了重复的初始化逻辑。
   - 简化对象创建：客户端无需关注对象的构造细节，只需通过克隆原型即可创建新对象，降低了对象创建的耦合度。
   - 支持动态创建对象：可以在运行时动态修改原型对象，再通过克隆创建符合需求的新对象，灵活性更高。
   - 便于创建相似对象：对于仅少量属性不同的相似对象，只需克隆原型后修改差异属性，减少重复代码。
2. 缺点
    - 克隆方法实现复杂：对于包含多层引用类型成员变量的对象，实现深克隆时需要逐层克隆（手动重写clone()）或依赖序列化，代码复杂度较高。
   - 违反 “开闭原则”：如果新增引用类型成员变量，需要修改克隆方法，手动添加该成员变量的克隆逻辑。
   - 对final成员变量不友好：final修饰的引用类型成员变量无法实现深克隆（无法重新赋值指向新对象）。
   
#### 五、原型模式的应用场景
1. ***创建复杂对象时***：对象初始化需要大量资源（如 IO、数据库连接、网络请求），克隆可以避免重复消耗这些资源。
2. ***需要创建大量相似对象时***：如批量生成用户对象、订单对象，仅部分属性不同，通过克隆原型 + 修改差异属性实现。
3. ***运行时动态创建对象时***：无法在编译期确定对象类型，需在运行时通过克隆原型创建对象（如插件扩展、动态代理）。
4. ***避免使用构造函数创建对象时***：如构造函数参数过多，或构造逻辑复杂，克隆更简洁

#### 六、总结
1. 原型模式是创建型模式，核心是***克隆原型对象创建新对象***，依赖Cloneable接口和Object.clone()方法。
2. 克隆分为浅克隆（仅复制引用，对象共享引用类型成员）和深克隆（完全复制所有对象，相互独立）。
3. 深克隆可通过 “手动克隆引用成员” 或 “序列化” 实现，序列化更适用于多层引用的复杂对象。
4. 原型模式的核心优势是***高性能、简化创建***，缺点是***克隆逻辑复杂、违反开闭原则***，适用于复杂对象或大量相似对象的创建场景


### n.2 

#### 一、

#### 二、

#### 总结

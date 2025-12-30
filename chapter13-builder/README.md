## 第13章 建造者模式 

### 13.1 建造者模式
***Java 建造者模式（Builder Pattern）***

#### 一、核心概念
建造者模式是一种***创建型设计模式***，它的核心思想是：***将一个复杂对象的构造过程和其表示分离，使用同样的构造过程可以创建不同的表示***。

简单来说，就是把对象的属性赋值、复杂逻辑构架和对象本身分离，由专门的 "建造者" 来负责构建对象，最终通过 "指挥者"(可选) 来统一构建流程，生成完整对象

#### 二、核心解决的问题
1. 当一个类由***大量可选属性***(比如用户对象有姓名、年龄、手机号、地址、邮箱等，大部分属性非必填项)时，避免出现参数列表过长的构造方法(即 "Telescoping Constructor" 反模式)，提高代码可读性和可维护性
2. ***解决复杂对象的构建逻辑分散问题，将构建步骤标准化，确保构建对象的一致性***
3. 支持分布构建，灵活控制对象的建造过程

#### 三、两种常见实例方式
##### 方式1 经典建造者模式（分离式，有独立 Builder 类）
经典模式包含 4 个核心角色：
- ***产品(Product)***：需要构建的复杂对象(如User、Computer)
- ***抽象建造者(Abstract Builder)***：定义对象构建的标准步骤（接口/抽象类）
- ***具体建造者(Concrete Builder)***：实现抽象建造者的方法，完成具体属性的赋值和构建
- ***指挥者(Director)***：统一调度建造者的构建步骤，无需关心具体构建细节（可选，简单场景可省略）

##### 方式2 内部静态建造者模式（常用，简单优雅）
这是 Java 开发中***最主流、最常用***的实现方式，将建造者类（Bulider）作为产品类的静态内部类，省略抽象建造者和指挥者，简化代码结构，使用链式调用提升开发体验

#### 四、完整代码示例（内部静态建造者模式，实战首选）
下面以 "用户对象（User）"为例，实现内部静态建造者模式，支持链式调用构建对象
1. 产品类 + 内部静态构造者
    ```java
    /**
     * 产品类：需要构建的复杂对象（User）
     */
    public class User { 
        // 必选属性（不可变，使用 final修饰）
        private final String username;
        private final String password;
        // 可选属性
        private Integer age;
        private String phone;
        private String email;
        private String address;
    
        /**
         * 私有构造方法：仅允许内部 Builder 类调用，确保对象构建的唯一性
         */
        private User(UserBuilder builder) {
            // 从构造者对象获取属性，赋值给产品对象
            this.username = builder.username;
            this.password = builder.password;
            this.age = builder.age;
            this.phone = builder.phone;
            this.email = builder.email;
            this.address = builder.address;
        }
    
        /**
         * 内部静态建造者类：与产品类耦合，简化调用
         */
        public static class UserBuilder {
            // 必选属性（无final，需要先复赋值）
            private String username;
            private String password;
            // 可选属性(初始化默认值，按需修改)
            private Integer age = null;
            private String phone = null;
            private String email = null;
            private String address = null;
    
            /**
             * 建造者构造方法：强制传入必选属性
             * @param username
             * @param password
             */
            public UserBuilder(String username, String password) {
                this.username = username;
                this.password = password;
            }
    
            /**
             * 可选属性赋值方法：返回Builder对象自身，支持链式调用
             * @param age
             * @return
             */
            public UserBuilder age(Integer age) {
                this.age = age;
                return this;
            }
            
            public UserBuilder phone(String phone) {
                this.phone = phone;
                return this;
            }
            
            public UserBuilder email(String email) {
                this.email = email;
                return this;
            }
            
            public UserBuilder address(String address) {
                this.address = address;
                return this;
            }
            
            public User build() {
                // 可选：在这里做参数校验（比如用户名非空、密码长度合法登）
                if (this.username == null || this.username.trim().isEmpty()) {
                    throw new IllegalArgumentException("用户名不能为空");
                }
                
                if (this.password == null || this.password.trim().isEmpty()) {
                    throw new IllegalArgumentException("密码不能为空");
                }
                return new User(this);
            }
            
        }
    
        // 省略getter方法（按需添加，建议只提供getter，保证对象不可变）
        public String getUsername() {
            return username;
        }
    
        public String getPassword() {
            return password;
        }
    
        public Integer getAge() {
            return age;
        }
    
        public String getPhone() {
            return phone;
        }
    
        public String getEmail() {
            return email;
        }
    
        public String getAddress() {
            return address;
        }
    
        @Override
        public String toString() {
            return "User{" +
                    "username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", age=" + age +
                    ", phone='" + phone + '\'' +
                    ", email='" + email + '\'' +
                    ", address='" + address + '\'' +
                    '}';
        }
    
    }
    ```
2. 测试调用（链式构建，简洁高效）
    ```java
    public class BuilderTest {
        public static void main(String[] args) {
            // 1. 构建仅包含必选属性的 User 对象
            User user1 = new User.UserBuilder("zhangsan", "123456").build();
            System.out.println("基础用户：" + user1);
            
            // 2. 构建包含必选+部分可选属性的User对象（链式调用，灵活组合）
            User fullUser = new User.UserBuilder("lisi", "87654321")
                    .age(25)
                    .phone("13800138000")
                    .email("lisi@example.com")
                    .address("北京市朝阳区")
                    .build();
            System.out.println("完整用户：" + fullUser);
        }
    }
    ```
3. 运行结果
```text
基础用户：User{username='zhangsan', password='12345678', age=null, phone='null', email='null', address='null'}
完整用户：User{username='lisi', password='87654321', age=25, phone='13800138000', email='lisi@example.com', address='北京市朝阳区'}
```

#### 五、关键特性说明
1. ***链式调用***：建造者的每个可选属性方法都返回this（当前 Builder 对象），从而支持连续的链式调用（.age(25).phone("138xxxx").email("xxxx@xx.com")），代码简洁易读
2. ***对象不可变性***：产品类（User）的属性使用 final 修饰，仅提供私有构造方法（由 Builder 调用） 和 getter 方法，没有 setter 方法，确保对象创建后无法被修改，线程安全
3. ***参数检验***：在 builder() 方法中可以添加参数合法性校验，避免创建无效对象（比如用户名不能为空、密码长度不合法登）
4. ***灵活配置***：按需选择可选属性，无需为不同属性组合创建多个构造方法，解决了 "构造方法爆炸" 问题

#### 六、经典建造者模式（补充、适用于复杂构建流程）
如果对象构建流程复杂且需要标准化，可使用经典模式（以 "电脑(Computer)" 为例）
1. ***产品类***

    ```java
    /**
     * 产品类：电脑
     */
    public class Computer {
        private String cpu;
        private String memory;
        private String hardDisk;
        private String graphicsCard;
    
        // setter & getter & toString
        public void setCpu(String cpu) {
            this.cpu = cpu;
        }
    
        public void setMemory(String memory) {
            this.memory = memory;
        }
    
        public void setHardDisk(String hardDisk) {
            this.hardDisk = hardDisk;
        }
    
        public void setGraphicsCard(String graphicsCard) {
            this.graphicsCard = graphicsCard;
        }
    
        @Override
        public String toString() {
            return "Computer{" +
                    "cpu='" + cpu + '\'' +
                    ", memory='" + memory + '\'' +
                    ", hardDisk='" + hardDisk + '\'' +
                    ", graphicsCard='" + graphicsCard + '\'' +
                    '}';
        }
    }
    ```

2. ***抽象建造者***
    ```java
    // 抽象建造者：定义电脑构建的标准化步骤
    public abstract class ComputerBuilder {
        // 构建CPu
        public abstract void buildCpu();
        // 构建内存
        public abstract void buildMemory();
        // 构建硬盘
        public abstract void buildHardDisk();
        // 构建显卡
        public abstract void buildGraphicsCard();
        // 返回构建好的电脑
        public abstract Computer getComputer();
    }
    ```
3. ***具体建造者（游戏本建造者）***
    ```java
    // 具体建造者：游戏本建造者（实现具体建造逻辑）
    public class GamingComputerBuilder extends ComputerBuilder {
        protected Computer computer = new Computer();
   
        @Override
        public void buildCpu() {
            computer.setCpu("Intel i9-14900HX");
        }
        
        @Override
        public void buildMemory() {
            computer.setMemory("32GB DDR5 6400");
        }
        
        @Override
        public void buildHardDisk() {
            computer.setHardDisk("2TB PCIe 4.0 SSD");
        }
        
        @Override
        public void buildGraphicsCard() {
            computer.setGraphicsCard("NVIDIA RTX 4090 Laptop");
        }
    }
    ```
4. ***指挥者***
    ```java
    // 指挥者：调度构建步骤，统一构建流程
    public class ComputerDirector {
        // 传入具体构建者，完成构建
        public Computer construct(ComputerBuilder computerBuilder) {
            computerBuilder.buildCpu();
            computerBuilder.buildMemory();
            computerBuilder.buildHardDisk();
            computerBuilder.buildGraphicsCard();
            return computerBuilder.getComputer();
        }
    }
    ```
5 ***测试调用***
```java
public class ClassBuilderTest {
    public static void main(String[] args) {
        // 1. 创建具体建造者（游戏本）
        ComputerBuilder computerBuilder = new GamingComputerBuilder();
        // 2. 创建指挥者
        ComputerDirector computerDirector = new ComputerDirector();
        // 3. 指挥者调度构建
        Computer computer = computerDirector.construct(computerBuilder);
        // 4. 获取构建结果
        System.out.println("构建结果：" + computer);
    }
}
```

#### 七、适用场景总结
1. ***当对象有大量可选属性，且属性组合灵活时（如用户、订单、配置对象），优先使用内部静态建造者模式***
2. ***当对象构建流程复杂且标准化，需要统一控制构建步骤时（如汽车组装、电脑组装），使用经典建造者模式（带 Director）***
3. ***当需要创建不可变对象（线程安全），且避免暴露 setter 方法时，建造者模式是最佳选择之一***

#### 八、与工厂模式的区别
- 工厂模式：关注 “批量创建同一类型对象”，无需关心对象内部属性细节，直接返回成品对象
- 建造者模式：关注 “分步构建复杂对象”，可以灵活配置对象的属性，控制构建流程，适用于属性复杂的对象

#### 总结
1. 建造者模式是创建型模式，核心是 “分离构建过程与表示”，解决复杂对象的构建问题。
2. ***Java 开发中，内部静态建造者模式（链式调用）是实战首选，简洁优雅，解决构造方法爆炸问题。***
3. 关键特性：链式调用、对象不可变性、参数校验、灵活配置。
4. 适用场景：大量可选属性的对象、复杂标准化构建流程、不可变对象创建。

### 13.2 经典建造者模式完整代码示例

#### 一、

#### 二、

#### 总结

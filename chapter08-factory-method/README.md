## 第8章 工厂方法模式 

### 8.1 工厂方法模式
***工厂方法模式 （Factory Method Pattern）***

#### 一、核心定义
***工厂方法模式***是一种***创建型设计模式***，它的核心作用是：***定义一个创建对象的接口（抽象方法），但将具体的对象创建逻辑延迟到其子类中实现***。简单来说，就是把对象的创建权交给子类，避免在父类中硬编码具体的产品类型，实现 “开闭原则”（对外开放，对修改关闭）

#### 二、核心角色
工厂方法模式包含 4 个固定核心角色，各司其职：
1. ***抽象产品(Product)***: 定义所有具体产品的公共接口/抽象类，规定了产品的核心行为和属性
2. ***具体产品(Concrete Product)***: 实现/继承抽象产品的具体类，是工厂方法最终要创建的对象实例
3. ***抽象工厂***: 定义工厂方法的接口/抽象类，该方法返回一个抽象产品类型的对象（不直接创建具体产品），可以包含与产品相关的业务逻辑
4. ***具体工厂(Concrete Creator)***: 实现/继承抽象工厂的具体类，重新工厂方法，返回具体的产品的实例，负责具体的对象创建逻辑

#### 三、完整代码实际示例
下面以 “电子产品生产” 为例，实现工厂方法模式，清晰展示各角色的对应关系
1. ***抽象产品(Product)-电子产品接口
    ```java
    // 抽象产品：定义电子产品的公共行为
    public class ElectronicProduct {
        // 产品功能：启动设备
        void startUp();
        // 产品功能：关闭设备
        void shutDown();
    }
    ```
2. 具体产品(Concrete Product)

    分别实现手机和电脑两种具体电子产品，对应抽象产品接口
    ```java
    // 具体产品1：手机
    public class MobilePhone implements ElectronicProduct {
        @Override
        public void startUp() {
            System.out.println("手机开机，加载操作系统...");
        }
    
        @Override
        public void shutDown() {
            System.out.println("手机关机，保存用户数据...");
        }
    }
    
    // 具体产品2：电脑
    public class Computer implements ElectronicProduct {
        @Override
        public void startUp() {
            System.out.println("电脑开机，自检硬件并加载系统...");
        }
    
        @Override
        public void shutDown() {
            System.out.println("电脑关机，关闭所有进程并断电...");
        }
    }
    ```
   
3. 抽象工厂(Creator) - 电子产品工厂接口
    ```java
    // 抽象工厂：定义创建电子产品的工厂方法
    public interface ElectronicFactory {
        // 工厂方法：返回抽象产品类型（ElectronicProduct）
        ElectronicProduct createProduct();
    }
    ```
4. 具体工厂 (Concrete Creator)

    分别实现手机工厂和电脑工厂，重写工厂方法创建对应具体产品
    ```java
    // 具体工厂1：手机工厂（负责创建手机对象）
    public class MobilePhoneFactory implements ElectronicFactory {
        @Override
        public ElectronicProduct createProduct() {
            // 具体产品创建逻辑，返回手机实例
            return new MobilePhone();
        }
    }
    
    // 具体工厂2：电脑工厂（负责创建电脑对象）
    public class ComputerFactory implements ElectronicFactory {
        @Override
        public ElectronicProduct createProduct() {
            // 具体产品创建逻辑，返回电脑实例
            return new Computer();
        }
    }
    ```
5. 客户端测试代码

    ```java
    public class FactoryMethodTest {
        public static void main(String[] args) {
            // 1. 创建手机工厂，生产手机产品
            ElectronicFactory mobileFactory = new MobilePhoneFactory();
            ElectronicProduct mobilePhone = mobileFactory.createProduct();
            mobilePhone.startUp();
            mobilePhone.shutDown();
    
            System.out.println("---------------------------");
            
            // 2. 创建电脑工厂，生产电脑产品
            ElectronicFactory computerFactory = new ComputerFactory();
            ElectronicProduct computer = computerFactory.createProduct();
            computer.startUp();
            computer.shutDown();
        }
    }
    ```
6. 运行结果
```text
手机开机，加载操作系统...
手机关机，保存用户数据...
------------------------
电脑开机，自检硬件并加载系统...
电脑关机，关闭所有进程并断电...
```

#### 四、关键特点与应用场景
1. 核心特点
   - 解耦: 将对象创建与业务逻辑分离，降低代码耦合度
   - 遵循开闭原则: 新增产品时，只需要新增 “具体产品类” 和 “具体工厂类”，无需修改原有代码
   - 单一职责: 抽象工厂负责定义创建规则，具体工厂负责具体创建，每个类只承担单一职责

2. 典型应用场景
   - 当你不知道需要创建那种具体对象的实例时（比如根据配置文件、用户输入动态创建对象）
   - 当你需要封装复杂的对象创建逻辑，避免重复代码时
   - 当你希望系统具有良好的扩展性，便于后续新增产品类型时（比如电商系统的支付方式、物流方式扩展）

#### 五、与简单工厂模式的区别(补充)
很多人会混淆工厂方法和简单工厂模式，核心区别如下 </br>

| 特性   | 简单工厂模式            | 工厂方法模式               |
|------|-------------------|----------------------|
| 核心特性 | 一个工厂类(非抽象) + 多个产品 | 抽象工厂 + 多个具体工厂 + 多个产品 |
| 扩展性  | 差(新增产品需要修改工厂类代码)  | 好(新增产品只需要新增工厂和产品类)   |
| 开闭原则 | 不遵循               | 遵循                   |
| 复杂度  | 简单(适合产品类型固定场景)    | 稍高(适合产品类型需扩展场景)      |

#### 总结
1. 工厂方法模式的核心是 “***抽象工厂定义创建接口，具体工厂实现创建逻辑***”，将对象创建延迟到子类
2. 四大核心角色：***抽象产品，具体产品，抽象工厂，具体工厂***，缺一不可
3. 核心优势是解耦和良好的扩展性，遵循开闭原则和单一职责原则，适用于产品类型需动态扩展的场景
4. 与简单工厂模式相比，虽然复杂度稍高，但扩展性更强，是更灵活的创建型模式

### n.2 

#### 一、

#### 二、

#### 总结

## 第21章 单例模式

### 21.1 单例模式 
**单例模式(Singleton Pattern)**

#### 一、核心定义
单例模式（Singleton Pattern）是 Java 中最基础的创建型设计模式之一，它的核心目标是：**保证一个类在整个应用程序的生命周期中，只有一个实例对象被创建**，并提供一个全局唯一的访问点来获取该实例

#### 二、核心特性
1. **构造方法私有（private）**：这是实现单例的基础，禁止外部通过new关键字直接创建类的实例，确保实例创建的控制权完全由类自身掌控。
2. **自身持有唯一实例**：在类内部声明并创建自身类型的静态唯一实例对象。
3. **提供全局访问点**：通过一个公有的静态方法（通常命名为getInstance()），向外部提供该唯一实例的访问入口

#### 三、常见实现方式
##### 1. 饿汉式（立即加载）
实现代码
```java
/**
 * 饿汉式单例 - 立即加载
 */
public class HungrySingleton {
    // 1. 类加载时就创建唯一实例（静态变量初始化，线程安全）
    private static final HungrySingleton INSTANCE = new HungrySingleton();

    // 2. 私有构造方法，禁止外部new创建
    private HungrySingleton() {}

    // 3. 公有静态方法，提供全局访问点
    public static HungrySingleton getInstance() {
        return INSTANCE;
    }
}
```

**优缺点**
- **优点**：实现简单，天然线程安全（类加载由 JVM 保证线程安全，只会创建一次实例），获取实例的速度快（无需加锁等额外操作）。
- **缺点**：属于 “立即加载”，无论后续是否会使用该实例，类加载时都会创建，可能造成内存浪费（比如该实例占用资源较多，但程序全程未使用）

#### 2. 懒汉式（延迟加载，非线程安全）
实现代码
```java
/**
 * 懒汉式单例 - 延迟加载（非线程安全）
 */
public class LazySingletonUnsafe {
    // 1. 声明静态实例变量，暂不初始化
    private static LazySingletonUnsafe instance;

    // 2. 私有构造方法
    private LazySingletonUnsafe() {}

    // 3. 公有静态方法，使用时才创建实例
    public static LazySingletonUnsafe getInstance() {
        // 存在线程安全问题：多线程同时进入if判断，会创建多个实例
        if (instance == null) {
            instance = new LazySingletonUnsafe();
        }
        return instance;
    }
}
```

**优缺点**
- **优点**：延迟加载，只有在首次调用getInstance()时才创建实例，节省内存资源。
- **缺点**：非线程安全，在多线程并发场景下，多个线程可能同时通过instance == null的判断，进而创建多个不同的实例，违背单例模式的核心要求，仅适用于单线程环境

#### 3. 懒汉式（延迟加载，线程安全 - 加锁方式）
为了解决非线程安全问题，可通过synchronized关键字加锁实现线程安全

实现代码
```java
/**
 * 懒汉式单例 - 延迟加载（线程安全，synchronized加锁）
 */
public class LazySingletonSafe {
    private static LazySingletonSafe instance;

    private LazySingletonSafe() {}

    // 给整个getInstance方法加锁，保证线程安全
    public static synchronized LazySingletonSafe getInstance() {
        if (instance == null) {
            instance = new LazySingletonSafe();
        }
        return instance;
    }
}
```

**优缺点**
- **优点**：延迟加载，线程安全，实现简单。
- **缺点**：性能较差。synchronized加在静态方法上，相当于给类对象加锁，每次调用getInstance()时，无论实例是否已创建，都会进行锁竞争，带来额外的性能开销

#### 4. 双重检查锁定（DCL，Double-Checked Locking，推荐）
这是对 “加锁懒汉式” 的优化，既保证线程安全，又兼顾性能，是实际开发中常用的单例实现方式

实现代码
```java
/**
 * 双重检查锁定（DCL）单例 - 延迟加载、线程安全、高性能
 */
public class DclSingleton {
    // 关键：使用volatile关键字修饰实例变量
    // 禁止指令重排，保证实例对象的初始化完成后再被其他线程可见
    private static volatile DclSingleton instance;

    private DclSingleton() {}

    public static DclSingleton getInstance() {
        // 第一次检查：实例已存在时，直接返回，无需进入锁逻辑，提升性能
        if (instance == null) {
            // 加锁：仅当实例未创建时，进行锁竞争
            synchronized (DclSingleton.class) {
                // 第二次检查：防止多个线程等待锁后，重复创建实例
                if (instance == null) {
                    // 此处若没有volatile，可能发生指令重排，导致其他线程获取到未完全初始化的实例
                    instance = new DclSingleton();
                }
            }
        }
        return instance;
    }
}
```

**关键说明**
- 必须使用volatile关键字修饰实例变量：instance = new DclSingleton()并非原子操作，会分为 “分配内存”“初始化对象”“引用指向内存地址” 三步，JVM 可能进行指令重排。若没有volatile，其他线程可能获取到 “已分配内存但未初始化完成” 的无效实例。
- 双重检查的意义：第一次检查避免已创建实例后的锁竞争，第二次检查避免多线程等待锁后重复创建实例，兼顾线程安全和高性能

**优缺点**
- **优点**：延迟加载、线程安全、高性能，是最优的单例实现方式之一。
- **缺点**：实现相对复杂，需要理解volatile关键字和指令重排的概念

##### 5. 静态内部类（推荐，更简洁）
利用 Java 类加载机制实现线程安全和延迟加载，代码更简洁优雅，也是实际开发中的推荐方案
```java
/**
 * 静态内部类单例 - 延迟加载、线程安全、简洁优雅
 */
public class StaticInnerClassSingleton {
    // 私有构造方法
    private StaticInnerClassSingleton() {}

    // 静态内部类：不会随着外部类的加载而加载，仅在被调用时才加载
    private static class SingletonHolder {
        // 内部类加载时，创建外部类的唯一实例（JVM保证线程安全）
        private static final StaticInnerClassSingleton INSTANCE = new StaticInnerClassSingleton();
    }

    // 全局访问点：调用时才触发SingletonHolder的加载
    public static StaticInnerClassSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```

**原理说明**
- 外部类StaticInnerClassSingleton加载时，静态内部类SingletonHolder不会被加载，因此实例不会被立即创建，实现延迟加载。
- 当首次调用getInstance()时，会触发SingletonHolder的加载，JVM 在类加载过程中保证线程安全，只会创建一次INSTANCE实例，无需额外加锁

**优缺点**
- **优点**：延迟加载、线程安全、实现简洁、性能高，比 DCL 更易书写和理解。
- **缺点**：无法应对 “序列化与反序列化” 场景（若需要序列化支持，需额外实现readResolve()方法）

#### 6. 枚举单例（Effective Java 推荐，最安全）
这是《Effective Java》一书推荐的单例实现方式，天然解决线程安全、序列化破坏、反射破坏等问题，是最安全的单例模式

```java
/**
 * 枚举单例 - 天然线程安全、防反射、防序列化、实现最简单
 */
public enum EnumSingleton {
    // 唯一实例（枚举常量，本质是枚举类的静态最终实例）
    INSTANCE;

    // 可在枚举中添加业务方法
    public void businessMethod() {
        System.out.println("枚举单例的业务方法执行");
    }
}

// 调用方式
public class TestEnumSingleton {
    public static void main(String[] args) {
        // 直接通过枚举常量获取实例，无需getInstance()方法
        EnumSingleton singleton1 = EnumSingleton.INSTANCE;
        EnumSingleton singleton2 = EnumSingleton.INSTANCE;
        System.out.println(singleton1 == singleton2); // true，保证唯一
        singleton1.businessMethod();
    }
}
```

**核心优势**
- **天然线程安全**：枚举类的加载由 JVM 保证线程安全，枚举常量只会被创建一次。
- **防止反射破坏**：Java 反射机制无法通过Constructor.newInstance()创建枚举类的实例（会抛出IllegalArgumentException），避免了反射攻击。
- **防止序列化破坏**：枚举类的序列化和反序列化由 JVM 特殊处理，反序列化时不会创建新实例，始终返回原有的枚举常量。
- **实现最简单**：代码量最少，无需处理构造方法私有、锁、volatile等细节

**优缺点&&
- **优点**：最安全、最简洁、无任何潜在问题，是单例模式的最优解。
- **缺点**：属于立即加载（枚举类加载时创建实例），无法实现延迟加载（但绝大多数场景下，这种 “立即加载” 的影响可忽略不计）

#### 总结
1. 单例模式的核心是：**私有构造方法 + 自身持有唯一实例 + 全局访问点**。
2. 若无需延迟加载，优先选择**饿汉式（简单）**或**枚举单例（安全）**。
3. 若需要延迟加载，优先选择**静态内部类（简洁）**或**DCL（高性能）**。
4. 若对单例的安全性要求极高（防反射、防序列化），优先选择**枚举单例**。
5. 避免使用 “非线程安全懒汉式”（多线程环境下失效）和 “简单加锁懒汉式”（性能较差）


### n.2 

#### 一、

#### 二、

#### 总结

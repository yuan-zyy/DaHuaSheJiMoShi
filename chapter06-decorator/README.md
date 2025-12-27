## 第6章 装饰模式 

### 6.1 装饰模式
核心目的是在不修改原有类代码的前提下，动态地给对象添加额外功能


#### 一、装饰模式的核心定义
***装饰模式***是一种***结构性设计模式***，它的核心思想是：在不改变原有对象代码结构的前提下，动态地给对象添加额外的功能

##### 案例 1
- ***核心对象***: 一部基础款手机（提供打电话、发短信的核心功能）
- ***装饰者***: 手机壳、贴膜、无线充电背夹（每个配件都在不改变手机本身的前提下，给手机增加新功能，比如保护、防刮、续航）。
- 特点：可以自由组合装饰者（比如既加手机壳又加背夹），动态扩展功能，符合 “开闭原则”（对扩展开放，对修改关闭）。

##### 案例 2
你有一杯基础的咖啡（核心对象）
不想改变咖啡本身的制作方式，但可以动态加奶、加糖、加冰（装饰功能）
加了奶的咖啡还是咖啡，加了糖的奶咖也还是咖啡，只是功能（口味）被增强了

#### 二、装饰模式核心角色
1. ***抽象组件***：定义核心对象的接口/抽象类，规定核心功能
2. ***具体组件***：抽象组件的实现类，是被装饰的核心对象
3. ***抽象装饰者***：继承/实现抽象组件，持有一个抽象组件的引用类（用来包裹核心对象），本身不实现具体功能，仅作为装饰着的基类
4. ***具体装饰者***：继承装饰者，实现具体的装饰功能（给核心对象加新功能）
#### 三、Java 代码示例（咖啡订单场景）
以咖啡订单为例：基础咖啡（美式）可以加牛奶、加糖、加奶泡，每种配料都是一个装饰者，动态组合出不同的咖啡。

1. 抽象组件（咖啡接口）
    ```java
    // 抽象组件：定义咖啡的核心功能（获取描述、计算价格）
    public interface Coffee {
        // 获取咖啡描述
        String getDescription();
        // 计算价格
        double getPrice();
    }
    ```
2. 具体组件（基础咖啡）
    ```java
    // 具体组件：基础美式咖啡（被装饰的核心对象）
    public class Americano implements Coffee {
        @Override
        public String getDescription() {
            return "美式咖啡";
        }
    
        @Override
        public double getPrice() {
            return 15.0; // 基础价格
        }
    }
    ```
3. 抽象装饰者（咖啡配料基类）
    ```java
    // 抽象装饰者：持有咖啡对象的引用，实现咖啡接口
    public abstract class CoffeeDecorator implements Coffee {
        // 被装饰的咖啡对象
        protected Coffee coffee;
    
        // 构造方法：传入要装饰的咖啡
        public CoffeeDecorator(Coffee coffee) {
            this.coffee = coffee;
        }
    }
    ```
4. 具体装饰者（各种配料）
    ```java
    // 具体装饰者1：加牛奶
    public class MilkDecorator extends CoffeeDecorator {
        public MilkDecorator(Coffee coffee) {
            super(coffee);
        }
    
        @Override
        public String getDescription() {
            // 在原有描述基础上添加装饰功能
            return coffee.getDescription() + " + 牛奶";
        }
    
        @Override
        public double getPrice() {
            // 在原有价格基础上增加装饰成本
            return coffee.getPrice() + 3.0;
        }
    }
    
    // 具体装饰者2：加糖
    public class SugarDecorator extends CoffeeDecorator {
        public SugarDecorator(Coffee coffee) {
            super(coffee);
        }
    
        @Override
        public String getDescription() {
            return coffee.getDescription() + " + 糖";
        }
    
        @Override
        public double getPrice() {
            return coffee.getPrice() + 1.0;
        }
    }
    ```
5. 测试代码（使用装饰模式）
```java
public class DecoratorTest {
    public static void main(String[] args) {
        // 1. 基础美式咖啡
        Coffee coffee = new Americano();
        System.out.println(coffee.getDescription() + "：" + coffee.getPrice() + "元");
        // 输出：美式咖啡：15.0元

        // 2. 美式咖啡 + 牛奶
        coffee = new MilkDecorator(coffee);
        System.out.println(coffee.getDescription() + "：" + coffee.getPrice() + "元");
        // 输出：美式咖啡 + 牛奶：18.0元

        // 3. 美式咖啡 + 牛奶 + 糖
        coffee = new SugarDecorator(coffee);
        System.out.println(coffee.getDescription() + "：" + coffee.getPrice() + "元");
        // 输出：美式咖啡 + 牛奶 + 糖：19.0元

        // 4. 也可以直接组合：美式咖啡 + 糖（跳过牛奶）
        Coffee coffee2 = new SugarDecorator(new Americano());
        System.out.println(coffee2.getDescription() + "：" + coffee2.getPrice() + "元");
        // 输出：美式咖啡 + 糖：16.0元
    }
}
```
#### 四、装饰模式的应用场景
Java 标准库中最典型的装饰模式应用就是 java.io 包：</br>
- InputStream(抽象组件)：核心输入流接口
- FileInputStream、ByteArrayInputStream(具体组件)：基础输入流实现
- FilterInputStream (抽象装饰者)：装饰者基类
- BufferedInputStream、DataInputStream(具体装饰者)：给输入流添加缓冲、数据解析等功能

示例（IO的装饰模式）
```java
// 基础文件输入流（具体组件） + 缓冲装饰（具体装饰者）
InputStream is = new BufferedInputStream(new FileInputStream("text.txt"));
```

#### 总结
1. 装饰模式的核心是***动态扩展对象功能***，无需修改原有类，符合开闭原则
2. 装饰者和被装饰者必须实现同一个抽象组件，保持类型一致
3. 装饰者通过***组合（持有被装饰者引用）***而非继承扩展功能，比继承更灵活（可自由组合多个装饰者）

#### 关键点回顾
- ***核心目的***：不修改原有类，动态给对象加功能
- ***核心结构***：抽象组件 -> 具体组件（核心对象）-> 抽象装饰者（持有组件引用） -> 具体装饰者（实现扩展功能）
- ***优势***：功能组合灵活，避免继承带来的类爆炸问题；劣势：会增加少量装饰类，代码层级稍复杂


### 6.2 案例：在 Java 中使用装饰模式实现缓存功能 （方案一）
适用装饰模式实现缓存功能是一个非常经典且实用的场景。它的核心思想是：***把“数据查询逻辑***和”缓存控制逻辑“解耦

假设我们有一个查询数据库的服务，为了提高性能，我们需要给它加上缓存。使用装饰模式，我们可以在不修改原有数据库查询代码的前提下，动态地给它套上一层 “缓存壳”。

以下是具体的实现步骤和代码示例：

#### 一、场景定义
我们要实现一个 ***用户信息查询*** 功能：
1. 核心组件：直接查询数据库（耗时，真实数据）
2. 装饰组件：
    - 缓存装饰器：先查缓存（Redis/Map），有则返回；无则查询数据库，并存入缓存
    - 日志装饰类（可选）：记录查询耗时或访问日子

#### 二、代码实现
##### 1. 抽象组件（Component）
定义核心业务接口，规定必须要实现的功能
```java
// 抽象组件：用户服务接口
public interface UserService {
    // 根据ID查询用户
    User getUserById(Long id);
}
```
##### 2. 具体组件（ConcreteComponent）
实现核心业务逻辑（查询数据库）。注意：***这里完全不包含任何缓存代码***，保持代码纯净
```java
// 具体组件：数据库查询服务（被装饰者）
public class DataBaseUserService implements UserService {
    @Override
    public User getUserById(Long id) {
        //模拟数据库查询，通常这是耗时操作
        System.out.println("DB 正常查询数据库：" + id);
        // 模拟从数据库取出数据
        return new User(id, "User_" + id, "db_password_" + id);
    }
}

// 简单的 User 实体类
class User {
    private Long id;
    private String username;
    private String password;
}
```
##### 3. 抽象装饰者（Decorator）
创建一个装饰器类，持有 UserService 的引用，并实现相同的接口
```java
// 抽象装饰类：用户服务装饰器类
public abstract class UserServiceDecorator implements UserService {
    // 持有被装饰的对象
    protected UserService userService;
    
    // 通过构造器注入
    public UserServiceDecorator(UserService userService) {
        this.userService = userService;
    }
    
    // 具体的方法由子类实现增强功能
}
```
##### 4. 具体装饰者（ConcreteDecorator）
具体实现的缓存逻辑。这是装饰模式的核心，它在调用核心方法前后添加了额外行为

```java
import java.util.HashMap;
import java.util.Map;

// 具体装饰者：缓存装饰器
public class CacheUserService extends UserServiceDecorator {
    // 模拟本地缓存（实际中可以使 Redis, Caffeine 等）
    private Map<Long, User> cache = new HashMap<>();
    
    public CacheUserService(UserService userService) {
        super(userService);
    }
    
    @Override
    public User getUserById(Long id) {
        // 1. 先查缓存
        User user = cache.get(id);
        if (user != null) {
            System.out.println("[cache] 命中缓存，直接返回，ID：" + id);
            return user;
        }
        
        // 2. 缓存未命中，调用被装饰对象的方法（查数据库）
        user = super.userService.getUserById(id);
        
        // 3. 将结果放入缓存
        if (user != null) {
            System.out.println("[cache] 写入缓存，ID：" + id);
            cache.put(id,user);
        }
        return user;
    }
}
```
##### 5. 客户端调用（Client）
在使用时，我们公国 “套娃” 的方式将装饰器和核心对象组合起来
```java
public class DecoratorCacheDemo {
    public static void main(String[] args) {
        // 1. 创建核心对象（裸奔的数据库查询）
        UserService dbService = new DatabaseUserService();

        // 2. 使用装饰器包装核心对象（穿上缓存的盔甲）
        UserService cachedService = new CachedUserService(dbService);

        System.out.println("--- 第一次查询 ID=1 ---");
        cachedService.getUserById(1L); // 预期：查库 -> 存缓存

        System.out.println("\n--- 第二次查询 ID=1 ---");
        cachedService.getUserById(1L); // 预期：直接查缓存，不查库

        System.out.println("\n--- 第一次查询 ID=2 ---");
        cachedService.getUserById(2L); // 预期：查库 -> 存缓存
    }
}
```

#### 三、--- 第一次查询 ID=1 ---
【DB】正在查询数据库，ID: 1
【Cache】写入缓存，ID: 1

--- 第二次查询 ID=1 ---
【Cache】命中缓存，直接返回，ID: 1

--- 第一次查询 ID=2 ---
【DB】正在查询数据库，ID: 2
【Cache】写入缓存，ID: 2

从结果可以看出：
1. 第一次查询 ID=1 时，走了数据库，并写入了缓存。
2. 第二次查询 ID=1 时，直接从缓存返回，没有触发数据库查询（这就是装饰器起的作用）。

#### 四、这种实现方式的优势
1. 开闭原则（OCP）
    - 如果以后你想把本地 Map 缓存换成 Redis 缓存，你只需要写一个 RedisUserServiceDecorator，而不需要修改 DatabaseUserService 或原来的 CachedUserService。
    - 如果以后不需要缓存了，直接把 new CachedUserService(...) 去掉即可，业务代码完全不受影响。
2. 职责单一（SRP）：
   - DatabaseUserService 只关心如何从数据库拿数据。
   - CachedUserService 只关心如何管理缓存（读、写、过期）。
   - 两者互不干扰。
3. 动态组合：

    你可以轻松地组合多个装饰器。例如：
```java
// 先加日志，再加缓存
UserService service = new LoggedUserService(
        new CachedUserService(
        new DatabaseUserService()
        )
        );
```

#### 五、进阶思考（Spring AOP）
在实际的 Java 开发中（特别是 Spring Boot 项目）中，我们很少动手写这类代码，因为会产生大量的模板代码

Spring 的 ***AOP（面向切面编程）*** 实际上就是装饰模式（以及代理模式）的一种更高级的自动化实现
- ***手动装饰模式***：你需要手动写 CachedUserService 类
- ***Spring AOP + 注解***：你只需要写一个 @Cacheable 注解，Spring 会在运行时动态生成一个代理类（装饰者）来帮你做缓存

***对比***：</br>
虽然 Spring AOP 更方便，但理解装饰模式是理解 AOP 底层原理的基础。对于简单的非 Spring 项目，或者需要精细控制缓存逻辑的场景，手动实现装饰模式依然是极佳的选择。

### 6.2 案例：在 Java 中使用装饰模式实现缓存功能 （方案二）
下面给你一个在 Java 中用 “装饰模式” 实现缓存（Cache-as-a-Decorator）的典型做法：把 “真正的数据源 / 服务” 作为被装饰对象，把 “缓存逻辑” 作为装饰者，在不改动原有服务代码的前提下，给查询结果加一层内存缓存（或本地缓存）

#### 一、设计思路（对应的装饰模式角色）
- ***Component(抽象组件)***: DataService(定义查询接口)
- ***ConcreteComponent(具体组件)***: DatabaseDataService(真实从库查)
- ***Decorator(抽象装饰者)***: DataServiceDecorator(持有 DataService 引用)
- ConcreteDecorator(具体装饰者): CachedDataService(加缓存)

这样的好处: </br>
- 原有服务不需要知道缓存的存在（解耦）
- 缓存可以随时替换/叠加（如再加一层分布式缓存）
- 符合开闭原则

#### 二、代码实现（本地内存缓存示例）
##### 1. 抽象组件: DataService
```java
public interface DataService<K, V> {
    V query(K key);
}
```
##### 2. 具体组件: DataBaseService(真是实现)
```java
public class DataBaseService implements DataService<Long, String> {
    @Override
    public String query(Long id) {
        //模拟 DB 查询耗时
        System.out.println("DB query: id = " + id);
        return "value = " + id;
    }
}
```
##### 3. 抽象装饰者: DataServiceDecorator
```java
public class DataServiceDecorator<K, V> implements DataService<K, V> {
    protected DataService<K, V> dataService;
    
    public DataServiceDecorator(DataServce<K, V>dataServce) {
        this.dataService = dataServce;
    }
}
```
##### 4. 具体装饰者: CacheDataService(带缓存)
```java
public class CachedDataService<K, V> extends DataServiceDecorator<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public CachedDataService(DataService<K, V> delegate) {
        super(delegate);
    }

    @Override
    public V query(K key) {
        // 1) 先查缓存
        V v = cache.get(key);
        if (v != null) {
            System.out.println("Cache hit: key=" + key);
            return v;
        }

        // 2) 缓存未命中，查 delegate（DB/远程）
        v = delegate.query(key);

        // 3) 写入缓存
        cache.put(key, v);
        System.out.println("Cache miss, loaded: key=" + key);
        return v;
    }
}
```
#### 三 使用方式（装饰与组合）
```java
public class Main {
    public static void main(String[] args) {
        DataService<Long, String> service = new DatabaseDataService();

        // 给服务“套一层缓存装饰”
        DataService<Long, String> cached = new CachedDataService<>(service);

        cached.query(1L); // miss，走 DB
        cached.query(1L); // hit，走缓存
        cached.query(2L); // miss，走 DB
        cached.query(2L); // hit，走缓存
    }
}
```
输出类似
```text
DB query: id=1
Cache miss, loaded: key=1
Cache hit: key=1
DB query: id=2
Cache miss, loaded: key=2
Cache hit: key=2
```
#### 四、进阶: 加 TTL 过期（仍然用装饰模式）
你可以再写一个装饰者 TtlCachedDataService，在 CachedDataService 之上再装饰一层：
```java
public class TtlCachedDataService<K, V> extends DataServiceDecorator<K, V> {
    private final Map<K, ExpiringValue<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public TtlCachedDataService(DataService<K, V> delegate, long ttlMillis) {
        super(delegate);
        this.ttlMillis = ttlMillis;
    }

    @Override
    public V query(K key) {
        ExpiringValue<V> ev = cache.get(key);
        long now = System.currentTimeMillis();
        if (ev != null && now < ev.expireAt) {
            System.out.println("TTL cache hit: key=" + key);
            return ev.value;
        }

        V v = delegate.query(key);
        cache.put(key, new ExpiringValue<>(v, now + ttlMillis));
        System.out.println("TTL cache miss, loaded: key=" + key);
        return v;
    }

    private record ExpiringValue<V>(V value, long expireAt) {}
}
```
使用时组合：
```java
DataService<Long, String> service = new DatabaseDataService();
DataService<Long, String> cached = new TtlCachedDataService<>(
    service, 5000 // 5秒过期
);
```
#### 与 Spring 的结合（常见落地方式）
如果你用 Spring，通常会用 AOP / 注解（如 @Cacheable）做缓存，本质也是 “透明地给方法加一层缓存装饰”。装饰模式在这里对应的是：
- @Cacheable 生成的代理类 = Decorator
你的 @Service 真实 bean = ConcreteComponent
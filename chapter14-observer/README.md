## 第14章 观察者模式

### 14.1 观察者模式
***观察者模式（Observer Pattern）***

#### 一、核心概念
***观察者模式（Observer Design Pattern）是一种***行为性设计模式***，它定义了对象之间的一对多依赖关系：当一个对象（被观察者）的状态发生变时，它会自动通知通知所有依赖它的对象（观察者），并让观察者做出相应的更新操作，实现状态变更的联动同步

这种模式就像 "发布-订阅" 模型：观察者订阅被观察者的状态变化，被观察者在状态改变时主动发布通知，无需观察者主动轮询状态

#### 二、核心角色划分
观察者模式包含 4 个核心角色，职责清晰明确：
1. ***抽象被观察者(Observable/Subject)***: 
定义被观察者的核心行为规范，包括***注册观察者(添加订阅者)、移除观察者(取消订阅)、通知观察者(状态变更时发布消息)***的抽象方法，统一管理观察者集合

2. ***具体被观察者(ConcreteSubject)***:
抽象被观察者的实现类，维护自身的状态数据，当状态发生改变时，会调用通知方法，主动向所有已注册的观察者推送状态变更信息

3. ***抽象观察者(Observer)***:
定义观察者的统一更新接口，包含一个***更新方法(update)***，用于接收被观察者的通知并执行相应的业务逻辑（如更新自身状态）

4. ***具体观察者(ConcreteObserver)***:
抽象观察者的实现类，实现更新方法，根据被观察者推送的信息完成具体的业务逻辑，每个具体的观察者都可以有不同的更新逻辑

观察者模式包含两个核心角色，缺一不可，部分场景会扩展出抽象层以提高灵活性
1. 被观察者（Observable/Subject）
   - 维护一个观察者对象的集合（列表/集合），支持添加、删除观察者
   - 提供状态变更的方法，并在状态变更后，主动触发通知方法，遍历观察者集合进行通知
2. 观察者（Observer）
   - 提供一个统一的更新方法（update），用于接收被观察者的状态变更通知
   - 当接收到通知后，在更新方法中执行具体的业务逻辑（如更新自身状态、处理业务数据等）

#### 三、两种基础实现方式
Java 中观察者模式有两种主流基础实现方式：自定义实现（手动编码）和 JDK内置API实现（便捷高效）
##### 方式1： 自定义实现（手动编码，灵活可控）
完全手动实现核心角色，适合需要定制化逻辑的场景
***步骤 1: 定义抽象被观察者（Subject）***
```java
// 抽象被观察者：定义观察者管理和通知接口
public abstract class Subject {
    // 维护观察者列表
    protected List<Observer> observers = new ArrayList<>();

   /**
    * 注册观察者
    * @param observer
    */
   public void attach(Observer observer) {
       if(observer == null) {
           throw new NullPointerException("观察者不能为空");
       }
       observers.add(observer);
       System.out.println("观察者[：" + observer.getclass().getSimpleName() + "]已注册");
    }

   /**
    * 移除观察者
    * @param observer
    */
   public void detach(Observer observer) {
       if (observer == null || observers.isEmpty()) {
           return;
       }
       
       observers.remove(observer);
       System.out.println("观察者[：" + observer.getclass().getSimpleName() + "]已移除");
    }

   /**
    * 抽象通知方法：由具体被观察者实现
    * @param message
    */
   public abstract void notifyObservers(String message);
}
```

***步骤 2: 定义抽象观察者（Observer）***
```java
/**
 * 抽象观察者：定义统一更新接口
 */
public interface Observer {
   /**
    * 接收被观察者的通知并更新
    * @param message
    */
    void update(String message);
}
```

***步骤 3: 实现具体被观察者(ConcreteSubject)***
```java
/**
 * 具体被观察者: 例如 "公众号"，状态变更时通知所有订阅者
 */
public class WeChatOfficialAccount extends Subject { 
    // 公众号名称
    private String accountName;
    
    public WeChatOfficialAccount(String accountName) {
        this.accountName = accountName;
    }

   /**
    * 发布文章（状态变更）
    * @param articleTitle
    */
   public void publishArticle(String articleTitle) {
      System.out.println("公众号[：" + accountName + "]发布了新文章：" + articleTitle);
      // 通知所有观察者
      notifyObservers("新文章：" + articleTitle);
   }
   
   @Override
   public void notifyObservers(String message) {
       // 遍历观察者列表，通知所有观察者
      for (Observer observer : observers) {
          observer.update(message);
      }
   }
}
```

***步骤 4: 实现具体观察者(ConcreteObserver)***
```java
/**
 * 具体观察者1: 普通用户
 */
public class NormalUser implements Observer {
    private String userName;
    
    public NormalUser(String userName) {
        this.userName = userName;
    }
    
    @Override
    public void update(String message) {
        System.out.println(userName + "收到通知：公众号发布了新文章《" + message + "》，已自动推送至你的消息列表");
    }
}

/**
 * 具体观察者2: Vip用户
 */
public class VipUser implements Observer {
    private String userName;
    
    public VipUser(String userName) {
        this.userName = userName;
    }
    
    @Override
    public void update(String message) {
        System.out.println(userName + "(VIP) 收到通知：公众号发布了新文章《" + message + "》，已自动推送至你的消息列表");
    }
}
```

***步骤 5: 测试自定义观察者模式***
```java
public class CustomObserverTest {
   public static void main(String[] args) {
      // 1. 创建具体被观察者（Java技术公众号）
      WeChatOfficialAccount officialAccount = new WeChatOfficialAccount("Java技术栈");
      
      // 2. 创建具体观察者（普通用户、VIP用户）
      Observer user1 = new NormalUser("张三");
      Observer user2 = new VipUser("李四");
      Observer user3 = new NormalUser("王五");
      
      // 3. 注册观察者(订阅公众号)
      officialAccount.attach(user1);
      officialAccount.attach(user2);
      officialAccount.attach(user3);
      
      // 4. 被观察者状态变更（发布文章）
      officialAccount.publishArticle("《Java技术栈第1期》");
      
      // 5. 移除观察者(取消订阅公众号)
      officialAccount.detach(user3);
      System.out.println("--- 王五取消订阅后 ---");
      
      // 6. 再次发表文章，验证移除效果
      officialAccount.publishArticle("《Java技术栈第2期》");
   }
}
```

***测试效果***
```text
观察者[NormalUser]已注册
观察者[VipUser]已注册
观察者[NormalUser]已注册

Java技术栈发布了新文章：《Java观察者模式从入门到精通》
张三收到通知：公众号发布了新文章《Java观察者模式从入门到精通》，已自动推送至你的消息列表
李四（VIP）收到通知：公众号发布了新文章《Java观察者模式从入门到精通》，已为你优先推送并缓存全文
王五收到通知：公众号发布了新文章《Java观察者模式从入门到精通》，已自动推送至你的消息列表

观察者[NormalUser]已移除

--- 王五取消订阅后 ---

Java技术栈发布了新文章：《Java设计模式实战合集》
张三收到通知：公众号发布了新文章《Java设计模式实战合集》，已自动推送至你的消息列表
李四（VIP）收到通知：公众号发布了新文章《Java设计模式实战合集》，已为你优先推送并缓存全文
```

##### 方式二: JDK内置 API 实现
Java 提供了java.util.Observable（被观察者基类）和java.util.Observer（观察者接口），可直接复用，减少手动编码。

***注意事项***
- Observable是类而非接口，这是 JDK 设计的局限性（Java 单继承，若子类已继承其他类则无法使用）
- 观察者需实现java.util.Observer接口的update(Observable o, Object arg)方法
- 被观察者需继承Observable，通过setChanged()标记状态变更，notifyObservers()发送通知

***步骤 1： 实现 JDK 具体被观察者（继承 Observable）***
```java
import java.util.Observable;

// JDK具体被观察者：天气服务中心（提供天气变更通知）
public class WeatherStation extends Observable {
    // 天气信息
    private String weatherInfo;

    public void setWeatherInfo(String weatherInfo) {
        this.weatherInfo = weatherInfo;
        // 1. 标记被观察者状态已变更（必须调用，否则notifyObservers不生效）
        setChanged();
        // 2. 通知所有观察者，可携带消息参数（第二个参数）
        notifyObservers(weatherInfo);
        System.out.println("天气服务中心更新天气：" + weatherInfo);
    }

    public String getWeatherInfo() {
        return weatherInfo;
    }
}
```

***步骤 2： 实现 JDK 具体观察者（实现 Observer 接口）***
```java
import java.util.Observable;
import java.util.Observer;

// 具体观察者1：手机天气APP
public class PhoneWeatherApp implements Observer {
    private String appName;

    public PhoneWeatherApp(String appName) {
        this.appName = appName;
    }

    @Override
    public void update(Observable o, Object arg) {
        // 验证被观察者类型
        if (o instanceof WeatherStation) {
            String weather = (String) arg;
            System.out.println(appName + "收到天气更新：" + weather + "，已为你更新桌面天气组件");
        }
    }
}

// 具体观察者2：电视天气播报
public class TVWeatherReport implements Observer {
    private String tvChannel;

    public TVWeatherReport(String tvChannel) {
        this.tvChannel = tvChannel;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof WeatherStation) {
            String weather = (String) arg;
            System.out.println(tvChannel + "收到天气更新：" + weather + "，即将在晚间新闻中播报最新天气");
        }
    }
}
```

***步骤 3： 测试 JDK 自定义观察者模式***
```java
public class JdkObserverTest {
    public static void main(String[] args) {
        // 1. 创建JDK被观察者（天气服务中心）
        WeatherStation weatherStation = new WeatherStation();

        // 2. 创建JDK观察者
        Observer phoneApp = new PhoneWeatherApp("高德天气");
        Observer tvReport = new TVWeatherReport("中央电视台新闻频道");

        // 3. 注册观察者（添加监听）
        weatherStation.addObserver(phoneApp);
        weatherStation.addObserver(tvReport);

        // 4. 被观察者状态变更（更新天气）
        System.out.println("--- 第一次天气更新 ---");
        weatherStation.setWeatherInfo("明日晴，气温15-28℃，微风");

        System.out.println("\n--- 第二次天气更新 ---");
        weatherStation.setWeatherInfo("明日小雨，气温12-20℃，东北风3-4级");

        // 5. 移除观察者
        weatherStation.deleteObserver(tvReport);
        System.out.println("\n--- 移除电视播报后，第三次天气更新 ---");
        weatherStation.setWeatherInfo("后日多云转晴，气温14-26℃，南风2级");
    }
}
```

***测试效果***
```text
--- 第一次天气更新 ---
高德天气收到天气更新：明日晴，气温15-28℃，微风，已为你更新桌面天气组件
中央电视台新闻频道收到天气更新：明日晴，气温15-28℃，微风，即将在晚间新闻中播报最新天气
天气服务中心更新天气：明日晴，气温15-28℃，微风

--- 第二次天气更新 ---
高德天气收到天气更新：明日小雨，气温12-20℃，东北风3-4级，已为你更新桌面天气组件
中央电视台新闻频道收到天气更新：明日小雨，气温12-20℃，东北风3-4级，即将在晚间新闻中播报最新天气
天气服务中心更新天气：明日小雨，气温12-20℃，东北风3-4级

--- 移除电视播报后，第三次天气更新 ---
高德天气收到天气更新：后日多云转晴，气温14-26℃，南风2级，已为你更新桌面天气组件
天气服务中心更新天气：后日多云转晴，气温14-26℃，南风2级
```

***JDK 内置实现的局限性*** </br>
DK 提供的Observable和Observer存在明显缺陷，在实际开发中（尤其是复杂场景）已逐渐被替代
1. Observable是类而非接口：Java 仅支持单继承，若自定义被观察者已继承其他父类（如BaseService），则无法再继承Observable，灵活性受限
2. 方法访问权限问题：Observable的核心方法（如setChanged()）是protected修饰的，只能在子类内部调用，外部无法手动触发状态变更，定制化能力弱
3. 不支持泛型：通知参数为Object类型，需要强制类型转换，存在类型安全风险
4. 功能简单：缺乏异步通知、事件过滤等高级特性，无法满足复杂业务场景需求

#### 三、高级特性：观察者模式的两种通知方式
观察者模式有两种核心通知方式，使用场景不同，是实际开发中必须区分的关键点
1. ***推模式(Push Model)***
   - ***定义***: 被观察者主动将 [全部或部分状态数据] 推送给观察者，观察者无需主动获取数据
   - ***特点***: 被观察者主动数据传递，观察者被动接收，耦合度相对较高（观察者依赖被观察推送的数据格式）
   - ***对应案例***: 上述公众号案例、天气案例均为推模式（被观察者主动传递文章标题、天气信息）
   - ***优点***: 观察者使用便捷，无需额外调用被观察者的方法获取数据
   - ***缺点***: 可能推送冗余数据（观察者不需要的信息也会被推送），灵活性差

2. ***拉模式(Pull Model)***
   - ***定义***: 被观察者仅通知观察者 [状态已变更]，不主动推送具体数据；观察者收到通知后，主动调用被观察者的 getter 方法拉取所需数据
   - ***特点***: 观察者主导数据获取，耦合度更低（观察者仅依赖被观察者的接口，不依赖具体数据格式），灵活性更强

   - ***拉模式改造案例(基于自定义实现)***: </br>
     我们对之前的 Subject 和 Observer 进行改造，实现拉模式:
   ```java
   // 1. 改造抽象观察者: 移除消息参数，新增拉取数据的逻辑支撑
   public interface Observer {
      // 无消息参数，仅通知状态变更
      void update();
   }
   
   // 2. 改造具体被观察者：新增获取核心状态的方法
   public class WechatOfficialAccount extends Subject {
      private String accountName;
      // 新增：当前最新标题文章（核心状态）
      private String latestArticleTitle;
   
      public WechatOfficialAccount(String accountName) {
         this.accountName = accountName;
      }
   
      // 2. 发布文章：更新最新文章标题（状态变更）
      public void publishArticle(String articleTitle) {
         System.out.println(accountName + "发布了新文章文章：" + articleTitle);
         this.latestArticleTitle = articleTitle; // 保存最新状态
         notifyObservers(); // 无参数通知
      }
   
      // 新增：供观察者拉取数据的 getter 方法
      public String getLatestArticleTitle() {
         return latestArticleTitle;
      }
   
      @Override
      public void notifyObservers() {
         for (Observer observer : observers) {
            observer.update();  // 仅通知，不推送消息
         }
      }
   
   }
   
   // 3. 改造具体观察者：主动拉取所需数据
   public class NormalUser implements Observer {
      private String userName;
      // 持有被观察者引用，用于拉取消息
      private Subject subject;
   
      // 构造方法传入被观察者
      public NormalUser(String userName, Subject subject) {
         this.userName = userName;
         this.subject = subject;
      }
   
      @Override
      public void update() {
         // 主动拉取数据：先判断类型，再调用 getter 方法
         if (subject instanceof WechatOfficialAccount) {
            WechatOfficialAccount account = (WechatOfficialAccount) subject;
            String articleTitle = account.getLatestArticleTitle();
            System.out.println(userName + "收到通知：公众号发布了新文章《" + articleTitle + "》    " + ", 已推送");
         }
      }
   }
   ```
   - ***优点***: 避免冗余数据传递，观察者可按需拉取数据，灵活性高、耦合度低
   - ***缺点***: 观察者需要持有被观察者引用，且需额外调用 getter 方法，代码相对繁琐

3. 两种模式对比与选型

| 对比维度  | 推模式                  | 拉模式                              |
|-------|----------------------|----------------------------------|
| 耦合度   | 较高（依赖数据格式）           | 较低（仅依赖接口）                        |
| 数据冗余  | 可能存在冗余数据             | 无冗余数据，按需获取                       |
| 代码复杂度 | 简单，观察者直接使用数据         | 稍复杂，观察者需主动拉取                     |
| 灵活性   | 低，被观察者变更数据格式需同步修改观察者 | 高，被观察者状态变更不影响观察者（只要 getter 接口不变） |
| 选型建议  | 简单业务场景、数据格式固定        | 复杂业务场景、数据格式可能变更、观察者需按需获取数据       |


#### 四、观察者模式的潜在问题与解决方案
在实际开发中，若使用不当，观察者模式会引发一系列问题，以下是核心问题及对应解决方案
##### 问题 1： 观察者注册后未移除，导致内存泄漏
- ***现象***：当观察者对象（如 Android 中的 Activity、Java 中的业务 Bean）生命周期结束后，若未从被观察者的观察者列表中移除，被观察者会一直持有观察者的引用，导致观察者无法被 GC 回收，最终引发内存泄漏。
- ***典型场景***：Android 页面订阅了某个数据监控服务，页面销毁时未取消订阅，导致页面实例无法被回收
- ***解决方案***:
  1. ***显式移除***: 在观察者生命周期结束时（如 Android 的onDestroy()、Java 的close()方法），主动调用detach()（自定义实现）或deleteObserver()（JDK 内置）方法移除观察者
  2. ***使用弱引用***: 在被观察者的观察者列表中，存储观察者的WeakReference（弱引用），当观察者无强引用时，GC 会自动回收该观察者，避免内存泄漏
   ```java
   // 改造Subject的观察者列表：使用弱引用
   import java.lang.ref.WeakReference;
   import java.util.ArrayList;
   import java.util.List;
   import java.util.Iterator;
   
   public abstract class Subject {
       // 存储观察者的弱引用
       protected List<WeakReference<Observer>> observerWeakList = new ArrayList<>();
   
       // 注册观察者：包装为弱引用
       public void attach(Observer observer) {
           if (observer == null) {
               throw new NullPointerException("观察者不能为空");
           }
           observerWeakList.add(new WeakReference<>(observer));
           System.out.println("观察者[" + observer.getClass().getSimpleName() + "]已注册");
       }
   
       // 移除观察者：遍历弱引用列表，匹配并移除
       public void detach(Observer observer) {
           if (observer == null || observerWeakList.isEmpty()) {
               return;
           }
           Iterator<WeakReference<Observer>> iterator = observerWeakList.iterator();
           while (iterator.hasNext()) {
               WeakReference<Observer> weakRef = iterator.next();
               Observer obs = weakRef.get();
               if (obs == null || obs == observer) {
                   iterator.remove();
                   System.out.println("观察者[" + observer.getClass().getSimpleName() + "]已移除");
                   break;
               }
           }
       }
   
       // 通知观察者：先过滤已被GC回收的观察者
       @Override
       public void notifyObservers() {
           Iterator<WeakReference<Observer>> iterator = observerWeakList.iterator();
           while (iterator.hasNext()) {
               WeakReference<Observer> weakRef = iterator.next();
               Observer obs = weakRef.get();
               if (obs == null) {
                   // 移除已被GC回收的弱引用
                   iterator.remove();
               } else {
                   obs.update();
               }
           }
       }
   }
   ```
##### 问题 2： 观察者数量过多，导致通知耗时过长（同步阻塞）
- ***现象***：当被观察者有大量观察者时，同步遍历通知所有观察者会导致阻塞（被观察者的notifyObservers()方法执行时间过长），影响被观察者的其他业务逻辑执行
- ***典型场景***：一个消息推送服务有 1000 个订阅者，同步通知会阻塞主线程
  - 解决方案:
    1. ***异步通知***: 使用线程池（ExecutorService）异步执行观察者的update()方法，避免被观察者阻塞
        ```java
        import java.util.concurrent.Executors;
        import java.util.concurrent.ExecutorService;
      
        public class WeChatOfficialAccount extends Subject {
            // 定义线程池
            private ExecutorService executor = Executors.newFixedThreadPool(10);
      
            @Override
            public void notifyObservers() {
                for (Observer observer : observerList) {
                    // 异步执行观察者的update方法
                    executor.submit(() -> observer.update());
                }
            }
      
            // 关闭线程池（避免内存泄漏）
            public void close() {
                executor.shutdown();
            }
        }
        ```
    2. ***批量通知/分段通知***: 对大量观察者进行分段批量通知，减少单次阻塞时间
    3. ***优先级通知***: 为观察者设置优先级，优先通知核心观察者（如 VIP 用户），非核心观察者延后通知

##### 问题 3： 通知过程中观察者列表变更（并发修改异常）
- ***现象***: 当被观察者正在遍历观察者列表进行通知时，若某个观察者在update()方法中主动调用detach()（移除自己或其他观察者），会导致ConcurrentModificationException（并发修改异常）
- ***原因***: ArrayList 等集合在迭代器遍历期间，不允许通过非迭代器方法修改集合（添加 / 移除元素）
- ***解决方案***:
  1. ***使用迭代器移除***: 遍历观察者列表时，使用Iterator迭代器，通过iterator.remove()方法修改集合
  2. ***遍历副本***：先复制观察者列表的副本，遍历副本进行通知，即使原列表变更，也不会影响遍历过程
   ```java
   @Override
   public void notifyObservers(String message) {
       // 复制观察者列表的副本，遍历副本进行通知
       List<Observer> observerCopy = new ArrayList<>(observerList);
       for (Observer observer : observerCopy) {
           observer.update(message);
       }
   }
   ```

#### 五、观察者模式的替代方案与主流框架实现
JDK 内置Observable存在局限性，实际开发中更推荐使用成熟框架或自定义实现，以下是主流替代方案
1. ***Guava EventBus（谷歌开源，推荐）***
   - ***简介***: Guava 提供的EventBus是观察者模式的高级实现，无需手动管理观察者列表，支持同步 / 异步通知、事件分类、注解驱动，完全解决了 JDK 内置实现的局限性
   - ***核心优势***:
     1. 无需继承 / 实现特定接口（通过@Subscribe注解标记观察者方法）
     2. 支持事件类型匹配（按事件类类型自动分发通知）
     3. 内置同步EventBus和异步AsyncEventBus（基于线程池）
     4. 自动处理观察者的注册与移除，支持弱引用避免内存泄漏

   ```java
   // 1. 定义事件类（承载通知数据）
   public class ArticlePublishedEvent {
      private String accountName;
      private String articleTitle;
   
      public ArticlePublishedEvent(String accountName, String articleTitle) {
         this.accountName = accountName;
         this.articleTitle = articleTitle;
      }
   
      public String getAccountName() {
         return accountName;
      }
   
      public String getArticleTitle() {
         return articleTitle;
      }
   }
   
   // 2. 定义观察者（无需实现 Observer 接口）
   public class NormalUser {
       private String userName;
       
       public NormalUser(String userName, EventBus eventBus) {
           this.userName = userName;
           // 注册观察者到 EventBus
          eventBus.register(this);
       }
   
       // 标记为订阅方法，接收ArticlePublishEvent类型事件
       @Subscribe
       public void onArticlePublish(ArticlePublishedEvent event) {
          System.out.println(userName + "收到通知：" + event.getAccountName() + "发布了《" + event.getArticleTitle() + "》");
       }
   }
   
   // 3. 定义被观察者（事件发布者）
   public class WeChatOfficialAccount {
       private String accountName;
       private EventBus eventBus;
       
       public WeChatOfficialAccount(String accountName, EventBus eventBus) {
           this.accountName = accountName;
           this.eventBus = eventBus;
       }
       
       // 发布文章
       public void publishArticle(String articleTitle) {
           ArticlePublishedEvent event = new ArticlePublishedEvent(accountName, articleTitle);
           eventBus.post(event);   // 发布事件，自动通知所有订阅者
       }
   }
   
   // 4. 测试 Guava EventBus
   public class GuavaEventBusTest {
       public static void main(String[] args) {
          // 创建同步EventBus（异步使用new AsyncEventBus(Executors.newFixedThreadPool(10))）
          EventBus eventBus = new EventBus();
          
          // 创建观察者（自动注册）
          NormalUser user1 = new NormalUser("张三", eventBus);
          NormalUser user2 = new NormalUser("李四", eventBus);
          
          // 创建被观察者（事件发布者）
          WeChatOfficialAccount account = new WeChatOfficialAccount("Java技术栈", eventBus);
          
          // 发布事件
          account.publishArticle("Guava EventBus实战教程");
       }
   }
   ```

2. ***Spring Event(Spring 框架内置, 适用于 Spring 项目)***
   - ***简介***: Spring 框架提供的事件驱动模型（基于观察者模式），与 Spring 容器深度集成，支持单播事件、广播事件、异步事件，是 Spring 项目中实现观察者模式的首选
   - ***核心组件***:
     1. ***ApplicationEvent***: 事件基类（自定义事件需继承它）
     2. ***ApplicationEventPublisher***: 观察者接口（或使用@EventListener注解）
     3. ***ApplicationContext***: 事件发布者（通过publishEvent()方法发布事件）
   - ***核心优势***: 与 Spring 容器无缝集成，支持依赖注入、事务绑定（@TransactionalEventListener）、异步执行（@Async）

3. ***自定义观察者模式（终极灵活方案）***
   当项目不依赖 Guava、Spring 等框架时，自定义实现（基于接口 + 弱引用 + 异步线程池）是最优选择，可完全按需定制功能（如优先级通知、事件过滤、批量通知等）

#### 六、观察者模式与相似设计模式的区别
1. 观察者模式 vs 发布 - 订阅模式（Publish-Subscribe Pattern）</br>
   很多人会混淆两者，实际上发布 - 订阅模式是观察者模式的扩展和升级，核心区别如下

   | 对比维度 | 观察者模式                  | 发布 - 订阅模式                             |
   |------|------------------------|---------------------------------------|
   | 耦合度  | 较高（被观察者直接持有观察者引用，双向依赖） | 较低（通过中间件/消息队列解耦，发布者与订阅者无直接依赖）         |
   | 中间角色 | 无，直接交互                 | 有（消息队列/事件总线，如RabbitMQ、Kafka、EventBus） |
   | 适用场景 | 进程内、简单一对多场景            | 跨进程、跨系统、复杂分布式场景                       |
   | 灵活性  | 较低（仅支持进程内通知）           | 较高（支持异步、延迟、广播、路由等高级特性）                |
   | 典型案例 | 公众号订阅（进程内模式）、GUI事件监听   | RabbitMQ 消息推送、Kafka 日志收集              |

2.  观察者模式 vs 责任链模式 </br>
   - ***核心差异***: 
     1. 观察者模式：一对多依赖，被观察者通知所有观察者，观察者之间无关联、无顺序（或可自定义顺序）；
     2. 责任链模式：一对一 / 一对多依赖，请求沿责任链传递，每个处理器只处理自己负责的逻辑，处理完成后可传递给下一个处理器，观察者（处理器）之间有明确的执行顺序
   - ***适用场景***:
     1. 观察者模式：状态变更通知（所有观察者都需要知晓）；
     2. 责任链模式：请求处理（如权限校验、日志记录、参数验证等，按顺序执行）

#### 七、观察者模式的适用场景
当业务场景满足 “一对多依赖” 且需要 “实时响应状态变更” 时，优先使用观察者模式
1. 消息订阅 / 推送系统（如公众号订阅、短信通知、邮件推送）
2. 事件监听系统（如 GUI 界面按钮点击、鼠标移动等事件响应）
3. 数据监控系统（如天气监控、服务器性能监控、库存预警）
4. 发布 - 订阅模式（如 MQ 消息队列的核心思想，本质是观察者模式的扩展）

#### 八、 观察者模式的最佳实践（实际开发规范）
1. **优先使用框架实现**：Spring 项目使用Spring Event，非 Spring 项目使用Guava EventBus，避免重复造轮子，提升开发效率；
2. **避免使用 JDK Observable**：因单继承限制、功能简陋等问题，实际开发中尽量不使用java.util.Observable；
3. **必做内存泄漏防护**：要么显式移除观察者，要么使用弱引用存储观察者，避免内存泄漏；
4. **复杂场景使用异步通知**：当观察者数量多或update()方法耗时较长时，使用线程池实现异步通知，避免阻塞被观察者；
5. **使用拉模式降低耦合**：复杂业务场景优先使用拉模式，观察者按需拉取数据，提升系统灵活性和可维护性；
6. **防止并发修改异常**：遍历观察者列表时，使用迭代器或遍历副本，避免并发修改异常；
7. **事件分类清晰**：当存在多种通知类型时，定义不同的事件类（如ArticlePublishEvent、AccountDeleteEvent），便于观察者按需订阅。

#### 九、 完整总结
1. **核心本质**：观察者模式是「一对多依赖」的行为型模式，核心是「解耦被观察者与观察者，实现状态变更的实时通知」；
2. **基础实现**：自定义实现（灵活可控）、JDK 内置 API 实现（便捷高效但有局限性），框架实现（Guava/Spring，实际开发首选）；
3. **通知方式**：推模式（简单低灵活）、拉模式（低耦合高灵活），根据业务复杂度按需选型；
4. **核心问题**：内存泄漏、同步阻塞、并发修改异常，对应解决方案需熟练掌握，是生产环境稳定运行的关键；
5. **模式区别**：观察者模式是进程内直接交互，发布 - 订阅模式是通过中间件解耦的分布式交互，需明确区分避免误用；
6. **最佳实践**：优先框架、防内存泄漏、异步通知、拉模式降耦合，是实际开发的核心规范，能提升系统的可维护性和扩展性

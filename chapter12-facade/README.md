## 第12章 外观模式

### 12.1 外观模式
***外观模式(Facade Pattern)***，这是一种***结构型设计模式***，核心目标是***简化复杂系统的访问方式，同时解耦客户端与复杂子系统之间的依赖关系***

#### 一、核心定义
外观模式通过提供一个***统一外观接口(Facade类)***, 封装了底层多个复杂子系统的交互逻辑，客户端无需直接操作各个子系统，只需通过这个外观接口即可完成所需功能，就像医院的导诊台（外观类）整合了挂号、就诊、缴费、取药等多个部分（子系统）的流程，患者无需逐个对接部门

#### 二、核心角色
外观模式包含 3 个核心角色，职责清晰划分：
1. ***外观角色（Facade）***:
    - 核心角色，提供统一的对外接口，封装子系统的交互逻辑
    - 客户端直接调用该角色的方法，无需感知底层子系统的细节
2. ***子系统角色（SubSystem）***:
    - 一个或多个独立的子系统，实现具体的业务功能，是外观角色的底层支撑
    - 子系统不感知外观角色的存在，仅关注自身的核心功能实现
3. ***客户端角色（Client）***:
    - 外观模式的使用者，仅通过外观角色与子系统交互，无需直接操作子系统

#### 三、完整Java代码示例
下面一【家庭影院系统】为例，演示外观模式的视线：

家庭影院包含多个子系统（投影仪、音响、播放器、幕布），客户端无需逐个开启/关闭子系统，只需要通过外观类一键完成 "观影" 或 "结束观影" 操作
1. 定义多个子系统角色（SubSystem）
   ```java
   // 子系统1：投影仪
   public class Projector {
       // 单利（可选，子系统可按需设计）
       private static final Projector INSTANCE = new Projector();
       
       private Projector() {}
       
       public static Projector getInstance() {
           return INSTANCE;
       }
       
       public void turnOn() {
           System.out.println("投影仪：开启，调整至最佳分辨率");
       }
       
       public void turnOff() {
           System.out.println("投影仪：关闭， 进入待机模式");
       }
   }
   
   // 子系统2：音响
   public class SoundSystem {
       private static final SoundSystem INSTANCE = new SoundSystem();
       
       private SoundSystem() {}
      
       public static SoundSystem getInstance() {
           return INSTANCE;
       }
       
       public void turnOn() {
           System.out.println("音响：开启， 音量调至50%");
       }
       
       public void turnOff() {
           System.out.println("音响：关闭， 清除音效设置");
       }
   }
   
   // 子系统3：视频播放器
   public class VideoPlayer {
       private static final VideoPlayer INSTANCE = new VideoPlayer();
       
       private VideoPlayer() {}
      
       public static VideoPlayer getInstance() {
           return INSTANCE;
       }
       
       public void play() {
           System.out.println("播放器：开启， 开始播放影片");
       }
       
       public void stop() {
           System.out.println("播放器：关闭， 退出影片");
       }
   }
   
   // 子系统4：幕布
   public class Curtain {
       private static final Curtain INSTANCE = new Curtain();
       
       private Curtain() {}
      
       public static Curtain getInstance() {
           return INSTANCE;
       }
       
       public void drop() {
           System.out.println("幕布：开启， 准备观影");
       }
       
       public void rise() {
           System.out.println("幕布：关闭， 观影结束");
       }
   }
   ```

2. 定义外观角色（Facade）
   ```java
   // 家庭影院外观类（核心：封装子系统交互逻辑）
   public class HomeTheaterFacade {
       // 持有所有子系统的引用
       private Projector projector;
       private SoundSystem soundSystem;
       private VideoPlayer videoPlayer;
       private Curtain curtain;
       
       // 初始化子系统（按需注入，此处直接获取单例）
       public HomeTheaterFacade() {
           this.projector = Projector.getInstance();
           this.soundSystem = SoundSystem.getInstance();
           this.videoPlayer = VideoPlayer.getInstance();
           this.curtain = Curtain.getInstance();
       }
       
       // 统一接口：一键开启观影模式（封装多个子系统的操作）
      public void startMovie() {
         System.out.println("====== 开始准备观影 ======");
         curtain.drop();       // 放下幕布
         projector.turnOn();   // 开启投影仪
         soundSystem.turnOn(); // 开启音响
         videoPlayer.play();
         System.out.println("====== 观影模式已开启 ======");
      }
      
      // 统一接口：一键结束观影模式（封装多个子系统的操作）
      public void endMovie() {
         System.out.println("====== 开始结束观影 ======");
         videoPlayer.stop();       // 停止播放
         soundSystem.turnOff();    // 关闭音响
         projector.turnOff();      // 关闭投影仪
         curtain.rise();           // 升起幕布
         System.out.println("====== 观影模式已结束 ======");
      }
   }
   ```
3. 定义客户端角色（Client）
   ```java
   // 客户端：仅与外观类交互，无需感知子系统的细节
   public class Client {
       public static void main(String[] args) {
           // 1. 创建外观类实例
          HomeTheaterFacade facade = new HomeTheaterFacade();
          
          // 2. 一键开启观影(无需调用任何子系统的方法)
          facade.startMovie();
   
          System.out.println("\n----------观影中----------");
          
          // 3. 一键结束观影(无需调用任何子系统方法)
          facade.endMovie();
       }
   }
   ```

4. 运行结果
```text
===== 开始准备观影 =====
幕布：放下，准备观影
投影仪：开启，调整至最佳分辨率
音响：开启，音量调至50%
视频播放器：开始播放影片
===== 观影模式已开启 =====

---------- 观影中... ----------

===== 开始结束观影 =====
视频播放器：停止播放，退出影片
音响：关闭，清除音效设置
投影仪：关闭，进入待机模式
幕布：升起，观影结束
===== 观影模式已结束 =====
```

#### 四、核心优点
1. ***简化客户端操作***: 客户端无需记忆复杂的系统接口和交互顺序，仅需调用外观类的统一方法，降低了使用成本
2. ***解耦客户端与子系统***：客户端与系统之间通过外观类隔离，子系统的内部实现变更（如投影仪更换型号、音响调整音效逻辑），无需修改客户端代码，符合开闭原则
3. ***隐藏子系统的细节***: 屏蔽了子系统的复杂逻辑，减少了客户端需要处理的对象数量和代码复杂度，提高了系统的可维护性
4. ***便于管理子系统交互***：外观类集中管理子系统之间的交互逻辑，避免了客户端直接操作子系导致的逻辑混乱，便于后续统一维护和扩展

#### 五、适用场景
1. 当你需要访问一个***复杂子系统***，且希望简化其访问方式时（如第三发SDK封装，复杂业务流程整合）
2. 当你需要***解耦客户端与子系统***，降低两者之间的依赖耦合度，提高系统的灵活性
3. 当你需要为多个子系统提供一个***统一的对外接口***，便于客户端统一调用时（如后台管理系统的 “一键导出报表功能，封装了数据查询、格式转换、文件生成等子系统”）
4. 当你需要对现有复杂系统进行***分层设计***，外观类作为系统的入口层，屏蔽底层子系统的细节时

#### 总结
1. 外观模式是结构性模式，核心是***统一对外接口 + 封装子系统逻辑***，简化客户端访问
2. 核心角色：外观类（Facade），子系统类（Subsystem），客户端类（Client），三者职责明确
3. 核心价值：简化操作、解耦依赖、隐藏细节，适用于复杂系统的访问封装和分层设计
4. 关键特点：客户端仅与外观类交互，子系统无需感知外观类的存在，符合 “最少知识原则”+


### n.2 

#### 一、

#### 二、

#### 总结

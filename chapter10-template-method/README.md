## 第10章 模板方法

### 10.1 模板方法
***模板方法设计模式（Template Method Pattern）***

#### 一、核心概念
模板方法设计模式是一种***行为型设计模式***，它的核心思想是: ***定义一个算法的骨架(步骤流程)，将算法中某些步骤的具体实现延迟到子类汇总***。这样既可以保证算法的整体流程不变，又能让子类灵活定制其中的具体步骤，实现了 "流程复用" 和 "细节定制" 的分离

#### 二、核心结构
模板方法模式主要包含了两个核心角色，职责划分清晰：
1. ***抽象父类(Abstract Class)***
    - 定义算法的整体骨架(模板方法)，按固定顺序编排算法的各个步骤
    - 包含两类算法:
      - 模板方法(Template Method): 通常用 final 修饰(防止子类重写，保证算法流程不被篡改)，负责串联各个步骤
      - 具体步骤方法: 分为***抽象方法 (子类必须实现)***、***具体方法(父类提供默认实现，子类可选择性重写)***，***钩子方法(Hook Method，父类提供默认空实现或默认逻辑，子类可通过重写钩子方法控制算法流程)
2. ***具体子类(Concrete Class)***
    - 实现抽象父类中定义的抽象步骤方法，完成算法的具体细节实现
    - 可选择性重写父类的具体方法和钩子方法，定制个性化的逻辑

#### 三、完整 Java 示例
我们以 "制作饮品" 为场景(咖啡和茶的制作流程高度相似，符合模板方法的适用场景)，实现模板方法模式

1. ***抽象父类: 饮品制作模板 (定义算法骨架) ***
   ```java
   /**
    * 抽象父类: 饮品制作模板 (定义制作饮品的算法骨架)
    */
   public abstract class AbstractDrinkMaker {
       /**
        * 模板方法: 制作饮品的完整流程(用 final 修饰，防止子类篡改流程)
        * 流程固定: 烧开水 -> 冲泡原料 -> 倒入杯子 -> 添加辅料
        */
       public final void makeDrink() {
           boilWater();        // 步骤1: 烧开水 (父类默认实现，所有饮品通用)
           brewRawMaterial();  // 步骤2: 冲泡原料 (抽象方法，子类定制)
           pourIntoCup();        // 步骤3: 倒入杯子 (父类默认实现，所有饮品通用)
           if(needAddCondiment()) {
               addCondiment(); // 步骤4: 添加辅料 (抽象方法，子类定制)
           }
       }
   
      /**
       * 具体方法: 步骤1 - 烧开水 (所有饮品通用，父类提供默认实现)
       */
      private void boilWater() {
          System.out.println("1. 烧开水（100摄氏度）");
       }
   
      /**
       * 抽象方法: 步骤2 - 冲泡原料 (子类必须实现, 咖啡冲泡咖啡粉，茶冲茶叶)
       */
      protected abstract void brewRawMaterial();
   
      /**
       * 具体方法: 步骤3 - 倒入杯子 (所有饮品通用，父类提供默认实现)
       */
      private void pourIntoCup() {
         System.out.println("3. 将冲泡好的饮品倒入杯子中");
      }
   
      /**
       * 抽象方法: 步骤4 - 添加辅料 (子类必须实现, 咖啡加奶/糖，茶可加柠檬/蜂蜜)
       */
      protected abstract void addCondiment();
   
      /**
       * 钩子方法: 步骤4 - 是否需要添加辅料 (子类可选择性重写，默认返回 true)
       * 默认返回true（需要添加辅料），子类可根据需求修改
       */
      protected boolean needAddCondiment() {
          return true;
      }
   }
   ```
2. ***具体子类1: 咖啡制作类 ***
   ```java
   /**
    * 具体子类：咖啡制作类（实现抽象父类的抽象方法，定制咖啡制作细节）
    */
   public class CoffeeMaker extends AbstractDrinkMaker {
   
       /**
        * 实现抽象方法：冲泡咖啡粉
        */
       @Override
       protected void brewRawMaterial() {
           System.out.println("2. 冲泡烘焙好的咖啡粉");
       }
   
       /**
        * 实现抽象方法：添加奶和糖
        */
       @Override
       protected void addCondiment() {
           System.out.println("4. 添加纯牛奶和白砂糖");
       }
   
       // 可选：重写钩子方法（这里使用父类默认逻辑，即需要添加辅料）
   }
   ```
   
3. ***具体子类2: 茶制作类 ***
   ```java
   /**
    * 具体子类：茶制作类（实现抽象父类的抽象方法，定制茶制作细节）
    */
   public class TeaMaker extends AbstractDrinkMaker {
   
       /**
        * 实现抽象方法：冲泡茶叶
        */
       @Override
       protected void brewRawMaterial() {
           System.out.println("2. 冲泡龙井茶叶（80℃水温最佳）");
       }
   
       /**
        * 实现抽象方法：添加柠檬片
        */
       @Override
       protected void addCondiment() {
           System.out.println("4. 添加新鲜柠檬片");
       }
   
       /**
        * 重写钩子方法：部分人喝茶不加辅料，这里模拟“不需要添加辅料”的场景
        */
       @Override
       protected boolean needAddCondiment() {
           return false; // 不添加辅料，跳过步骤4
       }
   }
   ```

4. ***测试类: 验证模板方法模式 ***
   ```java
   /**
    * 测试类：测试咖啡和茶的制作流程
    */
   public class TemplateMethodTest {
       public static void main(String[] args) {
           System.out.println("===== 制作咖啡 =====");
           AbstractDrinkMaker coffeeMaker = new CoffeeMaker();
           coffeeMaker.makeDrink(); // 调用模板方法，执行咖啡制作流程
   
           System.out.println("\n===== 制作茶 =====");
           AbstractDrinkMaker teaMaker = new TeaMaker();
           teaMaker.makeDrink(); // 调用模板方法，执行茶制作流程
       }
   }
   ```

5. 运行结果
```text
===== 制作咖啡 =====
1. 烧开水（100℃）
2. 冲泡烘焙好的咖啡粉
3. 将冲泡好的饮品倒入杯子中
4. 添加纯牛奶和白砂糖

===== 制作茶 =====
1. 烧开水（100℃）
2. 冲泡龙井茶叶（80℃水温最佳）
3. 将冲泡好的饮品倒入杯子中
```

从结果可以看出：
- 咖啡和茶的制作流程（步骤 1、3）完全复用父类逻辑，实现了流程复用。
- 步骤 2、4 由子类定制实现，体现了细节灵活定制。
- 茶的制作因重写钩子方法，跳过了 “添加辅料” 步骤，实现了流程控制

#### 四、核心特性总结
1. ***算法骨架固定***：模板方法（final 修饰）保证了算法的整体流程不被子类修改，确保了一致性。
2. ***细节延迟绑定***：抽象方法将具体实现延迟到子类，符合 “开闭原则”（对扩展开放，对修改关闭）。
3. ***钩子方法灵活控制***：钩子方法为子类提供了干预算法流程的入口，增强了模式的灵活性。
4. ***代码复用性高***：父类中的通用步骤（如烧开水、倒入杯子）只需实现一次，所有子类均可复用

#### 五、适用场景
- 多个类的算法流程高度相似，仅部分步骤的实现不同（如上述饮品制作、报表生成、测试框架等）。
- 希望统一算法流程，同时允许子类定制具体细节。
- 需要避免子类重复编写通用的算法步骤，提高代码复用率

### n.2 

#### 一、

#### 二、

#### 总结

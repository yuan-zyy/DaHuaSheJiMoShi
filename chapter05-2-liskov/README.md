## 第5-2章 里氏替换原则
里氏替换原则（Liskov Substitution Principle，LSP），这是面向对象设计五大原则（SOLID）中的第三个，核心是保证继承体系的合理性

### 5-2.1 里氏替换原则

#### 一、里氏替换原则的核心定义
***里氏替换原则***（Liskov Substitution Principle，LSP），这是面向对象设计五大原则（SOLID）中的第三个，核心是保证继承体系的合理性
> 如果对每一个类型为 S 的对象o1，都由类型为 T 的对象o2，使得以 T 定义的所有程序 P 在所有的对象 o1 替换成 o2 时间，程序 P 的行为没有发生变化，那么 S 是 T 的子类型

用通俗的话讲: 子类可以完全替换父类，且替换后程序的行为不会出现任何异常。也就是说，父类能做的事情子类必须能做，且不能改变原有父类的语义

> ***大话设计模式***: </br>
> 一个软件实体如果使用的是一个父类的话，那么一定适用于其子类，而且它察觉不到父类对象和子类对象的区别；也就是说，在软件里面，把父类都替换成它的子类，程序的行为没有变化
>
> ***里氏替换原则:*** 子类型必须能够替换掉他们的父类型
>
> 只有当子类可以替换掉父类，软件单单位的功能不受影响时，父类才能被真正复用，而子类也能够在父类的基础上增加新的行为
>
> 正是***由于子类型的可替换性才使得父类类型的模块在无需修改的情况下就可以扩展***

#### 二、为什么要遵守里氏替换原则？
如果不遵守这个原则，继承体系会变得会乱: 比如子类重新父类的方法时改变了原有的逻辑，导致使用父类的地方替换成子类后程序出错，破坏了代码的可维护性和可扩展性

#### 三、Java 中的代码示例（正反对比）
1. 反例（违反里氏替换原则）

    假设我们定义了一个 "矩形" 类，然后定义了 ”正方形“ 类继承自矩形，但重新 setter 方法时改变了父类的语义：
    
    ```java
    // 父类
    class Rectangle {
        protected int width;
        private int height;
    
        public void setWidth(int width) {
            this.width = width;
        }
    
        public void setHeight(int height) {
            this.height = height;
        }
    
        public int getArea() {
            return width * height;
        }
    }
    
    // 子类
    class Square extends Rectangle {
        // 正方形的宽高必须相等，重写 setter 时强制修改另一个值
        @Override
        public void setWith(int with) {
            this.with = with;
            this.height = with; // 改变了父类原本 “只设置宽度” 的语义
        }
    
        @Override
        public void setHeight(int height) {
            this.height = height;
            this.with = height; // 改变了父类原本 “只设置高度” 的语义
        }
    }
    
    // 测试类：适用父类的地方替换成子类后出错
    public class LspBadExample {
        public static void main(String[] args) {
            // 期望：设置宽度为 2，高度为 3，面积 = 6
            Rectangle rectangle = new Square(); // 子类替换父类
            rectangle.setHeight(2);
            rectangle.setWidth(3);
            // 实际输出 9，不符合预期
            System.out.println("面积：" + rectangle.getArea());
        }
    }
    ```
   
    ***问题分析***：正方形继承矩形后，重新 setter 方法改变了父类的核心定义（矩形可以独立设置宽高，正方形却强制宽高相等），导致子类替换父类后程序行为异常，违反了里氏替换原则

2. 正例（遵守里氏替换原则）

   重构设计：定义更抽象的 “四边形” 父类，矩形和正方形都作为子类，且不改变父类语义：
    ```java
    // 抽象父类：四边形（定义通用行为）
    abstract class Quadrilateral {
        // 抽象方法：获取面积
        public abstract int getArea();
    }
    
    // 子类：矩形（实现父类方法，符合自身特性）
    class Rectangle extends Quadrilateral {
        private int width;
        private int height;
    
        public Rectangle(int width, int height) {
            this.width = width;
            this.height = height;
        }
    
        @Override
        public int getArea() {
            return width * height;
        }
    
        // 矩形的特有方法，不影响父类语义
        public void setWidth(int width) {
            this.width = width;
        }
    
        public void setHeight(int height) {
            this.height = height;
        }
    }
    
    // 子类：正方形（实现父类方法，符合自身特性）
    class Square extends Quadrilateral {
        private int side;
    
        public Square(int side) {
            this.side = side;
        }
    
        @Override
        public int getArea() {
            return side * side;
        }
    
        // 正方形的特有方法
        public void setSide(int side) {
            this.side = side;
        }
    }
    
    // 测试类：子类替换父类后行为正常
    public class LspGoodExample {
        // 通用方法：接收父类对象，计算面积
        public static void printArea(Quadrilateral quadrilateral) {
            System.out.println("面积：" + quadrilateral.getArea());
        }
    
        public static void main(String[] args) {
            // 矩形替换父类
            Quadrilateral rect = new Rectangle(2, 3);
            printArea(rect); // 输出6，符合预期
    
            // 正方形替换父类
            Quadrilateral square = new Square(3);
            printArea(square); // 输出9，符合预期
        }
    }
    ```
   ***分析***：正方形和矩形都继承自抽象的四边形，且各自实现getArea()方法时只遵循自身特性，没有改变父类的语义。子类替换父类后，程序行为完全符合预期，遵守了里氏替换原则。

#### 四、Java 中遵守里氏替换原则的常见注意事项
1. 子类不能重写父类的非抽象方法并改变其核心语义：比如父类的方法是 "查询数据"，子类不能重写成 “修改数据”
2. 子类新增的方法不能违背父类的设计初衷：父类定义的是 “只读” 行为，子类不能新增 “修改” 方法破坏这个约定
3. 子类的参数/返回值要兼容父类
- 子类方法的参数类型可以比父类更宽松（协变）
- 子类方法的返回值可以比父类更具体（逆变）
- 比如父类方法返回 Object，子类可以返回 String；父类的方法参数是 Obejct，子类的参数可以是 String (Java8+支持)

#### 总结
里氏替换原则的核心是***子类可完全替换父类且程序行为不变***，本质是保证继承的合理性。
违反该原则的常见场景：子类重写父类方法时改变了父类的核心语义（如矩形 / 正方形反例）。
遵守该原则的关键：让父类更抽象（如抽象类 / 接口），子类只扩展父类行为，不修改父类原有语义。
遵循里氏替换原则能让你的 Java 代码继承体系更清晰，减少因继承导致的隐藏 bug，提升代码的可维护性和可扩展性。


### 5-2.2 如何在项目中应用里氏替换原则？
下面我会结合实际项目场景，从设计阶段、编码阶段、校验阶段三个维度，给出可落地的应用方法，并附上真实项目中常见的示例，让你能直接参考。

#### 一、设计阶段：从源头规避违法 LSP 的风险
实际项目中，很多 LSP 问题是设计时 “继承关系选错” 导致的，这一步要把好关
1. 先判断：该继承还是组合？

    继承的的核心是 “is-a" (是一种) 关系，而非 ”has-a" (有一个)。如果子类和父类不是严格的 “is-a"，就用组合代替继承
    - ❌错误示例: Dog extends Animal 是合理的，但 Watermelon extends Fruit 看似合理，若父类 Fruit 由 setSweetness(设置甜度)，而子类 SeedlessWatermelon(无籽西瓜) 重新该方法强制甜度为固定值，就违法 LSP
2. 

#### 二、

#### 总结

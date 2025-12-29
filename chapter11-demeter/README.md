## 第11章 迪米特法则

### 11.1 迪米特法则
***Java 迪米特法则（Law of Demeter, LoD）***

#### 一、迪米特法则核心定义
迪米特法则(又称 "最少知识原则")的核心思想是：***一个对象应该对其他对象保持最少的了解***。也就是说，每个对象只需要和它的 ”直接盆友“ 交互，不应该和 ”陌生人“ 发生不必要的关联，降低对象之间的耦合，提高代码的可维护性和可扩展性

#### 二、核心要求：只与直接朋友交互
##### 1. 什么是 “直接朋友”
在 Java 中，一个对象的***直接朋友***包括以下几类(仅这些对象可以直接交互)
1) 该对象本身(this)
2) 方法的参数对象 
3) 该对象的成员变量(实例变量/类变量)
4) 方法内部创建的局部变量
5) 成员变量中的集合元素(如 List<User> 中的 User 对象)

##### 2. 禁止行为
***严谨通过 “中介对象”访问"***陌生人***" 的属性或方法，即不允许 A -> B -> C 的链式调用(如 c.getB().getC().doSomething())，这种链式调用会让 A 和 C 产生必须要的耦合，违反迪米特法则

#### 三、Java 中实现迪米特法则的关键方式
1. ***封装细节***: 将对象的内部状态和实现细节隐藏在类内部，仅通过公共方法暴露必要的功能，避免外部对象直接操作其私有成员
2. ***减少依赖***: 尽量降低类与类之间的直接关联，必要时可通过中间层(如工具类、服务类)解耦
3. ***避免链式调用***: 摒弃 ***对象链*** 访问，通过封装让上层对象无需感知底层对象的存在

#### 四、正反示例对比 (Java代码)
1. 反例: 违反迪米特法则

    下面的示例中，Teacher 类通过 Student(中介对象) 访问 Book(陌生人)的方法，违反的迪米特法则

    ```java
    import javax.crypto.interfaces.PBEKey;
    
    // 书籍类（陌生人：Teacher 对它无直接关联）
    public class Book {
        private String bookName;
    
        public Book(String bookName) {
            this.bookName = bookName;
        }
    
        // 获取书名(内部方法)
        public String getBookName() {
            return bookName;
        }
    }
    
    // 学生类（直接朋友：Teacher 的关联对象）
    public class Student {
        private Book book;
    
        public Student(Book book) {
            this.book = book;
        }
    
        public Book getBook() {
            return book;
        }
    }
    
    // 教师类
    public class Teacher {
        // 教师获取学生的书名（违反迪米特法则：Teacher -> Student -> Book 链式调用）
        public void getStudentBookName(Student student) {
            // 错误：通过 Student(中介) 访问Book(陌生人)的方法
            String bookName = student.getBook().getBookName();
            System.out.println("学生的书名：" + bookName);
        }
    }
    
    // 测试类
    public class LoDTest {
        public static void main(String[] args) {
            Book book = new Book("Java编程思想");
            Student student = new Student(book);
            Teacher teacher = new Teacher();
            teacher.getStudentBookName(student);
        }
    }
    ```
    
    ***问题分析***：Teacher类不需要知道Book的存在，它只需要获取学生的书名即可，但上述代码中Teacher直接操作了Book对象，导致Teacher与Book耦合，若后续Book的getBookName()方法修改，可能影响Teacher类。

2. 正例：遵循迪米特法则

   通过封装，让Student类提供获取书名的方法，Teacher仅与Student（直接朋友）交互，无需感知Book的存在
    ```java
    // 书籍类（细节被封装）
    class Book {
        private String bookName;
    
        public Book(String bookName) {
            this.bookName = bookName;
        }
    
        public String getBookName() {
            return bookName;
        }
    }
    
    // 学生类（封装与Book的交互，暴露给Teacher必要的方法）
    class Student {
        private Book book;
    
        public Student(Book book) {
            this.book = book;
        }
    
        // 新增：封装获取书名的逻辑，对外提供统一接口
        public String getMyBookName() {
            return book.getBookName();
        }
    }
    
    // 教师类（仅与直接朋友Student交互，不感知Book）
    class Teacher {
        // 遵循迪米特法则：只调用Student的方法
        public void getStudentBookName(Student student) {
            String bookName = student.getMyBookName();
            System.out.println("学生的书名：" + bookName);
        }
    }
    
    // 测试类
    public class LoDTest {
        public static void main(String[] args) {
            Book book = new Book("Java编程思想");
            Student student = new Student(book);
            Teacher teacher = new Teacher();
            teacher.getStudentBookName(student);
        }
    }
    ```
   ***优化分析***：Teacher类不再直接操作Book对象，仅通过Student提供的getMyBookName()方法获取书名，Book的修改不会影响Teacher类，降低了耦合度

#### 五、迪米特法则的意义与注意事项
1. 核心意义
   - 降低类之间的耦合度，提高代码的可维护性和可扩展性；
   - 减少代码的依赖关系，便于单元测试（无需模拟过多的依赖对象）；
   - 让代码结构更清晰，每个类的职责更单一（契合单一职责原则）。
2. 注意事项
    - 不可过度使用：若为了完全遵循迪米特法则而创建大量中间类 / 方法，会导致系统复杂度上升，可读性降低；
   - 核心是 “必要的最少了解”：不是完全禁止关联，而是禁止不必要的关联，需平衡耦合度与系统复杂度；
   - 与封装原则相辅相成：封装是迪米特法则的基础，迪米特法则是封装的延伸，二者共同实现代码的低耦合

#### 总结
- 迪米特法则核心：对象对其他对象保持最少了解，仅与直接朋友交互；
- 关键禁忌：禁止A->B->C的链式调用，避免与陌生人交互；
- 实现关键：封装内部细节、减少不必要依赖、暴露必要接口；
- 核心价值：降低耦合度，提高代码可维护性，需避免过度使用导致系统复杂


### n.2 

#### 一、

#### 二、

#### 总结

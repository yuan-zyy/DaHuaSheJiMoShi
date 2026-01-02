## 第20章 迭代器模式 

### 20.1 迭代器模式
**迭代器模式（Iterator Pattern）**

#### 一、迭代器模式的核心概念
**迭代器模式(Iterator Pattern)**是一种**行为型设计模式**，它的核心作用是: **提供一种方法访问顺序访问一个聚合对象（如集合、数组等）中的各个元素，而又不暴露该对象的内部表示（低层数据结构）

简单来说，迭代器模式实现了 “遍历逻辑” 与 “聚合对象” 的解耦：聚合对象只负责存储数据，遍历数据的职责交给迭代器，即使聚合对象的底层结构变化（如从数组改为链表），遍历逻辑也无需修改

#### 二、迭代器模式的核心角色
迭代器模式包含 4 个核心角色，职责划分清晰

##### 1. ***抽象迭代器（Iterator）***
定义遍历聚合对象的统一接口，通常包含核心方法
- hasNext(): 判断是否还有下一个原色（返回 boolean ）
- next(): 获取下一个元素（返回泛型对象）
- 可选方法: remove(): 移除当前遍历的元素（Java集合迭代器中提供的方法）

##### 2. ***具体迭代器（ConcreteIterator）***
实现抽象迭代器接口，持有聚合对象的引用，维护当前遍历的位置，实现具体的逻辑

##### 3. ***抽象聚合（Aggregate）***
定义创建迭代器的接口，核心方法是 createIterator()(返回抽象迭代器对象)，表示 “聚合对象可以生成迭代器”

##### 4. ***具体聚合（ConcreteAggregate）***
实现抽象聚合接口，创建具体迭代器的实例，自身负责存储元素数据

#### 三、完整代码实现（自定义迭代器）
下面通过 “自定义图书集合” 的例子，实现迭代器模式，清晰展示各角色的协作：
##### 1. 抽象迭代器（Iterator）

定义图书迭代器的统一接口
```java
// 抽象迭代器: 图书迭代器
public interface BookIterator {
    // 判断是否还有下一本书
    boolean hasNext();
    // 获取下一本书
    Book next();
}
```

#### 2. 具体迭代器（ConcreteIterator）
实现遍历逻辑，持有图书集合的引用

```java
import java.util.List;

// 具体迭代器: 图书集合迭代器
public class BookListIterator implements BookIterator {
    // 持有具体聚合对象的引用（存储图书的集合）
    private List<Book> bookList;
    // 维护当前遍历的索引位置
    private int index = 0;
    
    // 构造方法: 传入具体聚合对象
    public BookListIterator(List<Book> bookList) {
        this.bookList = bookList;
    }
    
    @Override
    public boolean hasNext() {
        // 索引未超出结合大小，说明还有下一个元素
        return index < bookList.size();
    }
    
    @Override
    public Book next() {
        Book book = null;
        if (hasNext()) {
            // 获取当前索引的元素，并将索引后移
            book = bookList.get(index++);
        }
        return book;
    }
}
```

#### 3. 图书实体类（辅助类）

```java
// 图书实体: 迭代器的元素类型
public class Book {
    private String bookName;
    private String author;

    public Book(String bookName, String author) {
        this.bookName = bookName;
        this.author = author;
    }

    public String getBookName() {
        return bookName;
    }
    
    public String getAuthor() {
        return author;
    }
    
    @Override
    public String toString() {
        return "《" + bookName + "》" + "作者：" + author;
    }
}
```

#### 4. 抽象聚合（Aggregate）
定义创建图书迭代器的接口
```java
// 抽象聚合: 图书集合接口
public interface BookAggregate {
    // 添加图书
    void addBook(Book book);
    
    // 移除图书
    void removeBook(Book book);
    
    // 创建迭代器（核心方法）
    BookIterator createIterator();
}
```

#### 5. 具体聚合（ConcreteAggregate）
实现图书聚合，返回具体迭代器实例
```java
public class BookList implements BookAggregate {
    // 底层数据结构（对外隐藏）
    private List<Book> bookList = new ArrayList<>();
    
    @Override
    public void addBook(Book book) {
        bookList.add(book);
    }
    
    @Override
    public void removeBook(Book book) {
        bookList.remove(book);
    }
    
    @Override
    public BookIterator createIterator() {
        return new BookListIterator(bookList);
    }
}
```

#### 6. 客户端测试代码
// 客户端: 适用迭代器遍历图书集合
```java
public class IteratorTest {
    public static void main(String[] args) {
        // 1. 创建具体聚合对象（图书列表）
        BookAggregate bookList = new BookList();

        // 2. 添加图书
        bookList.addBook(new Book("《算法导论》", "R.L.Rivest"));
        bookList.addBook(new Book("《数据结构》", "R.L.Rivest"));
        bookList.addBook(new Book("《计算机网络》", "R.L.Rivest"));
        
        // 3. 创建迭代器
        System.out.println("图书列表遍历: ");
        BookIterator bookIterator = bookList.createIterator();
        while (bookIterator.hasNext()) {
            Book book = bookIterator.next();
            System.out.println(book);
        }
    }
}
```

##### 7. 运行结果
```text
图书列表遍历：
《Java编程思想》- 作者：Bruce Eckel
《深入理解Java虚拟机》- 作者：周志明
《Effective Java》- 作者：Joshua Bloch
```

#### 四、Java 内置迭代器（JDK 实现）
Java 集合框架（java.util）已经内置了迭代器模式，无需自定义即可直接使用，核心相关类 / 接口：

##### 1. 抽象迭代器
java.util.Iterator（JDK 1.2 引入），核心方法：
- boolean hasNext()：判断是否存在下一个元素
- E next()：获取下一个元素
- void remove()：移除当前元素（默认抛出UnsupportedOperationException，需具体实现类支持）

##### 2. 抽象聚合
java.util.Collection（所有集合的根接口），核心方法 Iterator<E> iterator()（创建迭代器）

##### 3. 具体聚合
ArrayList、LinkedList、HashSet 等（实现Collection接口）

##### 4. 具体迭代器
各集合内部的私有迭代器实现（如ArrayList中的Itr内部类，对外隐藏）

##### JDK 内置迭代器使用示例
```java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JdkIteratorDemo {
    public static void main(String[] args) {
        List<String> strList = new ArrayList<>();
        strList.add("Java");
        strList.add("Python");
        strList.add("C++");

        // 1. 获取内置迭代器
        Iterator<String> iterator = strList.iterator();

        // 2. 遍历集合
        while (iterator.hasNext()) {
            String str = iterator.next();
            System.out.println(str);
            
            // 可选：移除元素（注意：不能用集合的remove方法，会触发并发修改异常）
            if ("Python".equals(str)) {
                iterator.remove();
            }
        }

        System.out.println("移除Python后的集合：" + strList);
    }
}
```

#### 五、迭代器模式的核心优缺点
##### 优点
1. **解耦遍历与聚合对象**：遍历逻辑封装在迭代器中，聚合对象无需关心遍历细节，底层数据结构变化（如ArrayList改LinkedList）不影响遍历代码。
2. **统一遍历接口**：所有聚合对象的迭代器都实现统一接口（如Iterator），客户端可以用相同的代码遍历不同的聚合对象（如ArrayList、HashSet）。
3. **便于扩展**：新增聚合对象时，只需实现对应的迭代器即可，无需修改现有遍历代码（符合开闭原则）

##### 缺点
1. **增加类的数量**：每一个具体聚合对象都需要对应一个具体迭代器，当聚合对象较多时，会导致类的数量成倍增加，增加系统复杂度。
2. **遍历效率受限**：对于某些特殊聚合对象（如数组），直接遍历可能比通过迭代器遍历更高效，迭代器的封装会带来少量性能开销。
3. **单向遍历为主**：Java 内置迭代器默认是单向遍历（从前往后），如需双向遍历（如ListIterator），需要额外实现，增加复杂度

#### 总结
1. 迭代器模式是行为型模式，核心是**解耦聚合对象与遍历逻辑，隐藏聚合对象内部结构**。
2. 核心四角色：抽象迭代器（Iterator）、具体迭代器（ConcreteIterator）、抽象聚合（Aggregate）、具体聚合（ConcreteAggregate）。
3. Java 集合框架已内置迭代器实现（java.util.Iterator），日常开发无需自定义，直接使用即可。
4. 优势是统一遍历接口、解耦结构，劣势是增加类数量、少量性能开销，适用于需要统一遍历多种聚合对象的场景。


### n.2 

#### 一、

#### 二、

#### 总结

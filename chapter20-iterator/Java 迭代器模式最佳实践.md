# Java 迭代器模式最佳实践

你希望掌握Java迭代器模式的最佳实践，这些实践既能规避常见问题，又能充分发挥迭代器模式的优势，涵盖**JDK内置迭代器**（日常开发主流）和**自定义迭代器**（特殊场景）的核心技巧，同时明确避坑要点和适用边界。

## 一、 优先使用JDK内置迭代器，避免重复造轮子

Java集合框架（`java.util`）已经提供了成熟、高效的迭代器实现（`Iterator`/`ListIterator`），这是日常开发的首选，无需手动自定义迭代器。

### 核心实践

1. 遍历标准集合（`ArrayList`/`LinkedList`/`HashSet`/`HashMap`等）时，直接通过`iterator()`/`listIterator()`获取迭代器；

2. 借助增强`for`循环（语法糖，底层仍基于`Iterator`）简化遍历代码，提升可读性；

3. Map集合遍历优先使用`entrySet().iterator()`（可同时获取键值对，性能优于`keySet()`遍历）。

### 代码示例

```java

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BuiltInIteratorBestPractice {
    public static void main(String[] args) {
        // 1. 增强for循环（简化迭代器使用，底层依赖Iterator）
        List<String> productList = new ArrayList<>();
        productList.add("手机");
        productList.add("电脑");
        productList.add("平板");
        System.out.println("增强for循环遍历List：");
        for (String product : productList) {
            System.out.println(product);
        }

        // 2. Map集合最优遍历：entrySet迭代器
        Map<Integer, String> userMap = new HashMap<>();
        userMap.put(1, "张三");
        userMap.put(2, "李四");
        userMap.put(3, "王五");
        System.out.println("\nMap entrySet迭代器遍历：");
        Iterator<Map.Entry<Integer, String>> entryIterator = userMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Integer, String> entry = entryIterator.next();
            System.out.println("用户ID：" + entry.getKey() + "，用户名：" + entry.getValue());
        }
    }
}
```

## 二、 遍历中移除元素：必须使用迭代器的`remove()`方法

这是迭代器使用的核心避坑点，**禁止在遍历过程中调用聚合对象（集合）自身的`remove()`方法**，否则会触发`ConcurrentModificationException`（并发修改异常）；如需移除元素，务必使用迭代器的`remove()`方法。

### 核心原因

集合的`remove()`方法会修改集合的修改次数（`modCount`），而迭代器遍历过程中会校验`modCount`与迭代器内部维护的`expectedModCount`是否一致，不一致则抛出异常；迭代器的`remove()`方法会同步更新`expectedModCount`，避免异常触发。

### 代码示例（正确用法）

```java

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorRemoveBestPractice {
    public static void main(String[] args) {
        List<String> fruitList = new ArrayList<>();
        fruitList.add("苹果");
        fruitList.add("香蕉");
        fruitList.add("橙子");
        fruitList.add("榴莲");

        // 正确：使用迭代器remove()移除元素（过滤榴莲）
        Iterator<String> iterator = fruitList.iterator();
        while (iterator.hasNext()) {
            String fruit = iterator.next();
            if ("榴莲".equals(fruit)) {
                iterator.remove(); // 迭代器移除，安全无异常
                System.out.println("移除元素：" + fruit);
                continue;
            }
            System.out.println("当前元素：" + fruit);
        }
        System.out.println("过滤后的集合：" + fruitList);

        // 错误：遍历中调用fruitList.remove()（会抛出ConcurrentModificationException）
        // for (String fruit : fruitList) {
        //     if ("香蕉".equals(fruit)) {
        //         fruitList.remove(fruit); // 禁止使用
        //     }
        // }
    }
}
```

## 三、 避免重复调用`next()`方法，防止元素跳过

`Iterator`的`next()`方法有两个核心作用：① 返回当前遍历位置的元素；② 将遍历指针向后移动一位。重复调用`next()`会导致指针异常后移，跳过部分元素，这是高频错误。

### 核心实践

1. 每次循环中，`next()`方法仅调用一次；

2. 若需多次使用当前元素，先将`next()`的返回值赋值给局部变量，再操作局部变量。

### 代码示例（正反对比）

```java

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorNextBestPractice {
    public static void main(String[] args) {
        List<Integer> numList = new ArrayList<>();
        numList.add(10);
        numList.add(20);
        numList.add(30);
        numList.add(40);

        System.out.println("错误用法（重复调用next()，跳过元素）：");
        Iterator<Integer> errorIterator = numList.iterator();
        while (errorIterator.hasNext()) {
            // 错误：多次调用next()，指针连续后移，跳过20、40
            System.out.println("判断是否大于15：" + (errorIterator.next() > 15));
            System.out.println("当前元素：" + errorIterator.next());
        }

        System.out.println("\n正确用法（仅调用一次next()，赋值给局部变量）：");
        Iterator<Integer> correctIterator = numList.iterator();
        while (correctIterator.hasNext()) {
            Integer num = correctIterator.next(); // 仅调用一次，赋值给局部变量
            System.out.println("判断是否大于15：" + (num > 15));
            System.out.println("当前元素：" + num);
        }
    }
}
```

## 四、 双向遍历优先使用`ListIterator`（仅支持List集合）

若业务需要正序+倒序遍历（如商品列表先正序展示，再倒序展示），优先使用`ListIterator`（继承自`Iterator`），它支持双向遍历，无需手动反转集合，更高效简洁。

### 核心优势

1. 支持`hasPrevious()`（判断是否有前一个元素）和`previous()`（获取前一个元素），实现倒序遍历；

2. 支持`add()`/`set()`方法，可在遍历中添加/修改元素（比`Iterator`功能更丰富）；

3. 仅依赖List集合，无需额外工具类。

### 代码示例

```java

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ListIteratorBestPractice {
    public static void main(String[] args) {
        List<String> songList = new ArrayList<>();
        songList.add("歌曲1：晴天");
        songList.add("歌曲2：花海");
        songList.add("歌曲3：稻香");

        // 获取ListIterator
        ListIterator<String> listIterator = songList.listIterator();

        // 1. 正序遍历
        System.out.println("正序遍历歌曲列表：");
        while (listIterator.hasNext()) {
            System.out.println(listIterator.next());
        }

        // 2. 倒序遍历（指针已在集合末尾，直接使用hasPrevious()）
        System.out.println("\n倒序遍历歌曲列表：");
        while (listIterator.hasPrevious()) {
            System.out.println(listIterator.previous());
        }

        // 3. 遍历中添加元素
        listIterator.next(); // 移动到第一个元素
        listIterator.add("歌曲0：七里香"); // 在当前位置前添加元素
        System.out.println("\n添加元素后的列表：" + songList);
    }
}
```

## 五、 自定义迭代器：遵循接口隔离原则，封装遍历逻辑

仅当JDK内置迭代器无法满足特殊需求（如固定倒序遍历、过滤遍历、自定义聚合对象）时，才实现自定义迭代器，核心遵循“接口隔离”和“单一职责”原则。

### 核心实践

1. 抽象迭代器仅定义必要方法（至少`hasNext()`和`next()`），避免冗余；

2. 具体迭代器持有聚合对象引用，维护遍历状态（索引/指针），封装所有遍历逻辑，对外隐藏；

3. 具体聚合对象仅负责存储元素，通过`createIterator()`方法返回迭代器，解耦遍历与存储；

4. 优先使用泛型，提升迭代器的通用性和类型安全性。

### 代码示例（自定义过滤迭代器：仅遍历成年员工）

```java

import java.util.ArrayList;
import java.util.List;

// 员工实体
class Employee {
    private String name;
    private int age;

    public Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "姓名：" + name + "，年龄：" + age;
    }
}

// 抽象迭代器：泛型接口，仅定义核心方法
interface FilterIterator<T> {
    boolean hasNext();
    T next();
}

// 具体迭代器：过滤成年员工（年龄>=18）
class AdultEmployeeIterator implements FilterIterator<Employee> {
    private List<Employee> empList;
    private int index;

    public AdultEmployeeIterator(List<Employee> empList) {
        this.empList = empList;
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        // 过滤逻辑：找到下一个成年员工
        while (index < empList.size()) {
            if (empList.get(index).getAge() >= 18) {
                return true;
            }
            index++;
        }
        return false;
    }

    @Override
    public Employee next() {
        Employee emp = null;
        if (this.hasNext()) {
            emp = empList.get(index++);
        }
        return emp;
    }
}

// 抽象聚合
interface EmployeeAggregate {
    void addEmployee(Employee emp);
    FilterIterator<Employee> createAdultIterator();
}

// 具体聚合
class EmployeeList implements EmployeeAggregate {
    private List<Employee> empList = new ArrayList<>();

    @Override
    public void addEmployee(Employee emp) {
        empList.add(emp);
    }

    @Override
    public FilterIterator<Employee> createAdultIterator() {
        return new AdultEmployeeIterator(empList); // 返回自定义过滤迭代器
    }
}

// 客户端使用
public class CustomIteratorBestPractice {
    public static void main(String[] args) {
        EmployeeAggregate empAggregate = new EmployeeList();
        empAggregate.addEmployee(new Employee("张三", 25));
        empAggregate.addEmployee(new Employee("李四", 17));
        empAggregate.addEmployee(new Employee("王五", 30));
        empAggregate.addEmployee(new Employee("赵六", 16));

        // 使用自定义过滤迭代器，仅遍历成年员工
        FilterIterator<Employee> adultIterator = empAggregate.createAdultIterator();
        System.out.println("成年员工列表：");
        while (adultIterator.hasNext()) {
            System.out.println(adultIterator.next());
        }
    }
}
```

## 六、 明确迭代器模式的适用边界，避免过度使用

迭代器模式并非万能，需明确其适用场景，避免为了设计模式而过度设计。

### 适用场景

1. 需要统一遍历多种聚合对象（如同时遍历`ArrayList`和自定义业务集合）；

2. 无需暴露聚合对象底层存储结构（如隐藏自定义集合的数组/链表实现）；

3. 需要自定义遍历规则（倒序、过滤、跳跃遍历等）；

### 不适用场景

1. 简单遍历数组：直接使用`for`循环更高效，无需迭代器；

2. 聚合对象结构简单，且无需统一遍历接口：直接遍历比迭代器更简洁；

3. 高性能要求的场景：迭代器的封装会带来少量性能开销，简单遍历更优。

## 七、 结合Java 8+ Stream API，提升遍历效率与可读性

Java 8及以上版本中，Stream API提供了更强大的遍历、筛选、映射能力，底层也兼容迭代器模式，可结合使用，提升代码简洁度和效率（尤其是大数据量处理）。

### 代码示例

```java

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IteratorWithStreamBestPractice {
    public static void main(String[] args) {
        List<Employee> empList = new ArrayList<>();
        empList.add(new Employee("张三", 25, 10000));
        empList.add(new Employee("李四", 30, 15000));
        empList.add(new Employee("王五", 28, 12000));

        // 结合Stream API：筛选工资>10000的员工，并收集姓名列表
        List<String> highSalaryEmpNames = empList.stream()
                .filter(emp -> emp.getSalary() > 10000) // 过滤
                .map(Employee::getName) // 映射
                .collect(Collectors.toList()); // 收集结果

        System.out.println("工资>10000的员工姓名：" + highSalaryEmpNames);
    }

    // 员工实体（补充工资字段）
    static class Employee {
        private String name;
        private int age;
        private double salary;

        public Employee(String name, int age, double salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }

        public String getName() {
            return name;
        }

        public double getSalary() {
            return salary;
        }
    }
}
```

## 总结

Java迭代器模式的最佳实践核心可归纳为：

1. **优先内置**：日常开发使用JDK内置`Iterator`/`ListIterator`，避免自定义迭代器；

2. **安全移除**：遍历中移除元素必须使用迭代器`remove()`，禁止使用集合自身`remove()`；

3. **慎用`next()`**：每次循环仅调用一次`next()`，避免元素跳过；

4. **双向遍历**：List集合双向遍历优先使用`ListIterator`；

5. **自定义规范**：自定义迭代器需封装遍历逻辑，遵循接口隔离原则，使用泛型提升通用性；

6. **明确边界**：避免过度使用，简单场景直接遍历更高效；

7. **结合新特性**：Java 8+ 结合Stream API，提升遍历可读性和效率。
> （注：文档部分内容可能由 AI 生成）
# 备忘录模式在框架及JDK中的应用案例

备忘录模式的核心是“在不破坏封装的前提下保存对象状态，支持后续恢复”，这一设计思想广泛应用于JDK基础类库、主流开发框架中，解决了状态回溯、操作撤销、数据一致性保障等关键问题。本文将从JDK原生实现、Spring框架、MyBatis框架三个维度，拆解备忘录模式的具体应用场景、角色映射及实现逻辑。

## 一、JDK中的备忘录模式应用

JDK内部多个核心类库隐式采用了备忘录模式的设计思路，其中最典型的是`java.util.Date`（历史状态保存）与`java.awt.event.ActionEvent`（事件状态快照），以及字符串常量池的状态复用机制。

### 案例1：java.util.Date（日期状态的快照与恢复）

#### 1. 应用场景

Date类用于表示特定的时间点，其内部通过`fastTime`字段（long类型，存储自1970年1月1日以来的毫秒数）维护核心状态。当需要保存某个时间点的状态（如记录操作时间戳快照），或基于历史时间点进行后续计算时，本质是通过Date对象的“不可变快照”实现状态保存，符合备忘录模式的核心逻辑。

#### 2. 角色映射

- 原发器（Originator）：业务代码中使用的Date对象（如记录操作时间的date实例），负责创建自身状态的快照（通过构造方法复制状态）。

- 备忘录（Memento）：Date对象本身（不可变特性保障状态不被篡改），其`fastTime`字段存储核心状态，且仅通过getter方法（`getTime()`）暴露状态，无setter方法（JDK 8+中Date的setTime方法虽存在，但官方推荐使用LocalDateTime等不可变类，本质是引导开发者将Date作为状态快照使用）。

- 管理者（Caretaker）：业务代码中的容器（如List<Date>）或变量，负责存储Date快照对象，不修改其内部状态。

#### 3. 实现原理示例

```java

// 1. 原发器：创建初始时间状态
Date originalDate = new Date();
System.out.println("初始状态：" + originalDate);

// 2. 创建备忘录（快照）：通过构造方法复制原始状态
Date mementoDate = new Date(originalDate.getTime()); // 核心：复制fastTime字段，生成状态快照

// 3. 管理者：存储快照
List<Date> timeSnapshots = new ArrayList<>();
timeSnapshots.add(mementoDate);

// 4. 修改原始状态（模拟业务操作）
originalDate.setTime(originalDate.getTime() + 3600000); // 增加1小时
System.out.println("修改后状态：" + originalDate);

// 5. 恢复状态：从备忘录中获取快照，恢复原始状态
originalDate.setTime(timeSnapshots.get(0).getTime());
System.out.println("恢复后状态：" + originalDate);
    
```

#### 4. 核心价值

通过Date对象的不可变特性（本质是状态快照），保证了时间状态的一致性，避免了并发场景下状态被篡改的问题；同时简化了状态保存逻辑，无需额外定义备忘录类，直接复用Date的不可变特性实现快照功能。

### 案例2：字符串常量池（String的不可变与状态复用）

String类是典型的不可变类，其内部char数组存储字符串内容，且无任何修改数组的方法。字符串常量池本质是“状态快照的管理者”，存储字符串的唯一状态快照，供多个对象复用，符合备忘录模式“状态保存与复用”的核心思想。

- 原发器：业务代码中的String变量（如String name = "张三"）。

- 备忘录：常量池中的String对象（不可变，存储核心状态char数组）。

- 管理者：字符串常量池（方法区中的内存区域），负责管理String快照的创建、复用与存储。

## 二、Spring框架中的备忘录模式应用

Spring框架中，备忘录模式主要应用于**事务管理**（状态回滚）与**BeanDefinition的状态保存**，其中事务管理是最典型的场景，通过保存事务执行前的数据源状态，实现事务回滚功能。

### 案例：Spring事务管理（DataSourceTransactionManager）

#### 1. 应用场景

当使用Spring声明式事务（@Transactional）时，若事务执行过程中出现异常，Spring会自动回滚到事务执行前的状态（如数据库连接的自动提交状态、事务隔离级别等）。这一过程本质是通过备忘录模式保存数据源的核心状态，异常时恢复状态。

#### 2. 角色映射

- 原发器（Originator）：`DataSourceTransactionManager`（数据源事务管理器），负责管理事务的开始、提交与回滚，同时创建数据源状态的快照。

- 备忘录（Memento）：`TransactionStatus`接口的实现类（如`DefaultTransactionStatus`），存储事务执行前的核心状态：数据库连接（Connection）、自动提交状态（autoCommit）、事务隔离级别（isolationLevel）、是否为新事务（newTransaction）等。

- 管理者（Caretaker）：`TransactionSynchronizationManager`（事务同步管理器），通过ThreadLocal存储当前线程的TransactionStatus对象（备忘录），确保线程安全，且仅负责存储，不修改状态。

#### 3. 实现原理拆解

1. 事务开始时（beginTransaction）：

        DataSourceTransactionManager从数据源获取Connection，保存其原始状态（autoCommit=true），然后将autoCommit设为false（开启事务）；同时创建DefaultTransactionStatus对象（备忘录），封装Connection、原始autoCommit状态等信息，并通过TransactionSynchronizationManager存储到当前线程的ThreadLocal中。
      

2. 事务执行中：

        业务代码操作数据库，所有操作基于当前Connection执行，TransactionSynchronizationManager负责维护备忘录的线程隔离。
     

3. 事务回滚时（rollback）：

        DataSourceTransactionManager从TransactionSynchronizationManager获取TransactionStatus（备忘录），恢复Connection的原始状态（将autoCommit设为true），并执行数据库回滚操作（rollback()）。
      

4. 事务提交时（commit）：

        执行数据库提交操作（commit()），然后恢复Connection的原始状态，最后删除ThreadLocal中的备忘录对象。

#### 4. 核心价值

通过备忘录模式封装了事务状态的保存与恢复逻辑，隔离了事务管理与业务逻辑，开发者无需手动处理连接状态的切换与回滚，降低了开发复杂度；同时通过ThreadLocal保证了多线程场景下的状态隔离，避免了事务状态污染。

## 三、MyBatis框架中的备忘录模式应用

MyBatis中，备忘录模式主要应用于**SqlSession的事务状态管理**与**MapperStatement的状态缓存**，其中SqlSession的事务回滚机制是核心应用场景，与Spring事务管理异曲同工，但更偏向于框架内部的状态控制。

### 案例：SqlSession的事务状态管理

#### 1. 应用场景

MyBatis的SqlSession是操作数据库的核心对象，支持手动事务管理（openSession(false)关闭自动提交）。当调用sqlSession.rollback()时，MyBatis会回滚到事务开始前的状态，这一过程通过保存数据库连接的状态快照实现，符合备忘录模式。

#### 2. 角色映射

- 原发器（Originator）：SqlSession实例（默认实现为DefaultSqlSession），负责创建事务状态快照、执行SQL操作、触发事务回滚/提交。

- 备忘录（Memento）：`Transaction`接口的实现类（如JdbcTransaction），封装了数据库连接（Connection）、事务隔离级别、自动提交状态等核心状态，且仅通过getter方法暴露状态，由SqlSession控制状态的修改。

- 管理者（Caretaker）：SqlSessionFactory或DefaultSqlSession的内部变量，负责存储Transaction对象（备忘录），不直接操作事务状态。

#### 3. 实现原理示例

```java

// 1. 获取SqlSession（关闭自动提交，开启手动事务）
SqlSession sqlSession = sqlSessionFactory.openSession(false);

try {
    // 2. 执行业务操作（模拟状态修改）
    UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
    userMapper.updateUserName(1, "新名称");
    
    // 3. 模拟异常
    int i = 1 / 0;
    
    // 4. 提交事务（无异常时提交，删除备忘录）
    sqlSession.commit();
} catch (Exception e) {
    // 5. 回滚事务（从备忘录恢复状态）
    sqlSession.rollback(); // 核心：通过Transaction对象恢复Connection的原始状态，执行回滚
} finally {
    sqlSession.close();
}
    
```

#### 4. 核心价值

通过Transaction对象封装事务状态，实现了SqlSession与事务状态的解耦；同时保证了事务回滚的准确性，避免了因连接状态混乱导致的事务失败问题，简化了手动事务管理的逻辑。

## 四、共性总结与设计启示

### 1. 框架中应用的共性

- 备忘录的“不可变性”：框架中的备忘录对象（如TransactionStatus、Date）均通过限制状态修改（无setter、final字段）保障状态一致性，避免快照被篡改。

- 管理者的“线程安全”：框架均通过ThreadLocal（如Spring的TransactionSynchronizationManager）或单例容器管理备忘录，确保多线程场景下的状态隔离。

- 低侵入性：框架将备忘录模式的实现隐藏在内部，开发者无需感知角色划分，仅通过API（如commit、rollback）即可使用状态保存与恢复功能。

### 2. 设计启示

在实际开发中，若需实现状态回溯功能（如操作撤销、数据备份），可借鉴框架的设计思路：

- 优先使用不可变对象作为备忘录，减少状态篡改风险；

- 通过容器（如List、ThreadLocal）作为管理者，简化备忘录的存储与获取；

- 隔离备忘录的创建与使用逻辑，让原发器专注于业务逻辑，管理者专注于状态存储，符合单一职责原则。

总之，备忘录模式在框架中的应用核心是“通过封装状态快照，实现低侵入、高一致性的状态管理”，这一设计思想不仅适用于框架开发，也可广泛应用于业务系统的状态回溯、数据备份等场景。
> （注：文档部分内容可能由 AI 生成）
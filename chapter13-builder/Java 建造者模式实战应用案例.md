# Java 建造者模式实战应用案例

建造者模式在 Java 开发中应用广泛，既包括**JDK 原生API**的内置实现，也包括**主流框架**和**业务开发**中的常规使用。下面按「JDK 内置案例」「框架案例」「自定义业务案例」分类讲解，附带可运行代码和核心解析。

## 一、JDK 原生 API 中的建造者模式应用

### 案例 1：`java.lang.StringBuilder`（经典建造者模式变体）

`StringBuilder` 是 JDK 中最典型的建造者模式应用之一（简化版，无抽象建造者和指挥者），用于分步构建字符串对象。

#### 核心解析

- 产品（Product）：最终构建的 `String` 对象

- 建造者（Builder）：`StringBuilder` 自身（既是建造者，也持有产品的构建状态）

- 分步构建：通过 `append()`、`insert()` 等方法分步添加字符串内容（链式调用）

- 构建方法：`toString()` 方法最终生成并返回完整的 `String` 对象

#### 使用示例

```java

public class StringBuilderDemo {

public static void main(String[] args) {

// 分步构建字符串（链式调用，建造者模式核心特性）

StringBuilder sb = new StringBuilder()

.append("Java") // 步骤1：添加基础字符串

.append(" 建造者模式") // 步骤2：追加内容

.insert(4, "语言") // 步骤3：插入内容

.delete(9, 11); // 步骤4：删除多余内容



// 最终构建（生成成品String对象）

String result = sb.toString();

System.out.println("构建结果：" + result); // 输出：Java语言 建造者式

}

}

```

### 案例 2：`java.nio.ByteBuffer`（通过 `allocate()` + `put()` 分步构建）

`ByteBuffer` 用于构建字节缓冲区对象，采用建造者模式的设计思想，支持分步写入字节数据。

#### 核心解析

- 产品（Product）：`ByteBuffer` 实例（不可变特性，构建后可直接使用）

- 建造者（Builder）：`ByteBuffer` 子类（如 `HeapByteBuffer`）

- 分步构建：`put()` 方法（支持字节、字符、数组等多种类型写入）

- 初始化方式：`allocate()`（堆内存）/ `allocateDirect()`（直接内存）相当于建造者的构造方法

#### 使用示例

```java

import java.nio.ByteBuffer;

import java.nio.charset.StandardCharsets;



public class ByteBufferDemo {

public static void main(String[] args) {

// 1. 初始化建造者（指定缓冲区大小，相当于必选参数）

ByteBuffer buffer = ByteBuffer.allocate(1024);



// 2. 分步构建（写入数据，链式调用）

buffer.put("Hello".getBytes(StandardCharsets.UTF_8))

.put((byte) ',')

.put(" Builder".getBytes(StandardCharsets.UTF_8));



// 3. 切换为读取模式（完成构建前的准备）

buffer.flip();



// 4. 读取构建好的字节数据（获取成品）

byte[] resultBytes = new byte[buffer.remaining()];

buffer.get(resultBytes);

String result = new String(resultBytes, StandardCharsets.UTF_8);

System.out.println("ByteBuffer 构建结果：" + result); // 输出：Hello, Builder

}

}

```

## 二、主流框架中的建造者模式应用

### 案例 1：MyBatis - `SqlSessionFactoryBuilder`

MyBatis 中使用 `SqlSessionFactoryBuilder` 构建 `SqlSessionFactory` 对象，是经典建造者模式的应用。

#### 核心解析

- 产品（Product）：`SqlSessionFactory`（MyBatis 核心工厂，用于创建 `SqlSession`）

- 建造者（Builder）：`SqlSessionFactoryBuilder`

- 构建方法：提供多个 `build()` 重载方法，支持从 `InputStream`、`Reader`、`Configuration` 等不同来源构建

- 无指挥者：简化版建造者模式，直接通过建造者的 `build()` 方法完成构建

#### 使用示例

```java

import org.apache.ibatis.io.Resources;

import org.apache.ibatis.session.SqlSession;

import org.apache.ibatis.session.SqlSessionFactory;

import org.apache.ibatis.session.SqlSessionFactoryBuilder;



import java.io.IOException;

import java.io.InputStream;



public class MyBatisBuilderDemo {

public static void main(String[] args) throws IOException {

// 1. 加载MyBatis配置文件

String resource = "mybatis-config.xml";

InputStream inputStream = Resources.getResourceAsStream(resource);



// 2. 建造者模式：构建 SqlSessionFactory（产品）

SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()

.build(inputStream); // 核心构建方法



// 3. 通过产品获取 SqlSession

try (SqlSession session = sqlSessionFactory.openSession(true)) {

System.out.println("SqlSession 创建成功：" + session);

}

}

}

```

### 案例 2：Spring - `ApplicationContext` 构建（变体）

Spring 中 `AnnotationConfigApplicationContext`、`ClassPathXmlApplicationContext` 本质上采用了建造者模式的思想，分步构建 IoC 容器。

#### 核心解析

- 产品（Product）：`ApplicationContext`（Spring 核心 IoC 容器）

- 建造者（Builder）：`ApplicationContext` 实现类自身

- 分步构建：通过 `scan()`（扫描包）、`register()`（注册Bean）等方法分步配置容器

- 自动构建：构造方法执行后自动完成容器初始化（`refresh()` 方法相当于 `build()`）

#### 使用示例

```java

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;



// 配置类

@Configuration

class SpringConfig {

@Bean

public User springUser() {

// 此处复用自定义User建造者

return new User.UserBuilder("spring_user", "12345678")

.age(30)

.email("spring@example.com")

.build();

}

}



public class SpringBuilderDemo {

public static void main(String[] args) {

// 建造者模式：构建 ApplicationContext（IoC容器）

AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()

.register(SpringConfig.class); // 步骤1：注册配置类



// 完成构建（刷新容器，相当于 build()）

context.refresh();



// 获取产品（Bean对象）

User user = context.getBean(User.class);

System.out.println("Spring 容器构建的User：" + user);



// 关闭容器

context.close();

}

}

```

### 案例 3：OkHttp - `Request.Builder`（主流HTTP客户端，链式建造者经典）

OkHttp 中 `Request` 对象的构建是**内部静态建造者模式**的完美体现，也是业务开发的参考典范。

#### 核心解析

- 产品（Product）：`Request`（HTTP 请求对象）

- 建造者（Builder）：`Request.Builder`（`Request` 的内部静态类）

- 链式调用：`url()`、`method()`、`header()`、`addHeader()` 等方法返回 `this`，支持灵活配置

- 构建方法：`build()` 方法最终生成 `Request` 实例

#### 使用示例

```java

import okhttp3.OkHttpClient;

import okhttp3.Request;

import okhttp3.Response;



import java.io.IOException;



public class OkHttpBuilderDemo {

public static void main(String[] args) throws IOException {

// 1. 创建OkHttpClient实例

OkHttpClient client = new OkHttpClient();



// 2. 建造者模式：构建 Request 对象（链式调用，灵活配置）

Request request = new Request.Builder()

.url("https://www.baidu.com") // 必选参数：请求URL

.get() // 请求方法：GET

.header("User-Agent", "OkHttp-Builder-Demo") // 请求头

.addHeader("Accept", "text/html") // 追加请求头

.build(); // 最终构建



// 3. 执行请求

try (Response response = client.newCall(request).execute()) {

System.out.println("请求状态码：" + response.code());

System.out.println("响应体：" + response.body().string());

}

}

}

```

## 三、自定义业务开发中的建造者模式应用

### 案例：订单对象（Order）构建（业务开发高频场景）

订单对象包含大量必选属性（订单号、用户ID、商品列表）和可选属性（优惠金额、备注、收货地址、支付方式等），使用建造者模式可避免构造方法爆炸，提高代码可读性。

#### 1. 产品类 + 内部静态建造者

```java

import java.math.BigDecimal;

import java.util.List;



/**

* 业务产品类：订单（Order）

* 包含必选属性和大量可选属性，适合使用建造者模式

*/

public class Order {

// 必选属性（final 修饰，确保不可变）

private final String orderNo; // 订单号

private final String userId; // 用户ID

private final List<String> productIds; // 商品ID列表

private final BigDecimal totalAmount; // 订单总金额



// 可选属性（final 修饰，无setter，确保对象不可变）

private final BigDecimal discountAmount; // 优惠金额

private final String remark; // 订单备注

private final String receiveAddress; // 收货地址

private final String payType; // 支付方式（WECHAT/ALIPAY/BANK）



// 私有构造方法：仅允许内部Builder调用

private Order(OrderBuilder builder) {

this.orderNo = builder.orderNo;

this.userId = builder.userId;

this.productIds = builder.productIds;

this.totalAmount = builder.totalAmount;

this.discountAmount = builder.discountAmount;

this.remark = builder.remark;

this.receiveAddress = builder.receiveAddress;

this.payType = builder.payType;

}



// 内部静态建造者类（业务开发首选）

public static class OrderBuilder {

// 必选属性（无final，构造方法强制传入）

private String orderNo;

private String userId;

private List<String> productIds;

private BigDecimal totalAmount;



// 可选属性（初始化默认值）

private BigDecimal discountAmount = BigDecimal.ZERO;

private String remark = "";

private String receiveAddress = "";

private String payType = "WECHAT"; // 默认微信支付



// 建造者构造方法：强制传入必选属性

public OrderBuilder(String orderNo, String userId, List<String> productIds, BigDecimal totalAmount) {

this.orderNo = orderNo;

this.userId = userId;

this.productIds = productIds;

this.totalAmount = totalAmount;

}



// 可选属性赋值方法：链式调用（返回this）

public OrderBuilder discountAmount(BigDecimal discountAmount) {

this.discountAmount = discountAmount;

return this;

}



public OrderBuilder remark(String remark) {

this.remark = remark;

return this;

}



public OrderBuilder receiveAddress(String receiveAddress) {

this.receiveAddress = receiveAddress;

return this;

}



public OrderBuilder payType(String payType) {

this.payType = payType;

return this;

}



// 构建方法：创建Order实例，支持参数校验

public Order build() {

// 参数校验：避免无效订单

if (this.orderNo == null || this.orderNo.trim().isEmpty()) {

throw new IllegalArgumentException("订单号不能为空");

}

if (this.productIds == null || this.productIds.isEmpty()) {

throw new IllegalArgumentException("商品列表不能为空");

}

if (this.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {

throw new IllegalArgumentException("订单总金额必须大于0");

}

return new Order(this);

}

}



// 省略getter方法

public String getOrderNo() { return orderNo; }

public String getUserId() { return userId; }

public List<String> getProductIds() { return productIds; }

public BigDecimal getTotalAmount() { return totalAmount; }

public BigDecimal getDiscountAmount() { return discountAmount; }

public String getRemark() { return remark; }

public String getReceiveAddress() { return receiveAddress; }

public String getPayType() { return payType; }



@Override

public String toString() {

return "Order{" +

"orderNo='" + orderNo + '\'' +

", userId='" + userId + '\'' +

", productIds=" + productIds +

", totalAmount=" + totalAmount +

", discountAmount=" + discountAmount +

", remark='" + remark + '\'' +

", receiveAddress='" + receiveAddress + '\'' +

", payType='" + payType + '\'' +

'}';

}

}

```

#### 2. 测试调用（业务场景使用）

```java

import java.math.BigDecimal;

import java.util.Arrays;

import java.util.List;



public class OrderBuilderTest {

public static void main(String[] args) {

// 1. 准备商品列表

List<String> productIds = Arrays.asList("P001", "P002", "P003");



// 2. 构建完整订单（必选属性 + 所有可选属性，链式调用）

Order fullOrder = new Order.OrderBuilder("ORDER_20251230_001", "U1001", productIds, new BigDecimal("999.00"))

.discountAmount(new BigDecimal("100.00"))

.remark("优先发货")

.receiveAddress("北京市朝阳区XX小区XX号楼")

.payType("ALIPAY")

.build();

System.out.println("完整订单：" + fullOrder);



System.out.println("====================================");



// 3. 构建简易订单（仅必选属性，默认可选属性）

Order simpleOrder = new Order.OrderBuilder("ORDER_20251230_002", "U1002", productIds, new BigDecimal("599.00"))

.build();

System.out.println("简易订单：" + simpleOrder);

}

}

```

#### 3. 运行结果

```

完整订单：Order{orderNo='ORDER_20251230_001', userId='U1001', productIds=[P001, P002, P003], totalAmount=999.00, discountAmount=100.00, remark='优先发货', receiveAddress='北京市朝阳区XX小区XX号楼', payType='ALIPAY'}

====================================

简易订单：Order{orderNo='ORDER_20251230_002', userId='U1002', productIds=[P001, P002, P003], totalAmount=599.00, discountAmount=0, remark='', receiveAddress='', payType='WECHAT'}

```

## 四、建造者模式应用场景总结

1.  **JDK 内置**：处理分步构建的对象（如 `StringBuilder` 构建字符串、`ByteBuffer` 构建字节缓冲区），简化对象创建流程。

2.  **框架开发**：配置复杂的核心对象（如 MyBatis `SqlSessionFactory`、OkHttp `Request`、Spring `ApplicationContext`），提供灵活的链式配置方式。

3.  **业务开发**：

- 包含**大量可选属性**的对象（用户、订单、配置、日志对象等），避免构造方法爆炸；

- 需要**不可变对象**（线程安全），隐藏 setter 方法；

- 复杂对象的**构建流程标准化**（如订单创建、商品打包、消息组装等）。

## 总结

1.  建造者模式在 Java 中应用广泛，分为「JDK 原生」「框架」「业务」三个层级，核心都是**分步构建 + 灵活配置**。

2.  内部静态建造者模式（链式调用）是业务开发和框架设计的首选（如 OkHttp `Request.Builder`、自定义 `Order.Builder`）。

3.  建造者模式的核心价值：解决大量可选属性的对象创建问题、保证对象不可变性、提高代码可读性和可维护性。
> （注：文档部分内容可能由 AI 生成）
# Java 抽象工厂模式：实际案例解析（2023 年 10 月 1 日）

你需要更多Java中抽象工厂模式的实际使用案例，我会分为**经典实战业务案例**（可手动落地）和**Java核心框架/API中的原生应用案例**（真实工业级使用），每个案例都清晰体现抽象工厂的核心特性。

## 一、 经典实战业务案例（手动实现，贴近开发场景）

### 案例1： 数据库访问体系（MySQL/Oracle 产品族）

这是开发中最常用的抽象工厂案例之一，不同数据库（MySQL、Oracle）对应一个产品族，每个产品族包含 `Connection`（数据库连接）、`Statement`（语句执行器）两个核心相关产品。

#### 核心角色对应

|抽象工厂模式角色|本案例具体实现|
|---|---|
|抽象产品1|`DBConnection`（数据库连接抽象接口）|
|抽象产品2|`DBStatement`（语句执行抽象接口）|
|具体产品1（MySQL族）|`MySQLConnection`、`MySQLStatement`|
|具体产品2（Oracle族）|`OracleConnection`、`OracleStatement`|
|抽象工厂|`DBFactory`（数据库工厂抽象接口）|
|具体工厂|`MySQLFactory`、`OracleFactory`|
#### 关键代码片段

```java

// 1. 抽象产品1：数据库连接
public interface DBConnection {
    void connect(); // 建立连接
    String getDBType(); // 获取数据库类型
}

// 2. 抽象产品2：语句执行器
public interface DBStatement {
    void execute(String sql); // 执行SQL
}

// 3. MySQL产品族具体产品
public class MySQLConnection implements DBConnection {
    @Override
    public void connect() {
        System.out.println("建立MySQL数据库连接，使用com.mysql.cj.jdbc.Driver");
    }

    @Override
    public String getDBType() {
        return "MySQL";
    }
}

public class MySQLStatement implements DBStatement {
    @Override
    public void execute(String sql) {
        System.out.println("MySQL执行SQL：" + sql);
    }
}

// 4. Oracle产品族具体产品
public class OracleConnection implements DBConnection {
    @Override
    public void connect() {
        System.out.println("建立Oracle数据库连接，使用oracle.jdbc.driver.OracleDriver");
    }

    @Override
    public String getDBType() {
        return "Oracle";
    }
}

public class OracleStatement implements DBStatement {
    @Override
    public void execute(String sql) {
        System.out.println("Oracle执行SQL：" + sql);
    }
}

// 5. 抽象工厂
public interface DBFactory {
    DBConnection createConnection();
    DBStatement createStatement();
}

// 6. 具体工厂
public class MySQLFactory implements DBFactory {
    @Override
    public DBConnection createConnection() {
        return new MySQLConnection();
    }

    @Override
    public DBStatement createStatement() {
        return new MySQLStatement();
    }
}

public class OracleFactory implements DBFactory {
    @Override
    public DBConnection createConnection() {
        return new OracleConnection();
    }

    @Override
    public DBStatement createStatement() {
        return new OracleStatement();
    }
}

// 7. 客户端使用
public class DBClient {
    public static void main(String[] args) {
        // 切换数据库只需替换具体工厂
        DBFactory factory = new MySQLFactory(); 
        // DBFactory factory = new OracleFactory();
        
        DBConnection connection = factory.createConnection();
        DBStatement statement = factory.createStatement();
        
        connection.connect();
        statement.execute("SELECT * FROM user");
        System.out.println("当前数据库类型：" + connection.getDBType());
    }
}
```

### 案例2： 软件UI皮肤体系（浅色/深色 产品族）

软件的皮肤切换是抽象工厂的典型场景，每个皮肤（浅色、深色）是一个产品族，包含 `Button`（按钮）、`TextField`（输入框）、`Label`（标签）等相关UI组件。

#### 核心角色对应

|抽象工厂模式角色|本案例具体实现|
|---|---|
|抽象产品1|`SkinButton`（皮肤按钮接口）|
|抽象产品2|`SkinTextField`（皮肤输入框接口）|
|抽象产品3|`SkinLabel`（皮肤标签接口）|
|具体产品1（浅色族）|`LightButton`、`LightTextField`、`LightLabel`|
|具体产品2（深色族）|`DarkButton`、`DarkTextField`、`DarkLabel`|
|抽象工厂|`SkinFactory`（皮肤工厂接口）|
|具体工厂|`LightSkinFactory`、`DarkSkinFactory`|
#### 关键代码片段

```java

// 1. 抽象产品：UI组件
public interface SkinButton {
    void renderButton(); // 渲染按钮
}

public interface SkinTextField {
    void renderTextField(); // 渲染输入框
}

public interface SkinLabel {
    void renderLabel(); // 渲染标签
}

// 2. 浅色皮肤产品族
public class LightButton implements SkinButton {
    @Override
    public void renderButton() {
        System.out.println("渲染浅色按钮：白色背景，黑色文字");
    }
}

public class LightTextField implements SkinTextField {
    @Override
    public void renderTextField() {
        System.out.println("渲染浅色输入框：浅灰色背景，黑色文字");
    }
}

public class LightLabel implements SkinLabel {
    @Override
    public void renderLabel() {
        System.out.println("渲染浅色标签：黑色文字，无背景");
    }
}

// 3. 深色皮肤产品族
public class DarkButton implements SkinButton {
    @Override
    public void renderButton() {
        System.out.println("渲染深色按钮：黑色背景，白色文字");
    }
}

public class DarkTextField implements SkinTextField {
    @Override
    public void renderTextField() {
        System.out.println("渲染深色输入框：深灰色背景，白色文字");
    }
}

public class DarkLabel implements SkinLabel {
    @Override
    public void renderLabel() {
        System.out.println("渲染深色标签：白色文字，无背景");
    }
}

// 4. 抽象工厂
public interface SkinFactory {
    SkinButton createButton();
    SkinTextField createTextField();
    SkinLabel createLabel();
}

// 5. 具体工厂
public class LightSkinFactory implements SkinFactory {
    @Override
    public SkinButton createButton() {
        return new LightButton();
    }

    @Override
    public SkinTextField createTextField() {
        return new LightTextField();
    }

    @Override
    public SkinLabel createLabel() {
        return new LightLabel();
    }
}

public class DarkSkinFactory implements SkinFactory {
    @Override
    public SkinButton createButton() {
        return new DarkButton();
    }

    @Override
    public SkinTextField createTextField() {
        return new DarkTextField();
    }

    @Override
    public SkinLabel createLabel() {
        return new DarkLabel();
    }
}

// 6. 客户端使用（切换皮肤只需替换工厂）
public class SkinClient {
    public static void main(String[] args) {
        // 切换皮肤：LightSkinFactory <-> DarkSkinFactory
        SkinFactory skinFactory = new DarkSkinFactory();
        
        SkinButton button = skinFactory.createButton();
        SkinTextField textField = skinFactory.createTextField();
        SkinLabel label = skinFactory.createLabel();
        
        button.renderButton();
        textField.renderTextField();
        label.renderLabel();
    }
}
```

### 案例3： 汽车配件生产体系（宝马/奔驰 产品族）

汽车制造中，每个品牌（宝马、奔驰）是一个产品族，包含 `Engine`（发动机）、`Tire`（轮胎）、`Seat`（座椅）等相关配件。

#### 核心代码片段（关键部分）

```java

// 1. 抽象产品：汽车配件
public interface Engine {
    void startEngine(); // 启动发动机
}

public interface Tire {
    void roll(); // 轮胎滚动
}

public interface Seat {
    void support(); // 座椅支撑
}

// 2. 宝马产品族
public class BMWEngine implements Engine {
    @Override
    public void startEngine() {
        System.out.println("宝马发动机启动：平稳静音，动力强劲");
    }
}

public class BMWTire implements Tire {
    @Override
    public void roll() {
        System.out.println("宝马轮胎滚动：抓地力强，噪音小");
    }
}

public class BMWSeat implements Seat {
    @Override
    public void support() {
        System.out.println("宝马座椅：真皮材质，电动调节");
    }
}

// 3. 奔驰产品族
public class BenzEngine implements Engine {
    @Override
    public void startEngine() {
        System.out.println("奔驰发动机启动：平顺有力，油耗较低");
    }
}

public class BenzTire implements Tire {
    @Override
    public void roll() {
        System.out.println("奔驰轮胎滚动：舒适性高，耐磨性强");
    }
}

public class BenzSeat implements Seat {
    @Override
    public void support() {
        System.out.println("奔驰座椅：航空级包裹，加热通风");
    }
}

// 4. 抽象工厂 + 具体工厂
public interface CarPartFactory {
    Engine createEngine();
    Tire createTire();
    Seat createSeat();
}

public class BMWPartFactory implements CarPartFactory {
    @Override
    public Engine createEngine() {
        return new BMWEngine();
    }

    @Override
    public Tire createTire() {
        return new BMWTire();
    }

    @Override
    public Seat createSeat() {
        return new BMWSeat();
    }
}

public class BenzPartFactory implements CarPartFactory {
    @Override
    public Engine createEngine() {
        return new BenzEngine();
    }

    @Override
    public Tire createTire() {
        return new BenzTire();
    }

    @Override
    public Seat createSeat() {
        return new BenzSeat();
    }
}

// 5. 客户端使用
public class CarClient {
    public static void main(String[] args) {
        CarPartFactory bmwFactory = new BMWPartFactory();
        Engine bmwEngine = bmwFactory.createEngine();
        Tire bmwTire = bmwFactory.createTire();
        Seat bmwSeat = bmwFactory.createSeat();
        
        bmwEngine.startEngine();
        bmwTire.roll();
        bmwSeat.support();
    }
}
```

## 二、 Java核心框架/API 中的原生应用案例（工业级使用）

### 案例1： JDBC 数据库访问框架（抽象工厂思想的经典实现）

JDBC 是Java访问数据库的标准API，其底层大量运用了抽象工厂模式的思想，是工业级场景的典型应用：

- 抽象工厂：`DriverManager`（本质上承担了抽象工厂的职责，用于获取不同数据库的连接）、`Connection`（也可视为工厂，创建`Statement`）

- 抽象产品：`Connection`（数据库连接）、`Statement`（SQL执行器）、`ResultSet`（结果集）

- 具体工厂：不同数据库驱动（MySQL驱动、Oracle驱动）对应的实现类（如`com.mysql.cj.jdbc.ConnectionImpl`对应的工厂逻辑）

- 具体产品：`MySQLConnection`、`OracleStatement`等数据库专属实现类

- 核心特性：客户端只需加载对应数据库驱动，即可通过统一的JDBC抽象接口操作不同数据库，无需关心底层实现，切换数据库只需更换驱动和连接参数。

### 案例2： Spring 框架中的 Bean 工厂体系

Spring 框架的核心是IOC容器，其中 `BeanFactory` 及其实现类大量借鉴了抽象工厂模式的设计思想：

- 抽象工厂：`BeanFactory`（顶级抽象工厂接口，定义了获取Bean的核心方法 `getBean(String name)`）、`ApplicationContext`（继承并扩展了`BeanFactory`）

- 抽象产品：各种Bean的接口（如`UserService`、`OrderDao`等业务接口）

- 具体工厂：`XmlBeanFactory`（基于XML配置的Bean工厂）、`AnnotationConfigApplicationContext`（基于注解配置的Bean工厂）

- 具体产品：Bean的实现类（如`UserServiceImpl`、`OrderDaoImpl`）

- 核心特性：客户端只需通过`ApplicationContext`（抽象工厂）的`getBean`方法获取Bean实例，无需关心Bean的创建、依赖注入、生命周期管理等细节，新增Bean类型或切换Bean实现只需修改配置，无需改动客户端代码。

### 案例3： Java AWT/Swing 图形界面框架

Java 原生的图形界面框架 AWT/Swing 中，不同的“外观风格（Look and Feel）”对应抽象工厂模式的产品族：

- 抽象工厂：`LookAndFeel`（顶级抽象工厂接口，定义了创建各类UI组件的方法）

- 抽象产品：`ButtonUI`、`TextFieldUI`、`LabelUI`等UI组件的抽象外观接口

- 具体工厂：`WindowsLookAndFeel`（Windows系统外观工厂）、`MetalLookAndFeel`（Java默认金属外观工厂）、`MotifLookAndFeel`（Unix系统外观工厂）

- 具体产品：对应外观风格的UI组件实现类（如`WindowsButtonUI`、`MetalTextFieldUI`）

- 核心特性：通过切换`LookAndFeel`工厂，可一次性将整个Swing应用的所有UI组件切换为对应系统的外观风格，保证UI组件的一致性和兼容性。

## 总结

抽象工厂模式在Java中的使用案例可分为两类，核心要点如下：

1. **实战业务案例**（手动落地）：数据库访问体系、UI皮肤切换、汽车配件生产，核心是创建配套产品族，支持快速切换；

2. **框架/API原生案例**（工业级）：JDBC、Spring Bean工厂、Swing Look and Feel，核心是隔离产品创建细节，降低耦合，提高扩展性；

3. 所有案例均体现抽象工厂的核心价值：封装产品族创建、保证产品兼容性、支持灵活切换、隔离实现细节。
> （注：文档部分内容可能由 AI 生成）
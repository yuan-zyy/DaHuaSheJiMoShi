# 里氏替换原则（LSP）更多实战案例

本文补充6个不同业务场景的里氏替换原则（LSP）实战案例，覆盖电商、物流、风控、缓存、文件处理、接口调用等高频场景，每个案例包含「业务背景+反例（违反LSP）+问题分析+修复方案」，帮助全面理解如何识别、规避违反LSP的问题。

## 一、电商场景：商品库存扣减（子类篡改父类核心逻辑）

### 业务背景

父类`Product`定义通用库存扣减逻辑：扣减库存前校验库存充足，扣减后记录日志；子类`LimitedProduct`（限购商品）重写扣减方法，跳过库存校验，导致超卖。

### 反例（违反LSP）

```java

// 父类：通用商品（契约：扣减前校验库存≥扣减数量）
class Product {
    protected int stock; // 库存

    public Product(int stock) {
        this.stock = stock;
    }

    // 核心逻辑：校验库存 → 扣减 → 记录日志
    public boolean deductStock(int num) {
        // 前置校验：库存充足
        if (stock < num || num <= 0) {
            System.out.println("库存不足，扣减失败");
            return false;
        }
        stock -= num;
        recordLog(num);
        System.out.println("库存扣减成功，剩余：" + stock);
        return true;
    }

    protected void recordLog(int num) {
        System.out.println("记录扣减日志：扣减" + num + "件");
    }
}

// 子类：限购商品（违反LSP：跳过库存校验）
class LimitedProduct extends Product {
    public LimitedProduct(int stock) {
        super(stock);
    }

    // 重写时删除库存校验，导致超卖
    @Override
    public boolean deductStock(int num) {
        if (num <= 0) {
            return false;
        }
        stock -= num; // 无校验，库存可扣为负数
        recordLog(num);
        System.out.println("限购商品扣减，剩余：" + stock);
        return true;
    }
}

// 业务代码：替换后超卖
public class StockService {
    public static void deduct(Product product, int num) {
        product.deductStock(num); // 通用商品正常，限购商品超卖
    }

    public static void main(String[] args) {
        // 通用商品：库存5，扣减10 → 失败（符合预期）
        deduct(new Product(5), 10);
        // 限购商品：库存5，扣减10 → 成功（库存-5，违反预期）
        deduct(new LimitedProduct(5), 10);
    }
}
```

### 问题分析

父类的核心契约是“库存不足时禁止扣减”，子类删除校验逻辑，破坏了这一契约；子类替换父类后，出现超卖（库存为负），违反LSP“子类可完全替换父类且行为不变”的核心要求。

### 修复方案

父类用`final`修饰核心流程方法，禁止重写；子类的特殊逻辑通过新增方法实现：

```java

class Product {
    protected int stock;

    public Product(int stock) {
        this.stock = stock;
    }

    // final修饰：核心流程不允许重写
    public final boolean deductStock(int num) {
        if (stock < num || num <= 0) {
            System.out.println("库存不足，扣减失败");
            return false;
        }
        doDeduct(num); // 子类实现具体扣减逻辑
        recordLog(num);
        System.out.println("库存扣减成功，剩余：" + stock);
        return true;
    }

    // 可重写的扩展点：仅处理扣减动作，不修改流程
    protected void doDeduct(int num) {
        stock -= num;
    }

    protected void recordLog(int num) { /* 通用日志 */ }
}

// 子类：仅扩展扣减逻辑，不修改核心流程
class LimitedProduct extends Product {
    public LimitedProduct(int stock) {
        super(stock);
    }

    // 仅扩展扣减动作（如添加限购标记），不跳过校验
    @Override
    protected void doDeduct(int num) {
        super.doDeduct(num);
        System.out.println("限购商品扣减，标记限购记录");
    }
}
```

## 二、物流场景：运费计算（子类缩小返回值范围）

### 业务背景

父类`Shipping`定义通用运费计算：返回≥0的运费（不同地区运费不同）；子类`VIPShipping`（VIP用户运费）重写后，对部分地区返回-1（表示免运费），破坏父类返回值语义。

### 反例（违反LSP）

```java

// 父类：通用运费计算（契约：返回值≥0）
class Shipping {
    // 返回值语义：运费（元），≥0
    public double calculate(String region) {
        if ("偏远地区".equals(region)) {
            return 20.0;
        }
        return 10.0;
    }
}

// 子类：VIP运费计算（违反LSP：返回-1表示免运费）
class VIPShipping extends Shipping {
    @Override
    public double calculate(String region) {
        if ("VIP专区".equals(region)) {
            return -1.0; // 父类无此返回值，破坏语义
        }
        return super.calculate(region);
    }
}

// 业务代码：依赖父类返回值≥0，替换后异常
public class ShippingService {
    public static void showShippingFee(Shipping shipping, String region) {
        double fee = shipping.calculate(region);
        // 父类返回值≥0，此处直接计算折扣
        double discountFee = fee * 0.9; 
        System.out.println("折后运费：" + discountFee);
    }

    public static void main(String[] args) {
        // 通用运费：正常
        showShippingFee(new Shipping(), "偏远地区");
        // VIP运费：折后-0.9（逻辑错误）
        showShippingFee(new VIPShipping(), "VIP专区");
    }
}
```

### 问题分析

父类承诺“返回值≥0”，子类返回-1（超出父类语义范围），导致依赖父类返回值的业务逻辑（折扣计算）出现错误；这是LSP中“子类缩小返回值范围”的典型违规。

### 修复方案

新增“免运费”标识方法，不修改返回值语义：

```java

class Shipping {
    public double calculate(String region) { /* 原有逻辑 */ }
    
    // 父类默认不免运费
    public boolean isFreeShipping(String region) {
        return false;
    }
}

class VIPShipping extends Shipping {
    @Override
    public double calculate(String region) {
        // 免运费时返回0，保持返回值≥0
        if ("VIP专区".equals(region)) {
            return 0.0;
        }
        return super.calculate(region);
    }

    // 新增专属方法：标记免运费
    @Override
    public boolean isFreeShipping(String region) {
        return "VIP专区".equals(region);
    }
}

// 业务代码：先判断免运费，再计算
public class ShippingService {
    public static void showShippingFee(Shipping shipping, String region) {
        if (shipping.isFreeShipping(region)) {
            System.out.println("免运费");
            return;
        }
        double fee = shipping.calculate(region);
        double discountFee = fee * 0.9;
        System.out.println("折后运费：" + discountFee);
    }
}
```

## 三、风控场景：风险评分（子类抛出未声明异常）

### 业务背景

父类`RiskEvaluator`（风险评估）的`score()`方法仅抛`RiskException`；子类`CreditEvaluator`（征信评分）重写后抛`SQLException`（数据库异常），调用方未捕获，导致程序崩溃。

### 反例（违反LSP）

```java

// 自定义异常：风险评估异常
class RiskException extends Exception {
    public RiskException(String msg) {
        super(msg);
    }
}

// 父类：风险评估（仅声明RiskException）
class RiskEvaluator {
    public int score(String userId) throws RiskException {
        // 模拟评分：0-100
        return 80;
    }
}

// 子类：征信评分（违反LSP：抛未声明的SQLException）
class CreditEvaluator extends RiskEvaluator {
    @Override
    public int score(String userId) throws RiskException, SQLException {
        // 从数据库查征信，可能抛SQL异常
        if (userId == null) {
            throw new SQLException("用户ID为空");
        }
        return 90;
    }
}

// 业务代码：仅捕获RiskException，替换后崩溃
public class RiskService {
    public static void evaluate(RiskEvaluator evaluator, String userId) {
        try {
            int score = evaluator.score(userId);
            System.out.println("风险评分：" + score);
        } catch (RiskException e) {
            System.out.println("评估失败：" + e.getMessage());
        }
        // 未捕获SQLException，运行时崩溃
    }
}
```

### 问题分析

子类抛出父类未声明的检查异常（`SQLException`），违反LSP的“异常兼容性”规则；调用方基于父类契约编写代码，无法处理子类新增的异常，导致程序崩溃。

### 修复方案

将子类异常包装为父类异常抛出：

```java

class CreditEvaluator extends RiskEvaluator {
    @Override
    public int score(String userId) throws RiskException {
        try {
            if (userId == null) {
                throw new SQLException("用户ID为空");
            }
            return 90;
        } catch (SQLException e) {
            // 包装为父类声明的异常
            throw new RiskException("征信查询失败：" + e.getMessage());
        }
    }
}
```

## 四、缓存场景：缓存操作（子类新增意外副作用）

### 业务背景

父类`Cache`的`get()`方法仅查询缓存（无写操作）；子类`RefreshCache`（自动刷新缓存）重写`get()`后，新增“缓存过期则更新数据库”的副作用，导致替换后数据库被意外修改。

### 反例（违反LSP）

```java

// 父类：通用缓存（契约：get()仅查询，无写操作）
class Cache {
    public String get(String key) {
        // 模拟查询缓存
        System.out.println("查询缓存：" + key);
        return "缓存值";
    }
}

// 子类：自动刷新缓存（违反LSP：新增写库副作用）
class RefreshCache extends Cache {
    @Override
    public String get(String key) {
        String value = super.get(key);
        // 新增副作用：缓存过期则更新数据库
        if ("expired".equals(value)) {
            updateDb(key, "新值");
        }
        return value;
    }

    private void updateDb(String key, String value) {
        System.out.println("更新数据库：" + key + "=" + value);
    }
}

// 业务代码：仅期望查询缓存，替换后修改数据库
public class CacheService {
    public static void queryCache(Cache cache, String key) {
        cache.get(key); // 通用缓存仅查询，刷新缓存额外写库
    }
}
```

### 问题分析

父类方法的契约是“只读操作”，子类新增“写数据库”的副作用，导致替换后程序出现预期之外的写操作；这是LSP中“子类新增父类无声明的副作用”的典型违规。

### 修复方案

新增方法实现刷新逻辑，保留父类纯查询语义：

```java

class Cache {
    public String get(String key) { /* 纯查询逻辑 */ }
}

class RefreshCache extends Cache {
    // 保留父类纯查询语义
    @Override
    public String get(String key) {
        return super.get(key);
    }

    // 新增方法：明确告知有刷新+写库副作用
    public String getAndRefresh(String key) {
        String value = get(key);
        if ("expired".equals(value)) {
            updateDb(key, "新值");
        }
        return value;
    }

    private void updateDb(String key, String value) { /* 写库逻辑 */ }
}
```

## 五、文件处理场景：文件读取（子类弱化前置条件）

### 业务背景

父类`FileReader`的`read()`方法要求“文件路径必须是绝对路径”（前置条件）；子类`RelativeFileReader`（相对路径读取）重写后，允许相对路径，弱化前置条件，导致依赖父类前置条件的代码异常。

### 反例（违反LSP）

```java

// 父类：文件读取器（前置条件：path必须是绝对路径）
class FileReader {
    public String read(String path) {
        // 校验前置条件：绝对路径
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("必须是绝对路径");
        }
        System.out.println("读取绝对路径文件：" + path);
        return "文件内容";
    }
}

// 子类：相对路径读取器（违反LSP：弱化前置条件）
class RelativeFileReader extends FileReader {
    @Override
    public String read(String path) {
        // 允许相对路径，弱化前置条件
        if (path.startsWith("./")) {
            path = "/user/" + path.substring(2);
        } else if (!path.startsWith("/")) {
            throw new IllegalArgumentException("必须是绝对路径或相对路径");
        }
        System.out.println("读取文件：" + path);
        return "文件内容";
    }
}

// 业务代码：依赖父类“仅绝对路径”的前置条件
public class FileService {
    public static void readFile(FileReader reader, String path) {
        // 调用方基于父类契约，仅传入绝对路径
        reader.read(path);
    }

    public static void main(String[] args) {
        // 父类：传入相对路径 → 抛异常（符合预期）
        try {
            readFile(new FileReader(), "./test.txt");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        // 子类：传入相对路径 → 正常读取（破坏父类契约）
        readFile(new RelativeFileReader(), "./test.txt");
    }
}
```

### 问题分析

LSP要求“子类的前置条件不能比父类更宽松”；父类仅允许绝对路径，子类允许相对路径，导致依赖父类“仅绝对路径”的业务逻辑（如路径权限校验）失效，违反LSP。

### 修复方案

父类抽象化，子类严格遵守前置条件，新增方法支持相对路径：

```java

// 父类：抽象文件读取器，明确前置条件
abstract class AbstractFileReader {
    // 核心方法：仅支持绝对路径
    public final String read(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("必须是绝对路径");
        }
        return doRead(path);
    }

    // 子类实现具体读取逻辑
    protected abstract String doRead(String path);
}

// 子类1：绝对路径读取器
class AbsoluteFileReader extends AbstractFileReader {
    @Override
    protected String doRead(String path) {
        System.out.println("读取绝对路径文件：" + path);
        return "文件内容";
    }
}

// 子类2：相对路径读取器（新增方法支持相对路径）
class RelativeFileReader extends AbstractFileReader {
    @Override
    protected String doRead(String path) {
        System.out.println("读取绝对路径文件：" + path);
        return "文件内容";
    }

    // 新增方法：支持相对路径
    public String readRelative(String path) {
        if (!path.startsWith("./")) {
            throw new IllegalArgumentException("必须是相对路径");
        }
        String absolutePath = "/user/" + path.substring(2);
        return read(absolutePath);
    }
}
```

## 六、接口调用场景：API重试（子类修改父类状态）

### 业务背景

父类`ApiClient`的`retry()`方法语义：重试次数≤3次，且每次重试后计数+1；子类`UnlimitedRetryClient`（无限重试）重写后，重置重试计数，导致重试次数无限制。

### 反例（违反LSP）

```java

// 父类：API客户端（契约：重试次数≤3，计数递增）
class ApiClient {
    protected int retryCount = 0;

    public boolean retry() {
        if (retryCount >= 3) {
            System.out.println("重试次数耗尽");
            return false;
        }
        retryCount++;
        System.out.println("重试第" + retryCount + "次");
        return true;
    }
}

// 子类：无限重试客户端（违反LSP：修改状态规则）
class UnlimitedRetryClient extends ApiClient {
    @Override
    public boolean retry() {
        retryCount = 0; // 重置计数，破坏“递增”规则
        System.out.println("无限重试");
        return true;
    }
}

// 业务代码：依赖父类“重试≤3次”的状态规则
public class ApiService {
    public static void callWithRetry(ApiClient client) {
        int i = 0;
        while (i < 5) {
            if (!client.retry()) {
                break; // 父类重试3次后退出，子类无限循环
            }
            i++;
        }
    }

    public static void main(String[] args) {
        // 父类：重试3次后退出（符合预期）
        callWithRetry(new ApiClient());
        // 子类：无限重试（死循环，违反预期）
        callWithRetry(new UnlimitedRetryClient());
    }
}
```

### 问题分析

父类的状态规则是“重试计数递增，最多3次”，子类静默重置计数，破坏了父类的状态变化逻辑；替换后程序出现死循环，这是LSP中“子类修改父类隐式状态契约”的典型违规。

### 修复方案

用`final`保护状态规则，子类新增无限重试方法：

```java

class ApiClient {
    protected int retryCount = 0;

    // final修饰：禁止修改重试计数规则
    public final boolean retry() {
        if (retryCount >= 3) {
            System.out.println("重试次数耗尽");
            return false;
        }
        retryCount++;
        System.out.println("重试第" + retryCount + "次");
        return doRetry();
    }

    // 子类扩展重试逻辑，不修改计数
    protected boolean doRetry() {
        return true;
    }
}

// 子类：新增无限重试方法，不修改父类状态
class UnlimitedRetryClient extends ApiClient {
    // 新增方法：明确无限重试
    public boolean retryUnlimited() {
        System.out.println("无限重试");
        return doRetry();
    }

    @Override
    protected boolean doRetry() {
        return true;
    }
}
```

## 总结（新增案例核心要点）

1. **核心违规类型**：6个案例覆盖了LSP的核心违规场景——篡改核心逻辑、缩小返回值范围、异常不兼容、新增副作用、弱化前置条件、修改状态规则；

2. **通用修复思路**：
            

    - 父类用`final`保护核心流程/状态方法，避免子类篡改；

    - 子类的特殊逻辑通过「新增方法」实现，而非重写父类方法；

    - 父类明确契约（前置/后置条件、异常、返回值语义），子类严格遵守；

3. **本质原则**：所有案例的修复核心都是“子类只能扩展父类能力，不能修改/收缩父类契约”，确保子类替换父类后，程序行为可预测、无意外。

这些案例覆盖了后端开发的高频场景，可直接参考对应的修复思路，在实际项目中规避LSP违规问题。
> （注：文档部分内容可能由 AI 生成）
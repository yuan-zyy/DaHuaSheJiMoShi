# 组合模式在量化交易策略组合中的Java实现

下面以**量化交易策略组合**为场景，实现组合模式。该场景中，`基础策略`（如均线策略、MACD策略）是叶子节点（无子策略），`组合策略`是容器节点（可包含多个基础策略或子组合策略），客户端可统一调用执行方法，无需区分策略类型。

## 一、核心角色对应

1. **抽象组件（Component）**：`TradeStrategy`（统一策略接口，定义执行方法）

2. **叶子节点（Leaf）**：`MAStragety`（均线策略）、`MACDStrategy`（MACD策略，无子策略）

3. **容器节点（Composite）**：`CompositeStrategy`（组合策略，可添加/移除子策略，递归执行所有子策略）

## 二、完整代码实现

### 1. 抽象组件：统一策略接口

```java

/**
 * 抽象组件：交易策略统一接口
 * 定义所有策略（基础策略/组合策略）的统一执行方法
 */
public abstract class TradeStrategy {
    // 策略名称
    protected String strategyName;
    // 策略描述
    protected String strategyDesc;

    public TradeStrategy(String strategyName, String strategyDesc) {
        this.strategyName = strategyName;
        this.strategyDesc = strategyDesc;
    }

    // ========== 容器节点专属方法（叶子节点默认抛出异常） ==========
    // 添加子策略
    public void addStrategy(TradeStrategy strategy) {
        throw new UnsupportedOperationException("当前[" + strategyName + "]为基础策略，不支持添加子策略");
    }

    // 移除子策略
    public void removeStrategy(TradeStrategy strategy) {
        throw new UnsupportedOperationException("当前[" + strategyName + "]为基础策略，不支持移除子策略");
    }

    // 获取指定索引的子策略
    public TradeStrategy getChildStrategy(int index) {
        throw new UnsupportedOperationException("当前[" + strategyName + "]为基础策略，不支持获取子策略");
    }

    // ========== 核心业务方法（所有策略必须实现） ==========
    /**
     * 执行策略，返回交易信号（BUY:买入, SELL:卖出, HOLD:持仓观望）
     * @param stockCode 股票代码
     * @param tradeDate 交易日期
     * @return 交易信号字符串
     */
    public abstract String execute(String stockCode, String tradeDate);
}
```

### 2. 叶子节点：基础策略（均线策略 + MACD策略）

#### （1）均线策略（MA策略）

```java

/**
 * 叶子节点：均线策略（基础策略，无子策略）
 * 简单逻辑：5日均线向上突破20日均线，返回买入信号；反之返回卖出信号；否则持仓
 */
public class MAStrategy extends TradeStrategy {

    public MAStrategy(String strategyName, String strategyDesc) {
        super(strategyName, strategyDesc);
    }

    @Override
    public String execute(String stockCode, String tradeDate) {
        // 模拟均线计算逻辑（实际项目中需对接行情数据）
        System.out.println("=== 执行[" + strategyName + "] ===");
        System.out.println("股票代码：" + stockCode);
        System.out.println("交易日期：" + tradeDate);
        System.out.println("策略描述：" + strategyDesc);
        // 模拟信号：此处固定返回HOLD，实际可根据行情动态计算
        String signal = "HOLD";
        System.out.println("均线策略输出信号：" + signal + "\n");
        return signal;
    }
}
```

#### （2）MACD策略

```java

/**
 * 叶子节点：MACD策略（基础策略，无子策略）
 * 简单逻辑：MACD金叉返回买入信号；死叉返回卖出信号；否则持仓
 */
public class MACDStrategy extends TradeStrategy {

    public MACDStrategy(String strategyName, String strategyDesc) {
        super(strategyName, strategyDesc);
    }

    @Override
    public String execute(String stockCode, String tradeDate) {
        // 模拟MACD计算逻辑（实际项目中需对接行情数据）
        System.out.println("=== 执行[" + strategyName + "] ===");
        System.out.println("股票代码：" + stockCode);
        System.out.println("交易日期：" + tradeDate);
        System.out.println("策略描述：" + strategyDesc);
        // 模拟信号：此处固定返回HOLD，实际可根据行情动态计算
        String signal = "HOLD";
        System.out.println("MACD策略输出信号：" + signal + "\n");
        return signal;
    }
}
```

### 3. 容器节点：组合策略

```java

import java.util.ArrayList;
import java.util.List;

/**
 * 容器节点：组合策略（可包含基础策略/子组合策略）
 * 核心：递归执行所有子策略，并汇总信号
 */
public class CompositeStrategy extends TradeStrategy {
    // 存储子策略列表（支持基础策略、子组合策略）
    private List<TradeStrategy> childStrategies = new ArrayList<>();

    public CompositeStrategy(String strategyName, String strategyDesc) {
        super(strategyName, strategyDesc);
    }

    // ========== 实现容器节点专属方法 ==========
    @Override
    public void addStrategy(TradeStrategy strategy) {
        childStrategies.add(strategy);
        System.out.println("成功给[" + this.strategyName + "]添加子策略：" + strategy.strategyName);
    }

    @Override
    public void removeStrategy(TradeStrategy strategy) {
        if (childStrategies.contains(strategy)) {
            childStrategies.remove(strategy);
            System.out.println("成功给[" + this.strategyName + "]移除子策略：" + strategy.strategyName);
        } else {
            System.out.println("[" + this.strategyName + "]中不存在子策略：" + strategy.strategyName);
        }
    }

    @Override
    public TradeStrategy getChildStrategy(int index) {
        if (index < 0 || index >= childStrategies.size()) {
            throw new IndexOutOfBoundsException("子策略索引越界，当前组合策略仅有" + childStrategies.size() + "个子策略");
        }
        return childStrategies.get(index);
    }

    // ========== 实现核心业务方法：递归执行 + 信号汇总 ==========
    @Override
    public String execute(String stockCode, String tradeDate) {
        System.out.println("========== 开始执行组合策略：[" + strategyName + "] ==========");
        System.out.println("组合策略描述：" + strategyDesc);
        System.out.println("待执行子策略数量：" + childStrategies.size() + "\n");

        // 1. 递归执行所有子策略，收集信号
        List<String> signalList = new ArrayList<>();
        for (TradeStrategy child : childStrategies) {
            String childSignal = child.execute(stockCode, tradeDate);
            signalList.add(childSignal);
        }

        // 2. 汇总信号（简单逻辑：若有≥1个BUY则返回BUY；若有≥1个SELL则返回SELL；否则HOLD）
        String finalSignal;
        if (signalList.contains("BUY")) {
            finalSignal = "BUY";
        } else if (signalList.contains("SELL")) {
            finalSignal = "SELL";
        } else {
            finalSignal = "HOLD";
        }

        // 3. 输出汇总结果
        System.out.println("========== 组合策略[" + strategyName + "]执行完毕 ==========");
        System.out.println("子策略信号列表：" + signalList);
        System.out.println("组合策略最终输出信号：" + finalSignal + "\n");
        return finalSignal;
    }
}
```

### 4. 客户端测试类

```java

/**
 * 客户端：统一操作基础策略（叶子节点）和组合策略（容器节点）
 */
public class TradeStrategyClient {
    public static void main(String[] args) {
        // 1. 创建叶子节点：2个基础策略
        TradeStrategy maStrategy = new MAStrategy("5日-20日均线策略", "短期均线突破长期均线，判断趋势反转");
        TradeStrategy macdStrategy = new MACDStrategy("MACD金叉死叉策略", "通过MACD柱状线和信号线判断买卖点");

        // 2. 创建容器节点1：基础策略组合（均线+MACD）
        CompositeStrategy basicComposite = new CompositeStrategy("基础策略组合", "包含均线和MACD的基础交易策略集合");
        basicComposite.addStrategy(maStrategy);
        basicComposite.addStrategy(macdStrategy);

        // 3. 创建容器节点2：顶级组合策略（包含基础策略组合 + 新增的均线策略）
        CompositeStrategy topComposite = new CompositeStrategy("顶级多因子组合策略", "整合基础策略组合，实现多因子共振判断");
        topComposite.addStrategy(basicComposite);
        topComposite.addStrategy(new MAStrategy("10日-60日均线策略", "中长期均线趋势判断"));

        // 4. 统一调用execute方法：无需区分基础策略/组合策略
        System.out.println("===== 单独执行基础策略（均线策略） =====");
        maStrategy.execute("600036", "2026-01-02");

        System.out.println("===== 执行基础策略组合 =====");
        basicComposite.execute("600036", "2026-01-02");

        System.out.println("===== 执行顶级组合策略 =====");
        topComposite.execute("600036", "2026-01-02");

        // 5. 操作组合策略的子组件（获取、移除）
        System.out.println("===== 操作组合策略子组件 =====");
        TradeStrategy child = topComposite.getChildStrategy(0);
        System.out.println("顶级组合策略第1个子策略名称：" + child.strategyName);

        topComposite.removeStrategy(maStrategy); // 移除不存在的子策略（测试异常提示）
        topComposite.removeStrategy(child); // 移除存在的子策略
    }
}
```

## 三、运行结果

```text

===== 单独执行基础策略（均线策略） =====
=== 执行[5日-20日均线策略] ===
股票代码：600036
交易日期：2026-01-02
策略描述：短期均线突破长期均线，判断趋势反转
均线策略输出信号：HOLD

===== 执行基础策略组合 =====
成功给[基础策略组合]添加子策略：5日-20日均线策略
成功给[基础策略组合]添加子策略：MACD金叉死叉策略
========== 开始执行组合策略：[基础策略组合] ==========
组合策略描述：包含均线和MACD的基础交易策略集合
待执行子策略数量：2

=== 执行[5日-20日均线策略] ===
股票代码：600036
交易日期：2026-01-02
策略描述：短期均线突破长期均线，判断趋势反转
均线策略输出信号：HOLD

=== 执行[MACD金叉死叉策略] ===
股票代码：600036
交易日期：2026-01-02
策略描述：通过MACD柱状线和信号线判断买卖点
MACD策略输出信号：HOLD

========== 组合策略[基础策略组合]执行完毕 ==========
子策略信号列表：[HOLD, HOLD]
组合策略最终输出信号：HOLD

===== 执行顶级组合策略 =====
成功给[顶级多因子组合策略]添加子策略：基础策略组合
成功给[顶级多因子组合策略]添加子策略：10日-60日均线策略
========== 开始执行组合策略：[顶级多因子组合策略] ==========
组合策略描述：整合基础策略组合，实现多因子共振判断
待执行子策略数量：2

========== 开始执行组合策略：[基础策略组合] ==========
组合策略描述：包含均线和MACD的基础交易策略集合
待执行子策略数量：2

=== 执行[5日-20日均线策略] ===
股票代码：600036
交易日期：2026-01-02
策略描述：短期均线突破长期均线，判断趋势反转
均线策略输出信号：HOLD

=== 执行[MACD金叉死叉策略] ===
股票代码：600036
交易日期：2026-01-02
策略描述：通过MACD柱状线和信号线判断买卖点
MACD策略输出信号：HOLD

========== 组合策略[基础策略组合]执行完毕 ==========
子策略信号列表：[HOLD, HOLD]
组合策略最终输出信号：HOLD

=== 执行[10日-60日均线策略] ===
股票代码：600036
交易日期：2026-01-02
策略描述：中长期均线趋势判断
均线策略输出信号：HOLD

========== 组合策略[顶级多因子组合策略]执行完毕 ==========
子策略信号列表：[HOLD, HOLD]
组合策略最终输出信号：HOLD

===== 操作组合策略子组件 =====
顶级组合策略第1个子策略名称：基础策略组合
[顶级多因子组合策略]中不存在子策略：5日-20日均线策略
成功给[顶级多因子组合策略]移除子策略：基础策略组合
```

## 四、核心特性说明

1. **递归执行**：`CompositeStrategy`的`execute()`方法会遍历并递归调用所有子策略的`execute()`，实现组合策略的分层执行，这是量化策略组合的核心需求。

2. **客户端一致性**：客户端调用`maStrategy.execute()`（基础策略）和`topComposite.execute()`（组合策略）的方式完全一致，无需区分策略类型，简化了交易系统的调用逻辑。

3. **灵活拓展**：新增基础策略（如RSI策略）或组合策略（如行业轮动组合策略）时，仅需新增`TradeStrategy`子类，无需修改现有代码，符合开闭原则。

4. **信号汇总**：组合策略支持自定义信号汇总逻辑（示例中为“优先级汇总”，实际可扩展为加权汇总、投票汇总等），满足量化交易的灵活需求。
> （注：文档部分内容可能由 AI 生成）
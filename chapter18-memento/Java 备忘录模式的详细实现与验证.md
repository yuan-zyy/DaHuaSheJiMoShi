# Java 备忘录模式的详细实现与验证

你想知道Java中备忘录模式的具体实现方式，我会通过**核心角色拆解、分步代码实现、完整验证示例**来详细说明，确保你能落地使用。

## 一、先明确备忘录模式的3个核心角色

Java实现备忘录模式的核心是围绕3个分工明确的角色展开，这是实现的基础：

1. **原发器（Originator）**：需要保存/恢复状态的目标对象（比如游戏角色、用户信息），负责创建备忘录（存档）和通过备忘录恢复自身状态（读档）。

2. **备忘录（Memento）**：专门存储原发器的内部状态，相当于“存档文件”，仅暴露读取状态的getter方法（不提供setter，保证状态不可篡改），且仅对原发器开放访问权限。

3. **管理者（Caretaker）**：仅负责保管备忘录对象，**不允许访问或修改备忘录的内部状态**，相当于“存档仓库”，可存储多个备忘录支持多版本恢复。

## 二、分步实现Java备忘录模式

下面以“游戏角色血量/蓝量存档与恢复”为例，分步完成可运行的实现。

### 步骤1：实现原发器（Originator）- GameRole

负责业务逻辑、状态创建（存档）和状态恢复（读档），持有自身的核心状态。

```java

/**
 * 原发器：游戏角色（需要保存/恢复状态的对象）
 */
public class GameRole {
    // 原发器的内部状态（需要存档的核心数据）
    private String roleName;
    private int bloodVolume; // 血量
    private int manaVolume;   // 蓝量

    // 构造方法：初始化角色状态
    public GameRole(String roleName, int bloodVolume, int manaVolume) {
        this.roleName = roleName;
        this.bloodVolume = bloodVolume;
        this.manaVolume = manaVolume;
    }

    // 核心方法1：创建备忘录，保存当前角色的所有内部状态
    public GameRoleMemento createMemento() {
        // 将当前对象的状态传入备忘录，完成存档
        return new GameRoleMemento(this.roleName, this.bloodVolume, this.manaVolume);
    }

    // 核心方法2：从备忘录中恢复角色之前的状态
    public void restoreFromMemento(GameRoleMemento memento) {
        // 从备忘录中读取存档状态，赋值给当前角色对象
        this.roleName = memento.getRoleName();
        this.bloodVolume = memento.getBloodVolume();
        this.manaVolume = memento.getManaVolume();
    }

    // 业务方法：角色战斗（状态变化，用于演示存档的必要性）
    public void fight() {
        System.out.println(roleName + " 进入战斗！");
        this.bloodVolume -= 30;
        this.manaVolume -= 20;
        if (this.bloodVolume < 0) this.bloodVolume = 0;
        if (this.manaVolume < 0) this.manaVolume = 0;
        System.out.println(roleName + " 战斗后状态：血量=" + bloodVolume + "，蓝量=" + manaVolume);
    }

    // 打印角色当前状态（用于验证存档/恢复效果）
    @Override
    public String toString() {
        return "游戏角色：{" +
                "角色名='" + roleName + '\'' +
                ", 血量=" + bloodVolume +
                ", 蓝量=" + manaVolume +
                '}';
    }

    // getter方法（仅供备忘录读取，外部无需直接访问）
    public String getRoleName() {
        return roleName;
    }

    public int getBloodVolume() {
        return bloodVolume;
    }

    public int getManaVolume() {
        return manaVolume;
    }
}
```

### 步骤2：实现备忘录（Memento）- GameRoleMemento

专门存储原发器的状态，是“只读”对象（无setter），保证状态的完整性和不可篡改。

```java

/**
 * 备忘录：游戏角色存档对象
 * 职责：仅存储原发器状态，不包含业务逻辑，仅暴露getter方法
 */
public class GameRoleMemento {
    // 与原发器一一对应的状态字段（完整存储原发器需要存档的信息）
    private String roleName;
    private int bloodVolume;
    private int manaVolume;

    // 构造方法：仅允许原发器调用，封装状态赋值（保证状态初始化的安全性）
    public GameRoleMemento(String roleName, int bloodVolume, int manaVolume) {
        this.roleName = roleName;
        this.bloodVolume = bloodVolume;
        this.manaVolume = manaVolume;
    }

    // 仅提供getter方法，不提供setter方法，保证备忘录状态不可修改
    public String getRoleName() {
        return roleName;
    }

    public int getBloodVolume() {
        return bloodVolume;
    }

    public int getManaVolume() {
        return manaVolume;
    }
}
```

### 步骤3：实现管理者（Caretaker）- GameRoleCaretaker

仅负责保管备忘录，不访问其内部状态，支持多备忘录存储（多存档位）。

```java

import java.util.ArrayList;
import java.util.List;

/**
 * 管理者：游戏角色备忘录保管者
 * 职责：仅保存和获取备忘录，不操作备忘录的内部状态
 */
public class GameRoleCaretaker {
    // 存储多个备忘录（支持多存档，比如角色的多个历史状态）
    private List<GameRoleMemento> mementoList = new ArrayList<>();

    // 保存备忘录（存档）
    public void saveMemento(GameRoleMemento memento) {
        mementoList.add(memento);
        System.out.println("已保存角色存档，当前存档数量：" + mementoList.size());
    }

    // 获取指定索引的备忘录（用于恢复指定存档）
    public GameRoleMemento getMemento(int index) {
        // 索引合法性校验
        if (index < 0 || index >= mementoList.size()) {
            System.out.println("存档索引无效！");
            return null;
        }
        return mementoList.get(index);
    }
}
```

### 步骤4：测试类（验证备忘录模式功能）

通过完整的业务流程，验证“存档-修改状态-恢复状态”的效果。

```java

/**
 * 测试类：验证Java备忘录模式的完整功能
 */
public class MementoPatternTest {
    public static void main(String[] args) {
        // 1. 创建原发器：初始化游戏角色（初始状态）
        GameRole hero = new GameRole("剑侠客", 100, 80);
        System.out.println("========== 初始状态 ==========");
        System.out.println(hero);

        // 2. 创建管理者：用于保管存档
        GameRoleCaretaker caretaker = new GameRoleCaretaker();

        // 3. 保存初始状态（第一次存档，索引0）
        System.out.println("\n========== 保存初始存档 ==========");
        caretaker.saveMemento(hero.createMemento());

        // 4. 角色战斗（状态变化）
        System.out.println("\n========== 第一次战斗 ==========");
        hero.fight();

        // 5. 保存战斗后的状态（第二次存档，索引1）
        System.out.println("\n========== 保存战斗后存档 ==========");
        caretaker.saveMemento(hero.createMemento());

        // 6. 角色再次战斗（状态进一步变化）
        System.out.println("\n========== 第二次战斗 ==========");
        hero.fight();
        System.out.println("\n========== 战斗后最终状态 ==========");
        System.out.println(hero);

        // 7. 恢复到初始状态（读取第一个存档，索引0）
        System.out.println("\n========== 恢复到初始状态 ==========");
        GameRoleMemento initialMemento = caretaker.getMemento(0);
        if (initialMemento != null) {
            hero.restoreFromMemento(initialMemento);
            System.out.println(hero);
        }

        // 8. 恢复到第一次战斗后的状态（读取第二个存档，索引1）
        System.out.println("\n========== 恢复到第一次战斗后状态 ==========");
        GameRoleMemento fightMemento = caretaker.getMemento(1);
        if (fightMemento != null) {
            hero.restoreFromMemento(fightMemento);
            System.out.println(hero);
        }
    }
}
```

## 三、运行结果（验证功能有效性）

```text

========== 初始状态 ==========
游戏角色：{角色名='剑侠客', 血量=100, 蓝量=80}

========== 保存初始存档 ==========
已保存角色存档，当前存档数量：1

========== 第一次战斗 ==========
剑侠客 进入战斗！
剑侠客 战斗后状态：血量=70，蓝量=60

========== 保存战斗后存档 ==========
已保存角色存档，当前存档数量：2

========== 第二次战斗 ==========
剑侠客 进入战斗！
剑侠客 战斗后状态：血量=40，蓝量=40

========== 战斗后最终状态 ==========
游戏角色：{角色名='剑侠客', 血量=40, 蓝量=40}

========== 恢复到初始状态 ==========
游戏角色：{角色名='剑侠客', 血量=100, 蓝量=80}

========== 恢复到第一次战斗后状态 ==========
游戏角色：{角色名='剑侠客', 血量=70, 蓝量=60}
```

## 四、关键实现要点总结

1. 备忘录**无setter方法**：仅通过构造方法初始化状态，保证存档后的状态不可被篡改。

2. 管理者**不访问备忘录内部状态**：仅做“增/查”备忘录的操作，符合单一职责原则，同时保证封装性。

3. 原发器**自主控制状态**：存档（`createMemento`）和恢复（`restoreFromMemento`）由原发器自身完成，不暴露内部状态给外部。

4. 支持多存档：管理者通过`List`存储多个备忘录，可实现恢复到任意历史状态（如办公软件的多次撤销）。

### 总结

Java实现备忘录模式的核心步骤：

1. 定义**原发器**：封装业务状态，提供创建备忘录和恢复状态的方法。

2. 定义**备忘录**：与原发器状态一一对应，仅提供getter方法，保证状态只读。

3. 定义**管理者**：保管备忘录，提供备忘录的存取接口，不操作其内部状态。

4. 测试验证：完成“存档-修改状态-恢复状态”的完整流程，验证功能有效性。
> （注：文档部分内容可能由 AI 生成）
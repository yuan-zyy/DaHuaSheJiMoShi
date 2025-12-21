package com.zyy.design.pattern.dhsjms.chapter02.demo01;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * 大话设计模式 - 多商品收益计算系统
 * 支持多商品明细、累计总价、下拉框单选折扣计算
 */
public class CashierSystem extends Application {

    // 基础输入组件
    private TextField priceField;
    private TextField quantityField;
    // 明细和总价展示组件
    private TextArea detailTextArea;
    private Label totalPriceLabel;
    // 折扣下拉框
    private ComboBox<String> discountComboBox;
    // 累计总价（全局变量）
    private double totalProfit = 0.0;
    // 商品序号
    private int productIndex = 1;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("多商品收益计算系统");

        // 网格布局
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        // 1. 单价输入区域
        Label priceLabel = new Label("单价（元）：");
        GridPane.setConstraints(priceLabel, 0, 0);
        priceField = new TextField();
        priceField.setPromptText("请输入商品单价（正数）");
        GridPane.setConstraints(priceField, 1, 0);

        // 2. 数量输入区域
        Label quantityLabel = new Label("数量（件）：");
        GridPane.setConstraints(quantityLabel, 0, 1);
        quantityField = new TextField();
        quantityField.setPromptText("请输入商品数量（正整数）");
        GridPane.setConstraints(quantityField, 1, 1);

        // 3. 折扣下拉框区域
        Label discountLabel = new Label("选择折扣：");
        GridPane.setConstraints(discountLabel, 0, 2);
        // 初始化折扣下拉框
        discountComboBox = new ComboBox<>();
        // 添加折扣选项（值与展示文本一致，也可自定义键值对）
        discountComboBox.getItems().addAll(
                "正常收费",
                "9折",
                "8折",
                "满100减10",
                "满200减30"
        );
        // 设置默认选中项
        discountComboBox.setValue("正常收费");
        // 设置下拉框宽度，适配内容
        discountComboBox.setPrefWidth(150);
        GridPane.setConstraints(discountComboBox, 1, 2);

        // 4. 功能按钮
        Button confirmBtn = new Button("确定");
        GridPane.setConstraints(confirmBtn, 0, 3);
        confirmBtn.setOnAction(e -> addProductDetail());

        Button resetBtn = new Button("重置");
        GridPane.setConstraints(resetBtn, 1, 3);
        resetBtn.setOnAction(e -> resetAll());

        // 5. 商品明细区域（只读）
        Label detailLabel = new Label("商品明细：");
        GridPane.setConstraints(detailLabel, 0, 4);
        detailTextArea = new TextArea();
        detailTextArea.setPrefRowCount(8);
        detailTextArea.setPrefColumnCount(40);
        detailTextArea.setEditable(false);
        detailTextArea.setPromptText("添加商品后，明细将显示在这里（包含折扣计算）...");
        GridPane.setConstraints(detailTextArea, 0, 5, 2, 1); // 跨2列

        // 6. 累计总价区域
        Label totalLabel = new Label("累计总价：");
        GridPane.setConstraints(totalLabel, 0, 6);
        totalPriceLabel = new Label("0.00 元");
        totalPriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2e8b57;");
        GridPane.setConstraints(totalPriceLabel, 1, 6);

        // 添加所有组件到布局
        grid.getChildren().addAll(
                priceLabel, priceField, quantityLabel, quantityField,
                discountLabel, discountComboBox, confirmBtn, resetBtn,
                detailLabel, detailTextArea, totalLabel, totalPriceLabel
        );

        // 场景和窗口设置
        Scene scene = new Scene(grid, 500, 450);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * 添加商品明细（含下拉框折扣计算）
     */
    private void addProductDetail() {
        try {
            // 1. 获取并校验基础输入
            double price = Double.parseDouble(priceField.getText().trim());
            int num = Integer.parseInt(quantityField.getText().trim());

            // 2. 计算原始小计（无折扣）
            double originalSubtotal = price * num;
            // 3. 获取下拉框选中的折扣，计算最终小计
            String selectedDiscount = discountComboBox.getValue();
            double finalSubtotal = switch (selectedDiscount) {
                case "9折" -> originalSubtotal * 0.9;
                case "8折" -> originalSubtotal * 0.8;
                case "5折" -> originalSubtotal * 0.5;
                default -> originalSubtotal;
            };

            // 4. 拼接明细（包含折扣计算过程）
            String detail = String.format("✅ 商品%d: ", productIndex++) +
                    String.format("   单价: %.2f 数量: %d  费用: %.2f元", price, num, originalSubtotal) +
                    String.format("   %s", selectedDiscount) +
                    String.format("   应收：%.2f元\n", finalSubtotal);

            // 5. 更新明细和总价
            detailTextArea.appendText(detail);
            totalProfit += finalSubtotal;
            totalPriceLabel.setText(String.format("%.2f 元", totalProfit));

            // 6. 清空输入框（保留折扣选中状态，方便连续添加同折扣商品）
            priceField.clear();
            quantityField.clear();
            priceField.requestFocus();

        } catch (NumberFormatException e) {
            updateDetailWithError("❌ 输入错误！请输入有效的数字（单价支持小数，数量为整数）");
        }
    }

    /**
     * 重置所有内容（输入框、明细、总价、折扣下拉框）
     */
    private void resetAll() {
        // 清空输入框
        priceField.clear();
        quantityField.clear();
        // 清空明细文本域
        detailTextArea.clear();
        detailTextArea.setPromptText("添加商品后，明细将显示在这里（包含折扣计算）...");
        // 重置总价和商品序号
        totalProfit = 0.0;
        productIndex = 1;
        // 重置总价标签
        totalPriceLabel.setText("0.00 元");
        // 重置折扣下拉框为默认值
        discountComboBox.setValue("无折扣");
        // 焦点回到单价输入框
        priceField.requestFocus();
    }

    /**
     * 辅助方法：更新明细区域显示错误提示
     */
    private void updateDetailWithError(String errorMsg) {
        detailTextArea.appendText(errorMsg + "\n");
        // 清空输入框，方便重新输入
        priceField.clear();
        quantityField.clear();
        priceField.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
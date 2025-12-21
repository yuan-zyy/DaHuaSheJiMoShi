package com.zyy.design.pattern.demo.javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// 所有JavaFX应用都需继承Application类
public class HelloJavaFX extends Application {

    // 核心方法：启动应用，参数primaryStage是主窗口
    @Override
    public void start(Stage primaryStage) {

        primaryStage.show();
    }

    // 程序入口
    public static void main(String[] args) {
        // 启动JavaFX应用（底层会调用start方法）
        launch(args);
    }
}

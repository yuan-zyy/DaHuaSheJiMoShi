package com.zyy.design.pattern.dhsjms.chapter07.demojdk;

import com.zyy.design.pattern.dhsjms.chapter07.demojdk.user.UserService;
import com.zyy.design.pattern.dhsjms.chapter07.demojdk.user.UserServiceImpl;
import com.zyy.design.pattern.dhsjms.chapter07.demojdk.user.UserServiceInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Demo01 {

    public static void main(String[] args) {
        // 1. 创建目标对象（被代理对象）
        UserService userService = new UserServiceImpl();

        // 2. 创建自定义 InvocationHandler 实例，传入目标对象
        InvocationHandler handler = new UserServiceInvocationHandler(userService);

        // 3. 适用Proxy.newProxyInstance() 生成代理对象
        UserService userServiceProxy = (UserService) Proxy.newProxyInstance(
                userService.getClass().getClassLoader(),
                userService.getClass().getInterfaces(),
                handler);

        // 4. 调用代理对象的方法（实际会触发 InvocationHandler 的 invoke 方法）
        userServiceProxy.addUser("张三");
        userServiceProxy.deleteUser("李四");

    }

}

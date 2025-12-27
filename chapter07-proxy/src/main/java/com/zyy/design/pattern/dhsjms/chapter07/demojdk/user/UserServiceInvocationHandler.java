package com.zyy.design.pattern.dhsjms.chapter07.demojdk.user;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UserServiceInvocationHandler implements InvocationHandler {
    // 维护目标对象（被代理对象），构造方法注入
    private Object target;

    public UserServiceInvocationHandler(Object target) {
        this.target = target;
    }

    /**
     * 代理对象调用任意方法，都会触发该方法
     * @param proxy 动态生成的代理对象（一般很少用）
     * @param method 被调用的目标方法（发射对象）
     * @param args 目标方法的入参
     * @return 目标方法的返回值
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("前置日志：调用方法 Before");
        Object result = method.invoke(target, args);
        System.out.println("后置日志：调用方法 After");
        return result;
    }
}

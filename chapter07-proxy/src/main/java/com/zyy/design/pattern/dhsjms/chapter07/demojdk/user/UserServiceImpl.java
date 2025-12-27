package com.zyy.design.pattern.dhsjms.chapter07.demojdk.user;

public class UserServiceImpl implements UserService {
    @Override
    public void addUser(String username) {
        System.out.println("新增用户：" + username);
    }

    @Override
    public void deleteUser(String username) {
        System.out.println("删除用户：" + username);
    }
}

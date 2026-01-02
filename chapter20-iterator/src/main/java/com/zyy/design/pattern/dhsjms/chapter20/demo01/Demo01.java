package com.zyy.design.pattern.dhsjms.chapter20.demo01;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Demo01 {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");

//        Iterator<String> iterator = list.iterator();
        list.stream().filter(null);
    }

}

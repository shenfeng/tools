package me.shenfeng.proxy;

import java.util.ArrayList;

/**
 * Created by feng on 1/5/15.
 */
public class Crawler {
    public static void main(String[] args) {

        ArrayList<String> seeds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            seeds.add(String.format("http://www.proxy.com.ru/list_%d.html", i + 1));
        }
    }
}




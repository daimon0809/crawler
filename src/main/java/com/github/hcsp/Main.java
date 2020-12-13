package com.github.hcsp;

public class Main {
    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDaoImpl();

        for (int i = 0; i < 4; i++) {
            new Crawler(dao).start();
        }
    }
}

package com.yanyushkin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Singleton {
    private static Singleton instance;
    private String connectionUrl = "jdbc:postgresql://localhost:5432/DatabaseOfCars";
    private Connection connObj = null;

    private Singleton(){
        try {
            connObj = DriverManager.getConnection(connectionUrl, "postgres", "123");//установка соединения с БД
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    public Connection getConnObj() {//получение соединения с базой данных
        return connObj;
    }
}

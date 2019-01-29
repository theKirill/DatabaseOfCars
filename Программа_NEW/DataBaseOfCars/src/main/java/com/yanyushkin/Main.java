package com.yanyushkin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    public static Singleton session;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainForm.fxml"));
        //root.getStylesheets().add("G:/3 курс/Java/Электронный журнал/src/sample/css/styles.sss");
        primaryStage.setTitle("База данных продаж автомобилей");
        Scene scene = new Scene(root);//создание сцены с заданным fxml
        scene.getStylesheets().add("/css/styles.css");//установка стилей
        //scene.getStylesheets().addAll(this.getClass().getResource("../css/styles.css").toExternalForm());
        primaryStage.setScene(scene);//устанавливаем главному окну сцену
        //primaryStage.setMaximized(true);
        session= Singleton.getInstance();//создаем объект Singleton (соединяемся с БД)
        primaryStage.setResizable(false);
        primaryStage.show();

       /* AutoService autoService=new AutoService();
        Auto auto=new Auto("Daewoo");
        autoService.saveAuto(auto);*/
    }
}
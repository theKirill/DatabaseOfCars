package com.yanyushkin.contoller;
import com.jfoenix.controls.JFXTextField;
import com.yanyushkin.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Auto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class MainFormController {
    @FXML
    private TableView table;
    @FXML
    private TableColumn<Auto, Integer> idColumn;
    @FXML
    private TableColumn<Auto, String> brandAndModelColumn;
    @FXML
    private TableColumn<Auto, String> cityColumn;
    @FXML
    private ImageView imageView;
    @FXML
    private JFXTextField fullnameauto;
    @FXML
    private JFXTextField city;
    @FXML
    private JFXTextField year;
    @FXML
    private JFXTextField mileage;
    @FXML
    private JFXTextField engine;
    @FXML
    private JFXTextField bodywork;
    @FXML
    private JFXTextField transmission;
    @FXML
    private JFXTextField drive;
    @FXML
    private JFXTextField condition;
    @FXML
    private JFXTextField price;
    @FXML
    private JFXTextField url;

    public void loadOnAction(ActionEvent actionEvent) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //назначение значений строкам таблицы из соответствующих полей класса Auto
        idColumn.setCellValueFactory(new PropertyValueFactory<Auto, Integer>("id"));
        brandAndModelColumn.setCellValueFactory(new PropertyValueFactory<Auto, String>("fullNameAuto"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<Auto, String>("city"));

        ObservableList<Auto> listOfCars = FXCollections.observableArrayList();//список автомобилей (для таблицы)

        Statement stmt = null;

        try {
            stmt = Main.session.getConnObj().createStatement();//получение соединения с базой данных
            ResultSet executeQueryCars = stmt.executeQuery("SELECT * FROM autos");//отправка запроса на выборку (выбор всех автомобилей из БД) и получение результатов
            table.getItems().clear();//очистка таблицы

            while (executeQueryCars.next()) {//обход результатов выборки
                Auto auto = new Auto();
                //получение нужных полей для объекта Auto из БД
                auto.setId(executeQueryCars.getInt("id"));
                auto.setFullNameAuto(executeQueryCars.getString("fullNameAuto"));
                auto.setCity(executeQueryCars.getString("city"));
                //добавление текущего автомобиля в список автомобилей
                listOfCars.add(auto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //присваивание значений таблице
        table.setItems(listOfCars);

        TableView.TableViewSelectionModel<Auto> selectionModel = table.getSelectionModel();//получение модели выбора строки таблицы

        Statement finalStmt = stmt;
        selectionModel.selectedItemProperty().addListener(new ChangeListener<Auto>() {//слушатель выбора строки таблицы
            @Override
            public void changed(ObservableValue<? extends Auto> observable, Auto oldValue, Auto newValue) {
                try {
                    //по нажатой строке определяется id автомобиля, который используется для запроса поиска этого автомобиля
                    ResultSet executeQueryCar = finalStmt.executeQuery("SELECT * FROM autos WHERE autos.id="+selectionModel.getSelectedItem().getId());//выборка автомобиля с заданным id

                    while (executeQueryCar.next()) {
                        //получение нужных атрибутов автомобиля из БД и установка значений атрибутов в textfield-ы на форме
                        fullnameauto.setText(executeQueryCar.getString("fullNameAuto"));
                        city.setText(executeQueryCar.getString("city"));
                        year.setText(Integer.toString(executeQueryCar.getInt("yearOfRelease")));
                        mileage.setText(executeQueryCar.getString("mileage"));
                        engine.setText(executeQueryCar.getString("engine"));
                        bodywork.setText(executeQueryCar.getString("bodywork"));
                        transmission.setText(executeQueryCar.getString("transmission"));
                        drive.setText(executeQueryCar.getString("drive"));
                        condition.setText(executeQueryCar.getString("condition"));
                        price.setText(executeQueryCar.getString("price"));
                        url.setText(executeQueryCar.getString("url"));
                        imageView.setImage(new Image(executeQueryCar.getString("photo")));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateButtonOnAction(ActionEvent actionEvent) {
        Statement stmt = null;
        String url = "";

        try {
            stmt = Main.session.getConnObj().createStatement();//подключение к БД
            stmt.executeUpdate("DELETE FROM autos");//запрос - очистка БД от всех записей
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            for (int numOfPage = 1; numOfPage <= 3; numOfPage++) {//проход по 3м страницам сайта
                if (numOfPage == 1)
                    url = "https://rolf-probeg.ru/commercial/";
                else
                    url = "https://rolf-probeg.ru/commercial/page/"+numOfPage+"/";

                Document doc = Jsoup.connect("https://rolf-probeg.ru/commercial/").maxBodySize(0).timeout(0).get();//получение документа для разбора
                Elements elems = doc.body().getElementsByTag("a");//поиск элементов по тегу "а"
                ArrayList<String> cars = new ArrayList<>();//список ссылок на каждую машину

                for (Element element : elems)
                    if (element.attr("href").length() > 34 && element.attr("href").substring(0, 34).equals("https://rolf-probeg.ru/commercial/") && countOfSymbol(element.attr("href")) == 7)//выбор нужных ссылок
                        cars.add(element.attr("href"));//добавление ссылки на машину в список ссыллок

                for (String ref : cars) {//проход по каждой ссылке
                    Document doc2 = Jsoup.connect(ref).maxBodySize(0).timeout(0).get();//получение документа страницы автомобиля для разбора
                    Elements elems2 = doc2.body().getElementsByAttributeValue("class", "info-table__data");//получение элементов с нужным атрибутом

                    //извлечение нужных данных
                    String city = "Москва/Московская обл";
                    Integer year = Integer.parseInt(elems2.get(0).text());
                    String mileage = elems2.get(1).text();
                    String bodywork = elems2.get(2).text();
                    String engine = elems2.get(4).text();
                    String transmission = elems2.get(5).text();
                    String drive = elems2.get(6).text();
                    String condition = "";
                    String href = ref;
                    elems2 = doc2.body().getElementsByAttributeValue("class", "price__current");//получение элементов с нужным атрибутом
                    String price = elems2.get(0).text();
                    elems2 = doc2.body().getElementsByAttributeValue("class", "sub-header__heading");//получение элементов с нужным атрибутом
                    String name = elems2.get(0).text();
                    elems2 = doc2.body().getElementsByAttributeValue("class", "gallery__image");//получение элементов с нужным атрибутом
                    String photo = elems2.get(0).attr("src");

                    //Запрос для вставки нового автомобиля в базу данных
                    String sql = "INSERT INTO autos (fullNameAuto, city, yearOfRelease, mileage, engine, bodywork, transmission, drive, condition, price, photo, url) " +
                            "VALUES ('" + name + "', '" + city + "', " + year + ", '" + mileage + "', '" + engine + "', '" + bodywork + "', '" + transmission + "', '" + drive + "', '" + condition + "', '" + price + "'," + "'" + photo + "', '" + href + "')";
                    try {
                        stmt.executeUpdate(sql);//запрос
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Integer numOfCar;
            for (int numOfPage = 1; numOfPage <= 1; numOfPage++) {//проход по 10 страницам сайта
                numOfCar=1;//порядковый номер машины на отдельной странице
                if (numOfPage == 1)
                    url = "http://www.bibika.ru/search?idtype=2";
                else
                    url = "http://www.bibika.ru/search/page" + numOfPage + "?idtype=2";

                Document doc = Jsoup.connect(url).maxBodySize(0).timeout(0).get();//получение документа для разбора
                Elements elems = doc.body().getElementsByAttributeValue("class", "car_advert_info");//получение элементов с нужным атрибутом
                int i = 1;
                Elements elemsForPhotos = doc.body().getElementsByTag("a").select("img");//получение ссылок на фото автомобиля

                for (Element elem : elems) {
                    if (i != 1) {
                        String urlOfCurrentCar=elem.child(0).attr("href");//получение ссылки на конкретный автомобиль
                        try {
                            if (!urlOfCurrentCar.equals("")) {
                                Document doc2 = Jsoup.connect(urlOfCurrentCar).maxBodySize(0).timeout(0).get();//получение документа конкретного автомобиля для разбора
                                Elements elemsForInfo = doc2.body().getElementsByAttributeValue("class", "small_row_right");//получение элементов с нужным атрибутом

                                if (elemsForInfo.size() == 9) {//извлечение нужных данных, если они имеются
                                    String name = elem.child(0).text();
                                    String city = elemsForInfo.get(0).text();
                                    Integer year = Integer.parseInt(elemsForInfo.get(1).text());
                                    String mileage = elemsForInfo.get(2).text();
                                    String engine = elemsForInfo.get(3).text();
                                    String bodywork = elemsForInfo.get(4).text();
                                    String transmission = elemsForInfo.get(5).text();
                                    String drive = elemsForInfo.get(6).text();
                                    String condition = elemsForInfo.get(7).text();
                                    String price = elemsForInfo.get(8).text();
                                    String href = elem.child(0).attr("href");
                                    String photo=elemsForPhotos.get(numOfCar).attr("src");

                                    //Запрос для вставки нового автомобиля в базу данных
                                    String sql = "INSERT INTO autos (fullNameAuto, city, yearOfRelease, mileage, engine, bodywork, transmission, drive, condition, price, photo, url) " +
                                            "VALUES ('" + name + "', '" + city + "', " + year + ", '" + mileage + "', '" + engine + "', '" + bodywork + "', '" + transmission + "', '" + drive + "', '" + condition + "', '" + price + "'," + "'"+photo+"', '" + href + "')";
                                    stmt.executeUpdate(sql);//запрос
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        numOfCar++;
                    }
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Справка");
        alert.setHeaderText("Данные успешно обновлены!");
    }

    public long countOfSymbol(String str){
        long answer = str.chars().filter(ch->ch=='/').count();
        return answer;
    }

    public void aboutAuthorOnAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Справка");
        alert.setHeaderText("Заказчик: ВятГУ");
        alert.setContentText("Выполнил: Янюшкин Кирилл, ФИб-3, 2018/19");
        alert.showAndWait();
    }

    public void aboutAppOnAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Справка");
        alert.setHeaderText("Работа в программе:");
        alert.setContentText("\"Обновить\" - удаление из базы старых данных и сбор новой информации с ресурсов\n"+"\"Загрузить\" - вывести данные из базы на экран\n"+"Для получения подробной информации об автомобиле необходимо нажать на него в таблице");
        alert.showAndWait();
    }

    public void buttonLoadOnAction(ActionEvent actionEvent) {
        loadOnAction(actionEvent);//при нажатии на кнопку "Загрузить" выполняются действия, аналогичные при нажатии на ту же кнопку в menuBar (данные из БД загружаются в таблицу)
    }
}
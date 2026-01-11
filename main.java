import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.image.*;
import javafx.scene.effect.*;
import javafx.scene.text.*;
import javafx.scene.input.*;
import javafx.scene.canvas.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.event.*;
import javafx.geometry.*;
import javafx.collections.*;

import java.io.*;

import javafx.animation.*;

/* #シーンの基底クラス

#機能
全シーンに必要最低限必要な機能を搭載している

#概要
UI部品の構成
scene
 | = borderPane
     |
     |=top=menu
     |     |=mb
     |        |=mi
     |        |=mi1
     |        |=mi2
     |        |=mi3
     |
     |=center=root
               |...

メゾッド
-コンストラクタ
インスタンス化およびUIノードの関係の構築
-getScene[戻り値:Scene]
sceneの返却
-setHandler[引数:EventHandler ...]
配列もしくは変数で受け取り、ハンドラが必要な変数にaddするため
-createMenuBar[戻り値:MenuBar]
メニューを作る。

 */

//グローバル変数
class GV {

    // 全シーン共通のサイズ
    public static final int sceneW = 400;
    public static final int sceneH = 450;

    // コップ作成でのドットのサイズと配列の長さ
    public static final int dot_size = 20;
    public static final int dot_len = 20;

    // タイマーの設定
    public static int totalSeconds;// 勉強時間
    public static int breakSeconds;// 休憩時間

}

class BaseScene {

    protected BorderPane borderPane;
    protected VBox root;
    protected Scene scene;

    protected MenuBar mb;
    protected Menu menu;
    protected MenuItem mi;
    protected MenuItem mi1;
    protected MenuItem mi2;
    protected MenuItem mi3;
    protected MenuItem mi4;

    public BaseScene() {

        borderPane = new BorderPane();
        root = new VBox();
        scene = new Scene(borderPane, GV.sceneW, GV.sceneH);

        borderPane.setTop(createMenuBar());
        borderPane.setCenter(root);

    }

    public Scene getScene() {
        return scene;
    }

    // 複数ある場合も想定して配列で受け取る
    public void setHandler(EventHandler<Event>... handler) {

    }

    private MenuBar createMenuBar() {
        mb = new MenuBar();
        menu = new Menu("設定");
        mi = new MenuItem("ホーム");
        mi1 = new MenuItem("タイマー設定");
        mi2 = new MenuItem("コップ作成");
        mi3 = new MenuItem("コップ選択");
        mi4 = new MenuItem("終了");

        mi.setId("home");
        mi1.setId("setting");
        mi2.setId("makeCup");
        mi3.setId("selectCup");
        mi4.setId("exit");

        menu.getItems().addAll(mi, mi1, mi2, mi3, mi4);
        mb.getMenus().addAll(menu);
        return mb;
    }

}

// タイマー画面(ホーム画面)
class TimerScene extends BaseScene {
    // UI部品
    private Label timerLabel;
    private Button timerButton;

    // タイマー関連
    private Timeline timeline;

    private enum BtnState {
        STOP,
        START,
        BREAK,
        RESET
    }

    BtnState btnState;

    public TimerScene() {

        GV.totalSeconds = 10;
        GV.breakSeconds = 5;

        btnState = BtnState.STOP;

        root.getChildren().add(createCupLabel());
        root.getChildren().add(createTimerLabel());
        root.getChildren().add(createStartButton());
        Init();

    }

    // 初期設定
    private void Init() {
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> timerUpdate()));
        timeline.setCycleCount(Timeline.INDEFINITE);

    }

    private void timerStop() {
        btnState = BtnState.STOP;
        timerButton.setText("START");
        timeline.stop();

        System.out.println("Timer stop");
    }

    // スタート
    private void timerStart() {
        btnState = BtnState.START;
        timerButton.setText("STOP");
        timeline.play();

        System.out.println("Timer start");

    }

    public void timerStateChange() {
        // System.out.println(btnState.name());
        // (STOP->START)
        if (btnState == BtnState.STOP) {
            timerStart();
        } else
        // (START->STOP)
        if (btnState == BtnState.START) {
            timerStop();
        }

    }

    private int n = 0;

    private void timerUpdate() {
        int hoge = 0;

        if (btnState == BtnState.START) {
            hoge = GV.totalSeconds - n;

        } else if (btnState == BtnState.BREAK) {
            hoge = GV.breakSeconds - n;
        }

        int m = hoge / 60;
        int s = hoge % 60;
        System.out.print("\rタイマーカウント:" + hoge + "  ");

        if (hoge <= 0) {
            if (btnState == BtnState.BREAK) {
                btnState = BtnState.START;
                timerButton.setDisable(false);
            } else if (btnState == BtnState.START) {
                btnState = BtnState.BREAK;
                timerButton.setDisable(true);
            }
            n = 0;
        }

        timerLabel.setText(String.format("%02d:%02d", m, s));
        n++;
    }

    @Override
    public void setHandler(EventHandler<Event>... handler) {
        this.menu.addEventHandler(ActionEvent.ANY, handler[0]);
        this.timerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, handler[1]);

    }

    private Label createTimerLabel() {
        timerLabel = new Label("25:00");
        timerLabel.setFont(new Font(48));
        timerLabel.setAlignment(Pos.CENTER);
        return timerLabel;
    }

    private Label createCupLabel() {
        Label cupLabel = new Label("コップ");
        cupLabel.setFont(new Font(24));
        cupLabel.setAlignment(Pos.CENTER);
        return cupLabel;
    }

    private Button createStartButton() {
        timerButton = new Button("START");
        timerButton.setFont(new Font(24));
        return timerButton;
    }

}

class SettingScene extends BaseScene {
    private Label labelSetting;
    private Label labelColor;
    private ColorPicker colorPicker;

    public SettingScene() {

        labelSetting = new Label("設定画面");
        labelColor = new Label("色設定");
        colorPicker = new ColorPicker();
        colorPicker.getStyleClass().add(ColorPicker.STYLE_CLASS_BUTTON);
        colorPicker.setValue(Color.CORAL);

        root.getChildren().addAll(labelSetting, labelColor);
        root.getChildren().add(colorPicker);

    }

    @Override
    public void setHandler(EventHandler<Event>... handler) {
        colorPicker.addEventHandler(ActionEvent.ANY, handler[0]);
        this.menu.addEventHandler(ActionEvent.ANY, handler[1]);
    }

    public void CreateSettingContent() {

    }
}

enum DOT_STATUS {
    DRAW,
    WATER,
    EMPTY,
}

class MakeCupScene extends BaseScene {

    private HBox buttons = new HBox();
    private Button makeShape;
    private Button makeWater;
    private Button finish;
    private Button eraser;

    private Pane drawPane;
    private DOT_STATUS mode = DOT_STATUS.DRAW;
    private Color modeColor = Color.BLACK;

    private DOT_STATUS[][] dots = new DOT_STATUS[GV.dot_len][GV.dot_len];
    private Rectangle[][] rects = new Rectangle[GV.dot_len][GV.dot_len];

    public MakeCupScene() {
        makeShape = new Button("Draw");
        makeWater = new Button("Water");
        finish = new Button("Finish");
        eraser = new Button("eraser");
        buttons.getChildren().addAll(makeShape, eraser, makeWater, finish);

        makeShape.setOnMouseClicked(e -> {
            mode = DOT_STATUS.DRAW;
            modeColor = Color.BLACK;
            System.out.println(mode.name());
        });

        makeWater.setOnMouseClicked(e -> {
            mode = DOT_STATUS.WATER;
            modeColor = Color.AQUA;
            System.out.println(mode.name());
        });

        eraser.setOnMouseClicked(e -> {
            mode = DOT_STATUS.EMPTY;
            modeColor = Color.WHITE;
            System.out.println(mode.name());
        });

        finish.setOnMouseClicked(e -> {
            ConfirmationWindow();

            System.out.println("finish");

        });

        drawPane = new Pane();
        drawPane.setPrefSize(GV.dot_size * GV.dot_len, GV.dot_size * GV.dot_size);

        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                rects[y][x] = new Rectangle(x * GV.dot_size, y * GV.dot_size, GV.dot_size, GV.dot_size);
                drawPane.getChildren().add(rects[y][x]);
                dots[y][x] = DOT_STATUS.EMPTY;
                rects[y][x].setFill(Color.WHITE);
                rects[y][x].setStroke(Color.GRAY);
            }
        }

        drawPane.setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();

            int dx = (int) (x / GV.dot_size);
            int dy = (int) (y / GV.dot_size);
            dots[dy][dx] = mode;
            rects[dy][dx].setFill(modeColor);
        });

        drawPane.setOnMouseDragged((e -> {
            double x = e.getX();
            double y = e.getY();

            int dx = (int) (x / GV.dot_size);
            int dy = (int) (y / GV.dot_size);
            dots[dy][dx] = mode;
            rects[dy][dx].setFill(modeColor);

        }));

        root.getChildren().addAll(buttons);
        root.getChildren().addAll(drawPane);
    }

    @Override
    public void setHandler(EventHandler<Event>... event) {
        menu.addEventHandler(ActionEvent.ANY, event[0]);
    }

    private void ConfirmationWindow() {

        VBox vBox = new VBox();
        HBox btnAndtf = new HBox();
        TextField textField = new TextField("");

        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= 10) {
                return change;
            }
            return null; // 変更を無効化
        });
        textField.setTextFormatter(formatter);
        textField.setPromptText("名前:10文字まで");
        textField.setFocusTraversable(false);

        Button btn = new Button("決定");
        btnAndtf.getChildren().addAll(textField, btn);
        Pane p = new Pane();
        // p.setPrefSize(10,10);
        Rectangle[][] show_rec = new Rectangle[GV.dot_len][GV.dot_len];
        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                show_rec[y][x] = new Rectangle(10, 10, 10, 10);
                show_rec[y][x].setFill(rects[y][x].getFill());
                show_rec[y][x].setX(x * 10);
                show_rec[y][x].setY(y * 10);
                p.getChildren().add(show_rec[y][x]);

            }
        }

        vBox.getChildren().add(btnAndtf);
        vBox.getChildren().add(p);

        Scene scene = new Scene(vBox, 200, 200);
        Stage stage = new Stage();
        stage.setTitle("コップの命名");
        stage.setScene(scene);
        stage.show();

        btn.setOnMouseClicked(e -> {
            try {
                FileWriter file = new FileWriter("./cups.txt", true);
                PrintWriter pw = new PrintWriter(new BufferedWriter(file));

                // 現在時刻取得
                // 及び表示形式の指定
                LocalDateTime nowDate = LocalDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss E曜日");
                String formatNowDate = dtf.format(nowDate);
                pw.println("[cup]");
                pw.println("name=" + textField.getText());
                pw.println("date=" + formatNowDate);
                pw.print("data=");
                String prev = "";
                int count = 0;
                for (int y = 0; y < GV.dot_len; y++) {
                    for (int x = 0; x < GV.dot_len; x++) {
                        String dot_status = dots[y][x].name();
                        if (dot_status.equals("EMPTY"))
                            dot_status = "E";
                        if (dot_status.equals("WATER"))
                            dot_status = "W";
                        if (dot_status.equals("DRAW"))
                            dot_status = "D";

                        if (prev.equals(""))
                            prev = dot_status;

                        // 一個前と同じか\\同じでないなら
                        if (prev.equals(dot_status))
                            count++;
                        else {
                            pw.print(prev + count);
                            prev = dot_status;
                            count = 1;
                        }

                    }

                }
                pw.print(prev + count);

                pw.println();
                pw.close();

            } catch (IOException ee) {
                System.out.println("予期せぬエラーです。");
            }
            stage.close();
        });
    }
}

class Cup {
    /*
     * name: 名前
     * date: 作成日時
     * entity: 実際の形状
     * drawCup:コップのグラフィックス
     */
    private DOT_STATUS[][] entity;
    Rectangle[][] dots;
    private Pane pane;

    // その他必要なボタンやUI
    private HBox root;
    private VBox labels;
    private Label nameLabel;
    private Label dateLabel;
    // private HBox paneAndBtns;
    private HBox btns;
    private Button select;
    private Button deletion;
    private Button openButton;
    private BorderPane bP;

    public Cup() {

        entity = new DOT_STATUS[20][20];
        dots = new Rectangle[GV.dot_len][GV.dot_len];
        pane = new Pane();
        root = new HBox();
        labels = new VBox();
        nameLabel = new Label("名無し");
        dateLabel = new Label("名無し");
        btns = new HBox(20);
        select = new Button("選択");
        deletion = new Button("削除");
        openButton = new Button("閲覧");
        initBtn();
        bP = new BorderPane();

        pane.setMaxSize(50, 50);
        pane.setPrefSize(300, 300);

        labels.setPadding(new Insets(10));

        btns.getChildren().addAll(openButton, select, deletion);
        labels.getChildren().addAll(nameLabel, dateLabel);
        root.getChildren().addAll(labels, btns);
        root.setMargin(btns, new Insets(10));
        root.setAlignment(Pos.CENTER);

    }

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void setDate(String date) {
        dateLabel.setText(date);
    }

    public void setEntity(DOT_STATUS[][] entity) {
        this.entity = entity;

        for (int y = 0; y < entity.length; y++) {
            for (int x = 0; x < entity[y].length; x++) {
                if (this.entity[y][x] == null) {
                    this.entity[y][x] = DOT_STATUS.EMPTY;
                }
            }
        }
        if (this.entity == null)
            System.out.println();
        drawCup();
    }

    private void drawCup() {
        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                Color color = null;
                if (entity[y][x].equals(DOT_STATUS.DRAW))
                    color = Color.BLACK;
                if (entity[y][x].equals(DOT_STATUS.WATER))
                    color = Color.AQUA;
                if (entity[y][x].equals(DOT_STATUS.EMPTY))
                    color = Color.WHITE;

                dots[y][x] = new Rectangle(10, 10, 10, 10);
                dots[y][x].setFill(color);
                dots[y][x].setX(x * 10);
                dots[y][x].setY(y * 10);
                pane.getChildren().add(dots[y][x]);

            }
        }
    }

    private void initBtn() {
        openButton.setOnMouseClicked(e -> {

            Scene scene = new Scene(pane, 200, 200);
            Stage stage = new Stage();
            stage.setTitle("コップ選択");
            stage.setScene(scene);
            stage.show();

        });

        deletion.setOnMouseClicked(e -> {
            cupDelete();
        });

    }

    public HBox getRoot() {
        return root;
    }

    private void cupDelete() {
        // cupの削除と
        // 保存データの更新
        File file = new File("cups.txt");

        try (BufferedReader br = new BufferedReader(new FileReader((file)))) {
            ArrayList<String> fileCopy = new ArrayList<>();
            String str;

            while ((str = br.readLine()) != null) {
                if (str.equals("[cup]")) {
                    br.mark(20);

                    str = br.readLine();
                    String name = str.substring("name=".length());

                    if (name.equals(nameLabel.getText())) {
                        br.reset();
                        for (int i = 0; i < 3; i++)
                            br.readLine();
                        continue;
                    } else {
                        fileCopy.add("[cup]");

                    }
                }
                fileCopy.add(str);

            }
            System.out.println(fileCopy);
            System.out.println();

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}

class SelectScene extends BaseScene {

    private ArrayList<Cup> cups = new ArrayList<>();

    public SelectScene() {
        read();

        for (int i = 0; i < cups.size(); i++)
            root.getChildren().add(cups.get(i).getRoot());
    }

    public void read() {

        File file = new File("cups.txt");

        try (BufferedReader br = new BufferedReader(new FileReader((file)))) {
            String str;
            while ((str = br.readLine()) != null) {

                if (str.equals("[cup]")) {

                    // カップオブジェクトの作成
                    Cup cup = new Cup();

                    // 名前の取得
                    String name = br.readLine();
                    String date = br.readLine();
                    cup.setName(name.substring("name=".length()));
                    cup.setDate(date.substring("date=".length()));

                    // 形状の取得 DWE

                    ArrayList<DOT_STATUS> ary = new ArrayList<>();
                    DOT_STATUS[][] copy = new DOT_STATUS[GV.dot_len][GV.dot_len];
                    str = br.readLine();
                    str = str.substring("data=".length());
                    String[] splitted = str.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    System.out.println(Arrays.toString(splitted));
                    for (int i = 0; i < splitted.length / 2; i++) {

                        if (splitted[i * 2].equals("E"))
                            for (int j = 0; j < Integer.parseInt(splitted[i * 2 + 1]); j++)
                                ary.add(DOT_STATUS.EMPTY);
                        if (splitted[i * 2].equals("D"))
                            for (int j = 0; j < Integer.parseInt(splitted[i * 2 + 1]); j++)
                                ary.add(DOT_STATUS.DRAW);
                        if (splitted[i * 2].equals("W"))
                            for (int j = 0; j < Integer.parseInt(splitted[i * 2 + 1]); j++)
                                ary.add(DOT_STATUS.WATER);
                        System.out.println(splitted[i * 2]);
                    }
                    System.out.println(ary.size());
                    for (int y = 0; y < GV.dot_len; y++) {
                        for (int x = 0; x < GV.dot_len; x++) {
                            copy[y][x] = ary.get(x + y * GV.dot_len);
                        }
                    }
                    cup.setEntity(copy);
                    System.out.println("ok");
                    cups.add(cup);
                }
            }
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }

    }

    @Override
    public void setHandler(EventHandler<Event>... event) {
        menu.addEventHandler(ActionEvent.ANY, event[0]);
    }

}

public class main extends Application {

    Stage baseStage;
    MenuEventHandler myEventHandler;
    TimerEventHandler timerEventHandler;
    TimerScene timerScene;

    @Override
    public void start(Stage stage) {
        myEventHandler = new MenuEventHandler();
        timerEventHandler = new TimerEventHandler();
        timerScene = new TimerScene();

        timerScene.setHandler(myEventHandler, timerEventHandler);
        baseStage = stage;
        baseStage.setScene(timerScene.getScene());
        baseStage.setTitle("タイマー");
        baseStage.show();

    }

    // タイマーの操作系
    private class TimerEventHandler implements EventHandler<Event> {
        boolean firstStart = false;

        @Override
        public void handle(Event event) {
            if (!firstStart) {
                firstStart = true;

            }
            timerScene.timerStateChange();
            System.out.println("押しているよ");

        }
    }

    private class MenuEventHandler implements EventHandler<Event> {
        @Override
        public void handle(Event event) {
            MenuItem mi = (MenuItem) event.getTarget();
            String id = mi.getId();

            if (id.equals("home")) {

                timerScene.setHandler(myEventHandler, timerEventHandler);
                baseStage.setScene(timerScene.getScene());
            }

            if (id.equals("exit")) {
                Platform.exit();
            }
            if (id.equals("setting")) {
                SettingScene settingScene = new SettingScene();
                SettingEventHandler settingEventHandler = new SettingEventHandler();
                MenuEventHandler menuEventHandler = new MenuEventHandler();

                settingScene.setHandler(settingEventHandler, menuEventHandler);
                baseStage.setScene(settingScene.getScene());
            }

            if (id.equals("makeCup")) {
                MakeCupScene makeCupScene = new MakeCupScene();

                MenuEventHandler menuEventHandler = new MenuEventHandler();

                makeCupScene.setHandler(menuEventHandler);

                baseStage.setScene(makeCupScene.getScene());

            }

            if (id.equals("selectCup")) {

                SelectScene selectScene = new SelectScene();
                MenuEventHandler menuEventHandler = new MenuEventHandler();
                selectScene.setHandler(menuEventHandler);

                baseStage.setScene(selectScene.getScene());

            }
        }
    }

    private class SettingEventHandler implements EventHandler<Event> {
        @Override
        public void handle(Event event) {
            ColorPicker cp = (ColorPicker) event.getTarget();
            Color color = cp.getValue();
            System.out.println(color.toString());
        }
    }

    public static void main(String[] args) {
        launch(args);

    }
}

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

//グローバル変数
class GV {

    public static void getDefultCupNmae() {
    // ファイルの読み込み
    File file = new File("cups.txt");

    try {
        // ファイルが読み込み可能でない場合、作成する
        if (!file.canRead()) {
            file.createNewFile();
        }
    } catch (Exception e) {
        System.out.println("ファイルの作成に失敗しました: " + e.getMessage());
        return;
    }

    String str = null; // 読み込んだ内容を格納するためにnullで初期化
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        // ファイルの1行目を読み込み
        str = br.readLine();
        System.out.println(str == null); // nullである場合、デフォルト値を設定する

    } catch (IOException io) {
        System.out.println("ファイル読み込みエラー: " + io.getMessage());
    }

    // strがnullの場合は、ファイルにデフォルト情報を追加
    if (str == null) {
        try (PrintWriter bw = new PrintWriter(new FileWriter(file, true))) {
            bw.println("SELECTED_CUP:default");
            bw.println("[cup]");
            bw.println("name=default");
            bw.println("date=9999/99/99 99:99:99 ●曜日");
            bw.println("data=E63D2E7D2E9D2E7D2E9D2W7D5E6D2W7D2E1D2E6D2W7D2E1D2E6D2W7D2E1D2E6D2W7D2E1D2E6D2W7D2E1D2E6D2W7D2E1D2E6D2W7D2E1D2E6D2W7D5E6D2W7D2E9D11E9D11E66");
        } catch (IOException e) {
            System.out.println("ファイルへの書き込みエラー: " + e.getMessage());
        }
    } else {
        // 既存のSELECTED_CUPの名前を取得
        if (str.contains("SELECTED_CUP")) {
            GV.SELECTED_CUP = str.substring("SELECTED_CUP:".length());
            System.out.println("選択されたカップ名: " + GV.SELECTED_CUP);
        }
    }

    // ファイルの内容を再度読み込んで、カップ情報を取り出す
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.equals("[cup]")) {
                // カップオブジェクトの作成
                Cup cup = new Cup();
                // 名前と日付を取得
                String name = br.readLine();
                String date = br.readLine();

                // コップの名前と日付をセット
                cup.setName(name.substring("name=".length()));
                cup.setDate(date.substring("date=".length()));

                // ドット情報を取得
                ArrayList<DOT_STATUS> ary = new ArrayList<>();
                DOT_STATUS[][] copy = new DOT_STATUS[GV.dot_len][GV.dot_len];

                // データ行の取得と正規化
                String dataLine = br.readLine();
                dataLine = dataLine.substring("data=".length());

                // RLE(ランレングス圧縮)の解凍
                String[] splitted = dataLine.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
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
                }

                // 一次元配列を二次元に変換
                for (int y = 0; y < GV.dot_len; y++) {
                    for (int x = 0; x < GV.dot_len; x++) {
                        copy[y][x] = ary.get(x + y * GV.dot_len);
                    }
                }

                // カップのエンティティを設定
                cup.setEntity(copy);

                // 選択されたカップの設定
                if (cup.getName().equals(GV.SELECTED_CUP)) {
                    GV.nowCupEntity = cup;
                    System.out.println("選択されたカップのエンティティがセットされました。");
                }
            }
        }
    } catch (IOException io) {
        System.out.println("ファイル読み込みエラー: " + io.getMessage());
    }
}

    // 全シーン共通のサイズ
    public static final int sceneW = 420;
    public static final int sceneH = 470;

    // コップ作成でのドットのサイズと配列の長さ
    public static final int makeCupScene_dot_size = 20;
    public static final int timerScene_dot_size = 15;
    public static final int selectedScene_dot_size = 10;
    public static final int viewCup_window = 10;
    public static final int dot_len = 20;

    // タイマーの設定
    public static final int totalSeconds = 5;// 勉強時間
    public static final int breakSeconds = 5;// 休憩時間
    public static Cup nowCupEntity;
    public static String SELECTED_CUP;

}

/*
 * #シーンの基底クラス
 * 
 * #機能
 * 全シーンに必要最低限必要な機能を搭載している
 * 
 * #概要
 * UI部品の構成
 * scene
 * | = borderPane
 * |
 * |=top=menu
 * | |=mb
 * | |=mi
 * | |=mi1
 * | |=mi2
 * | |=mi3
 * |
 * |=center=root
 * |...
 * 
 * メゾッド
 * -コンストラクタ
 * インスタンス化およびUIノードの関係の構築
 * -getScene[戻り値:Scene]
 * sceneの返却
 * -setHandler[引数:EventHandler ...]
 * 配列もしくは変数で受け取り、ハンドラが必要な変数にaddするため
 * -createMenuBar[戻り値:MenuBar]
 * メニューを作る。
 * 
 */
class BaseScene {

    protected VBox root;
    protected Scene scene;
    protected BorderPane borderPane;

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
        root.setAlignment(Pos.CENTER);

        borderPane.setTop(createMenuBar());
        borderPane.setCenter(root);
        scene = new Scene(borderPane, GV.sceneW, GV.sceneH);
        scene.setFill(Color.WHITE);
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
        mi1 = new MenuItem("アプリ概要");
        mi2 = new MenuItem("コップ作成");
        mi3 = new MenuItem("コップ選択");
        mi4 = new MenuItem("終了");

        mi.setId("home");
        mi1.setId("setting");
        mi2.setId("makeCup");
        mi3.setId("selectCup");
        mi4.setId("exit");

        menu.getItems().addAll(mi, mi1,  mi2, mi3, mi4);
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
    private Timeline waterTimeline;

    // コップで表示するため
    private GridPane gridPane;

    // ボタンの状態一覧
    private enum BtnState {
        STOP,
        START,
        BREAK,
        RESET
    }

    // ボタン状態保持
    BtnState btnState;

    public TimerScene() {

        // ストップで設定
        btnState = BtnState.STOP;

        gridPane = new GridPane();

        // デフォルトが読み込めている
        if (GV.nowCupEntity != null)
            draw();
        else {
            root.getChildren().add(createCupLabel());
            root.getChildren().add(createTimerLabel());
            root.getChildren().add(createStartButton());
        }

        // タイマー部分
        // 一秒ごとに更新
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> timerUpdate()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        // アニメーション部分
        // 0.05秒に更新
        waterTimeline = new Timeline(new KeyFrame(Duration.millis(50), e -> updateWater()));
        waterTimeline.setCycleCount(Timeline.INDEFINITE);

    }

    // ストップ
    private void timerStop() {
        btnState = BtnState.STOP;
        timerButton.setText("START");
        timeline.stop();
        waterTimeline.stop();

        System.out.println("Timer stop");
    }

    // スタート
    private void timerStart() {
        btnState = BtnState.START;
        timerButton.setText("STOP");
        timeline.play();
        waterTimeline.play();

        System.out.println("Timer start");

    }

    private int n = 0;

    // タイマーのアップデートと休憩と勉強の切り替え
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
                time = 0;
            } else if (btnState == BtnState.START) {
                btnState = BtnState.BREAK;
                timerButton.setDisable(true);// 無効か
                time = 0;
            }
            n = 0;
            time = 0;
        }

        timerLabel.setText(String.format("%02d:%02d", m, s));
        n++;
    }

    private Label createTimerLabel() {
        timerLabel = new Label("25:00");
        timerLabel.setFont(new Font(48));
        timerLabel.setAlignment(Pos.CENTER);
        return timerLabel;
    }

    private Label createCupLabel() {
        Label cupLabel = new Label("コップを選択してください");
        cupLabel.setFont(new Font(24));
        cupLabel.setAlignment(Pos.CENTER);
        return cupLabel;

    }

    private Button createStartButton() {
        timerButton = new Button("START");
        timerButton.setFont(new Font(24));
        return timerButton;
    }

    // いま選択されているcupをgridPaneに描写する
    private void drawCup() {

        GridPane gp = new GridPane();

        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                Rectangle r = GV.nowCupEntity.getDot()[y][x];
                if (r.getFill() == Color.WHITE) {
                    r.setFill(null);
                    continue;
                }
                r.setWidth(GV.timerScene_dot_size);
                r.setHeight(GV.timerScene_dot_size);
                gp.setValignment(r, VPos.BOTTOM);
                gp.setHalignment(r, HPos.CENTER);
                gp.add(r, x, y);

            }
        }

        gridPane = gp;

        prevRecs = GV.nowCupEntity.getDot();

    }

    private double time = 0.f;
    private Rectangle[][] prevRecs;// 前の状態を管理する。
    // private Rectangle[][] fillRecs;// 水が減りあいた空間を埋めるため

    private void updateWater() {

        if (btnState == BtnState.START)
            downWater();
        if (btnState == BtnState.BREAK)
            upWater();

    }

    private void upWater() {
        time += 0.05;

        if (GV.nowCupEntity == null)
            return;

        // 水の範囲を取得
        int maxY = 0;
        int minY = GV.dot_len;

        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                if (GV.nowCupEntity.getEntity()[y][x] == DOT_STATUS.WATER) {
                    if (maxY < y)
                        maxY = y;
                    if (y < minY)
                        minY = y;
                }
            }
        }

        // 水の高さ
        int waterYArea = (maxY - minY) + 3;

        // 1行あたりの上昇時間
        double oneYAreaChangeTime = GV.breakSeconds / (double) waterYArea;

        // 現在の水位（下 → 上）
        int nowY = (int) (maxY - time / oneYAreaChangeTime);
        if (nowY < minY)
            nowY = minY;

        // 描画更新
        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {

                Rectangle rec = prevRecs[y][x];
                DOT_STATUS dot = GV.nowCupEntity.getEntity()[y][x];

                if (dot == DOT_STATUS.WATER && y >= nowY) {
                    rec.setFill(Color.AQUA);

                    rec.setHeight(GV.timerScene_dot_size);
                    // rec.setY(y*GV.timerScene_dot_size);
                    rec.setWidth(GV.timerScene_dot_size);

                }
            }
        }

    }

    private void downWater() {
        time += 0.05f;

        if (GV.nowCupEntity == null)
            return;

        int maxY = 0;
        int minY = GV.dot_len;

        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                if (GV.nowCupEntity.getEntity()[y][x] == DOT_STATUS.WATER) {
                    if (maxY < y)
                        maxY = y;
                    if (y < minY)
                        minY = y;
                }
            }
        }

        int waterYArea = maxY - minY + 1;
        double oneYAreaChangeTime = GV.totalSeconds / (double) waterYArea;
        double changeValue = GV.timerScene_dot_size / oneYAreaChangeTime;
        changeValue *= 0.05;
        int nowY = (int) (minY + time / oneYAreaChangeTime);
        if (maxY < nowY)
            nowY = maxY;

        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                // 更新するy軸の配列か

                Rectangle rec = prevRecs[y][x];

                if (nowY == y && GV.nowCupEntity.getEntity()[y][x] == DOT_STATUS.WATER) {
                    double h = prevRecs[y][x].getHeight();
                    if (h <= 0)
                        h = 0;

                    rec.setHeight(0);
                    // rec.setY(y*GV.timerScene_dot_size);
                    rec.setWidth(GV.timerScene_dot_size);

                }

                prevRecs[y][x] = rec;

            }
        }

    }

    public void draw() {
        drawCup();

        // UIのクリアとポジションをcenterに
        root.getChildren().clear();
        root.setAlignment(Pos.CENTER);

        // タイマー、コップ、ボタンの順に設置
        root.getChildren().add(createTimerLabel());
        root.getChildren().add(gridPane);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(20));
        root.getChildren().add(createStartButton());

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

    @Override
    public void setHandler(EventHandler<Event>... handler) {
        this.menu.addEventHandler(ActionEvent.ANY, handler[0]);
        this.timerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, handler[1]);

    }

}
//説明
class ExpScene extends BaseScene {
        private Label label_exp;
    private Label labelInfo;

    public ExpScene() {

        // アプリの概要説明ラベル
        label_exp = new Label("このアプリケーションは、あなたの操作に応じてカップを選択し、カスタマイズするゲームアプリです。\n" +
                              "コップを選び、そのデータに基づいてゲームを進めていきます。");

        // 詳細な操作説明ラベル
        labelInfo = new Label("1. コップの選択: 「コップ選択画面」から好きなコップを選びます。\n" +
                              "2. カスタマイズ: それぞれのコップにはカスタムデータ（色、サイズなど）が設定されています。\n" +
                              "3. ゲーム開始: コップを選択した後、ゲームが開始されます。");

        // レイアウトにラベルを追加
        root.getChildren().addAll(label_exp, labelInfo);

        // 各ラベルの位置設定
        label_exp.setLayoutX(50);  // 位置をX座標50に設定
        label_exp.setLayoutY(30);  // 位置をY座標30に設定

        labelInfo.setLayoutX(50);  // 位置をX座標50に設定
        labelInfo.setLayoutY(100); // 位置をY座標100に設定

        // フォントサイズやスタイルを変更して視覚的に強調することもできます
        label_exp.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        labelInfo.setStyle("-fx-font-size: 14px;");
    }

    @Override
    public void setHandler(EventHandler<Event>... handler) {
        
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

    private HBox buttons;
    private Button makeShape;
    private Button makeWater;
    private Button finish;
    private Button eraser;

    private GridPane drawPane;
    private DOT_STATUS mode = DOT_STATUS.DRAW;
    private Color modeColor = Color.BLACK;

    private DOT_STATUS[][] dots = new DOT_STATUS[GV.dot_len][GV.dot_len];// 保存するさいに必要
    private Rectangle[][] rects = new Rectangle[GV.dot_len][GV.dot_len];

    public MakeCupScene() {
        buttons = new HBox();
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

        drawPane = new GridPane();

        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                Rectangle rect = new Rectangle(x * GV.makeCupScene_dot_size, y * GV.makeCupScene_dot_size,
                        GV.makeCupScene_dot_size, GV.makeCupScene_dot_size);

                drawPane.add(rect, x, y);
                dots[y][x] = DOT_STATUS.EMPTY;
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.GRAY);
                rects[y][x] = rect;

                final int dy = y;
                final int dx = x;
                rect.setOnMouseClicked(e -> {

                    if (e.getButton() == MouseButton.PRIMARY) {
                        dots[dy][dx] = mode;
                        rect.setFill(modeColor);
                    } else if (e.getButton() == MouseButton.SECONDARY) {
                        dots[dy][dx] = DOT_STATUS.EMPTY;
                        rect.setFill(Color.WHITE);
                    }

                    System.out.println(dots[dy][dx].name());
                });

            }
        }

        drawPane.setOnMouseDragged(e -> {
            Node n = e.getPickResult().getIntersectedNode();
            if (n instanceof Rectangle rect) {
                int dx = GridPane.getColumnIndex(rect);
                int dy = GridPane.getRowIndex(rect);
                if (e.getButton() == MouseButton.PRIMARY) {
                    dots[dy][dx] = mode;
                    rect.setFill(modeColor);
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    dots[dy][dx] = DOT_STATUS.EMPTY;
                    rect.setFill(Color.WHITE);
                }

                System.out.println(dots[dy][dx].name());

            }
        });

        root.getChildren().addAll(buttons);
        root.getChildren().addAll(drawPane);
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
        GridPane p = new GridPane();
        p.setMouseTransparent(true);

        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                Rectangle r = rects[y][x];
                r.setWidth(GV.viewCup_window);
                r.setHeight(GV.viewCup_window);
                r.setStroke(null);

                p.add(r, x, y);

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

    @Override
    public void setHandler(EventHandler<Event>... event) {
        menu.addEventHandler(ActionEvent.ANY, event[0]);
    }

}

class Cup {
    /*
     * name: 名前
     * date: 作成日時
     * entity: 実際の形状
     * drawCup:コップのグラフィックス
     */
    private DOT_STATUS[][] entity;// 文字での情報を持つ
    private Rectangle[][] dots;// 実際の形で保存する
    private GridPane gridPane;// 集合体として

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

    private SelectScene parent;

    public Cup(SelectScene s) {

        parent = s;
        entity = new DOT_STATUS[20][20];
        dots = new Rectangle[GV.dot_len][GV.dot_len];
        gridPane = new GridPane();
        root = new HBox();
        labels = new VBox();
        nameLabel = new Label("名無し");
        dateLabel = new Label("名無し");
        btns = new HBox(20);
        select = new Button("選択");
        deletion = new Button("削除");
        openButton = new Button("閲覧");
        initBtn();

        labels.setPadding(new Insets(10));

        btns.getChildren().addAll(openButton, select, deletion);
        labels.getChildren().addAll(nameLabel, dateLabel);
        root.getChildren().addAll(labels, btns);
        root.setMargin(btns, new Insets(10));
        root.setAlignment(Pos.CENTER);

    }
    public Cup() {

        
        entity = new DOT_STATUS[20][20];
        dots = new Rectangle[GV.dot_len][GV.dot_len];
        gridPane = new GridPane();
        root = new HBox();
        labels = new VBox();
        nameLabel = new Label("名無し");
        dateLabel = new Label("名無し");
        btns = new HBox(20);
        select = new Button("選択");
        deletion = new Button("削除");
        openButton = new Button("閲覧");
        initBtn();

        labels.setPadding(new Insets(10));

        btns.getChildren().addAll(openButton, select, deletion);
        labels.getChildren().addAll(nameLabel, dateLabel);
        root.getChildren().addAll(labels, btns);
        root.setMargin(btns, new Insets(10));
        root.setAlignment(Pos.CENTER);

    }

    public void setDot_status(DOT_STATUS[][] ds) {
        entity = ds;
        setEntity(ds);
    }

    public Rectangle[][] getDot() {
        return dots;
    }

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void setDate(String date) {
        dateLabel.setText(date);
    }

    public DOT_STATUS[][] getEntity() {
        return entity;
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
                gridPane.add(dots[y][x], x, y);

            }
        }
    }

    public GridPane getDrawCupPane() {

        return gridPane;
    }

    public void updateDraw() {
        for (int y = 0; y < GV.dot_len; y++) {
            for (int x = 0; x < GV.dot_len; x++) {
                Color color = null;
                if (entity[y][x].equals(DOT_STATUS.DRAW))
                    color = Color.BLACK;
                if (entity[y][x].equals(DOT_STATUS.WATER))
                    color = Color.AQUA;
                if (entity[y][x].equals(DOT_STATUS.EMPTY))
                    color = Color.WHITE;

                dots[y][x].setFill(color);
                gridPane.add(dots[y][x], x, y);

            }
        }
    }

    private Stage stage;

    private void initBtn() {
        openButton.setOnMouseClicked(e -> {
            if (stage == null) {
                Scene scene = new Scene(gridPane, 200, 200);
                stage = new Stage();
                stage.setTitle("コップ選択");
                stage.setScene(scene);
            }

            stage.show();

        });

        deletion.setOnMouseClicked(e -> {
            cupDelete();
            parent.removeCup(this);

        });

        select.setOnMouseClicked(e -> {
            selectedCup();
        });

    }

    public HBox getRoot() {
        return root;
    }

    private void cupDelete() {
        // cupの削除と
        // 保存データの更新
        File file = new File("cups.txt");
        ArrayList<String> fileCopy = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader((file)))) {
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

        try {
            FileWriter fileWriter = new FileWriter("./cups.txt", false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fileWriter));
            for (int i = 0; i < fileCopy.size(); i++) {
                pw.println(fileCopy.get(i));

            }
            pw.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    private void selectedCup() {
        GV.nowCupEntity = this;
        parent.setNowCup(this);

    }

    public String getName() {
        return nameLabel.getText();
    }

    public Label getNameLabel() {
        return nameLabel;
    }
}

class SelectScene extends BaseScene {

    /*
     * cups: コップ管理list
     * selectedCup: 選ばれているコップを表示するためのLabel
     * nowCup: 選ばれているコップの情報を格納
     * timerScene: タイマーシーン
     * scrollPane: スクロールのために使用
     * cupContainer:スクロールの子でこの中にコップを追加
     */
    private ArrayList<Cup> cups = new ArrayList<>();
    private Label selectedCup;
    private Cup nowCup;
    private TimerScene timerScene;
    private ScrollPane scrollPane;
    private VBox cupContainer;

    public SelectScene() {

        saveFileRead();

        cupContainer = new VBox(10);

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setContent(cupContainer);// スクロール表示する子

        root.getChildren().add(scrollPane);// 親直下に配置

        // コップ周辺のＵＩの設置
        for (int i = 0; i < cups.size(); i++)
            cupContainer.getChildren().add(cups.get(i).getRoot());

        // コップの中に選択中のコップがあるならグローバルに設置
        for (Cup c : cups) {
            if (GV.SELECTED_CUP == c.getName()) {
                GV.nowCupEntity = c;
            }
        }

    }

    // 保存ファイルを読み込みデータにあるコップの情報をCupごとにインスト＆選択中の表示
    public void saveFileRead() {
        // ファイルの指定
        File file = new File("cups.txt");

        // ファイルの読み込み
        try (BufferedReader br = new BufferedReader(new FileReader((file)))) {

            // 一行分を読み込み保存するため
            String str;

            // 一行ずつ最後まで読み込む
            while ((str = br.readLine()) != null) {

                // カップ情報の文頭か
                if (str.equals("[cup]")) {

                    // カップオブジェクトの作成
                    Cup cup = new Cup(this);

                    // 名前と日付、取得
                    String name = br.readLine();
                    String date = br.readLine();

                    // コップの名前と日付をセット
                    cup.setName(name.substring("name=".length()));
                    cup.setDate(date.substring("date=".length()));

                    // ドット情報をとりあえず一次元で取得
                    ArrayList<DOT_STATUS> ary = new ArrayList<>();
                    // ドット情報を２次元にして扱いやすいようにするため
                    DOT_STATUS[][] copy = new DOT_STATUS[GV.dot_len][GV.dot_len];

                    // データ行の取得と正規化
                    str = br.readLine();
                    str = str.substring("data=".length());

                    // RLE(ランレングス圧縮)を解凍する
                    String[] splitted = str.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
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
                    }

                    // １次元を二次元配列に移動
                    for (int y = 0; y < GV.dot_len; y++) {
                        for (int x = 0; x < GV.dot_len; x++) {
                            copy[y][x] = ary.get(x + y * GV.dot_len);
                        }
                    }

                    // 実態をCupにセット
                    cup.setEntity(copy);

                    // 配列にadd
                    cups.add(cup);

                    if (cup.getName().equals(GV.SELECTED_CUP)) {
                        GV.nowCupEntity = cup;

                    }
                }
            }

        } catch (IOException io) {
            System.out.println(io.getMessage());
        }

        // コップがひとつもない
      if (GV.SELECTED_CUP != null) {
    // すでにラベルが存在すればそのまま、なければ新規作成
    selectedCup = (selectedCup == null ? new Label("選択中:" + GV.SELECTED_CUP) : selectedCup);
    // ラベルが既に存在する場合、テキストを最新に更新する処理を入れるとより安全です
    selectedCup.setText("選択中:" + GV.SELECTED_CUP);
} else {
    // 選択されていない場合の表示
    if (selectedCup == null) {
        selectedCup = new Label("選択されていません");
    } else {
        selectedCup.setText("選択されていません");
    }
}

    }

    public void redraw() {
        cupContainer.getChildren().clear();

        if (cups.isEmpty()) {
            // 候補がないことを伝えるUI設置
            NothingCupData();
        } else {
            // スクロールの子のBoxにコップを配置
            for (Cup c : cups) {
                cupContainer.getChildren().add(c.getRoot());
            }

        }

    }

    @Override
    public void setHandler(EventHandler<Event>... event) {
        menu.addEventHandler(ActionEvent.ANY, event[0]);
    }

    public void removeCup(Cup cup) {
        cups.remove(cup);
        redraw();
    }

    private void NothingCupData() {
        Label label = new Label("候補がないです.\nコップを作成してください");
        root.getChildren().add(label);
        root.setAlignment(Pos.CENTER);

    }

    public void setNowCup(Cup cup) {

        nowCup = cup;
        GV.SELECTED_CUP = nowCup.getName();
        System.out.println(nowCup.getName());
        selectedCup.setText("選択中:" + nowCup.getName());
        timerScene.draw();

        File file = new File("cups.txt");
        ArrayList<String> lines = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;
            while ((str = br.readLine()) != null)
                lines.add(str);
        } catch (IOException ioe) {
            // TODO: handle exception
            System.out.println(ioe.getMessage());
        }

        try {
            FileWriter fileWriter = new FileWriter(file, false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fileWriter));
            lines.set(0, "SELECTED_CUP:" + nowCup.getName());
            for (String s : lines) {
                pw.println(s);
            }
            pw.close();
            System.out.println(GV.SELECTED_CUP);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void setTimerScene(TimerScene ts) {
        this.timerScene = ts;
    }
}

public class main extends Application {

    /*
     * stgae: シーン遷移のため
     * myEventHandler: 全シーンに共通しているから
     * timerEventHandler :
     * timerScene: タイマーシーン
     * selectScene: セレクトシーン
     */
    Stage baseStage;
    MenuEventHandler myEventHandler;
    TimerEventHandler timerEventHandler;
    TimerScene timerScene;
    SelectScene selectScene;

    @Override
    public void start(Stage stage) {

        GV.getDefultCupNmae();

        
        // メニューバーのイベントハンドラ
        myEventHandler = new MenuEventHandler();

        // タイマーのイベントハンドラ
        timerEventHandler = new TimerEventHandler();
        // タイマーシーンのインスタンス化
        timerScene = new TimerScene();

        timerScene.setHandler(myEventHandler, timerEventHandler);
        baseStage = stage;
        baseStage.setScene(timerScene.getScene());
        baseStage.setTitle("タイマー");
        baseStage.show();

        System.out.println(GV.SELECTED_CUP);

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
                ExpScene settingScene = new ExpScene();
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
                // ファイル読み込みが必要
        selectScene = new SelectScene();


                MenuEventHandler menuEventHandler = new MenuEventHandler();
                selectScene.setHandler(menuEventHandler);
                selectScene.setTimerScene(timerScene);
            
                System.out.println("ファイル再読み込み");

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

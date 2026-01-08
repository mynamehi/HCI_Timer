
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

    public BaseScene() {

        borderPane = new BorderPane();
        root = new VBox();

        borderPane.setTop(createMenuBar());
        borderPane.setCenter(root);
        scene = new Scene(borderPane, 400, 500);

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
        mi3 = new MenuItem("終了");

        mi.setId("home");
        mi1.setId("setting");
        mi2.setId("makeCup");
        mi3.setId("exit");

        menu.getItems().addAll(mi, mi1, mi2, mi3);
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

    private int totalSeconds = 20;// 勉強時間25分
    private int breakSeconds = 10;// 休憩時間5分

    public TimerScene() {

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
            hoge = totalSeconds - n;

        } else if (btnState == BtnState.BREAK) {
            hoge = breakSeconds - n;
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
    ERASER,
    WATER,
    EMPTY,
}

class MakeCupScene extends BaseScene {

    private HBox buttons = new HBox();
    private Button makeShape;
    private Button makeWater;
    private Button finish;
    private Button eraser;
    private final int dot_size = 20;
    private final int dot_len = 20;

    private Pane drawPane;
    private DOT_STATUS mode = DOT_STATUS.DRAW;
    private Color modeColor = Color.BLACK;

    DOT_STATUS[][] dots = new DOT_STATUS[dot_len][dot_len];
    Rectangle[][] rects = new Rectangle[dot_len][dot_len];

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

        drawPane = new Pane();
        drawPane.setPrefSize(dot_size * dot_len, dot_size * dot_size);

        for (int y = 0; y < dot_len; y++) {
            for (int x = 0; x < dot_len; x++) {
                rects[y][x] = new Rectangle(x * dot_size, y * dot_size, dot_size, dot_size);
                drawPane.getChildren().add(rects[y][x]);
                dots[y][x] = DOT_STATUS.EMPTY;
                rects[y][x].setFill(Color.WHITE);
                rects[y][x].setStroke(Color.GRAY);
            }
        }

        drawPane.setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();

            int dx = (int) (x / dot_size);
            int dy = (int) (y / dot_size);
            dots[dy][dx] = mode;
            rects[dy][dx].setFill(modeColor);
        });

        drawPane.setOnMouseDragged((e -> {
            double x = e.getX();
            double y = e.getY();

            int dx = (int) (x / dot_size);
            int dy = (int) (y / dot_size);
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

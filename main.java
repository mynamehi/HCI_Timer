
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

    protected MenuBar createMenuBar() {
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

class MakeCupScene extends BaseScene {

    private HBox buttons = new HBox();
    private Button makeShape;
    private Button makeWater;
    private Button finish;

    private Pane drawPane;

    boolean[][] dots = new boolean[32][32];
    Rectangle[][] rects = new Rectangle[32][32];
    boolean drawMode = true; // true: 描く / false: 消す

    public MakeCupScene() {
        makeShape = new Button("Shape");
        makeWater = new Button("Water");
        finish = new Button("Finish");
        buttons.getChildren().addAll(makeShape, makeWater, finish);

        drawPane = new Pane();
        drawPane.setPrefSize(16 * 20, 16 * 20);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                Rectangle r = new Rectangle(
                        x * 20,
                        y * 20,
                        20,
                        20);
                r.setFill(Color.WHITE);
                r.setStroke(Color.LIGHTGRAY); // グリッド線（不要なら消す）

                final int fx = x;
                final int fy = y;

                r.setOnMousePressed(e -> {
                    drawMode = !dots[fx][fy]; // 描く or 消すを決定
                    dots[fx][fy] = drawMode;
                    r.setFill(drawMode ? Color.BLACK : Color.WHITE);
                });

                

                r.setOnMouseEntered(e -> {
                    // マウスボタンが押されたまま入ってきたか？
                    if (e.isPrimaryButtonDown()) {
                        if (dots[fx][fy] != drawMode) {
                            dots[fx][fy] = drawMode;
                            r.setFill(drawMode ? Color.BLACK : Color.WHITE);
                        }
                    }
                });

                rects[fx][fy] = r;
                drawPane.getChildren().add(r);
            }
        }

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

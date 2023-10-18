import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * ライントレーサーのモデルを表すクラス
 */
public class Model extends Thread implements ChangeListener
{
  /**
   * ライントレーサーのモデルオブジェクトを生成する
   * @param robot   ロボットオブジェクト
   * @param mapName マップ画像のファイル名
   */
  public Model(Robot robot, String mapName)
  {
    try {

      // ロボットオブジェクトを保存
      this.robot = robot;
      // ロボットにモデルを登録
      robot.setModel(this);

      // マップ名を保存
      this.mapName = mapName;
      // マップ画像データの読み込み
      mapImage = ImageIO.read(new File(mapName));
      // 縦・横幅の取得
      mapHeight = mapImage.getHeight();
      mapWidth  = mapImage.getWidth();

      // ロボット画像データの読み込み
      robotImage = ImageIO.read(new File("robot.png"));
      // 縦・横幅の取得
      robotHeight = robotImage.getHeight();
      robotWidth  = robotImage.getWidth();

      // ロボットの初期化
      init();

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  /**
   * 実行前の初期化用関数
   */
  public void init()
  {
    // ロボットの初期デフォルト位置
    robotX   = 200;
    robotY   = 200;
    robotDir = 0.0;

    // マップに応じて初期位置を設定する
    for (int i=0; i < mapNames.length; i++) {
	String[] f = mapName.split("/"); // ディレクトリを考慮する
	String filen = f[f.length - 1]; // ファイル名だけを切り出す
      if (filen.equals(mapNames[i])) {
        robotX   = startX[i];
        robotY   = startY[i];
        robotDir = startDir[i];
      }
    }
    
    // ロボットの移動距離のリセット
    robotRun = robotMiss = 0.0;
  }

  /**
   * 実行用関数
   */
  public synchronized void run()
  {
    while (true) {

      switch (request) {
      case RQ_PLAY:
        // もし停止状態からの繊維であれば，前処理を実行する
        if (status == ST_STOP)
          init();
        
        // 実行状態に遷移
        status = ST_RUNNING;
        // 要求を完遂
        request = RQ_NONE;

        // メイン処理を行う
        try {
          robot.run();
        } catch (InterruptedException e) {
          // 何もしない
        }

        // request == RQ_NONE: robot.run() が無事終了したことを表す
        if (request == RQ_NONE) {
          // 停止する
          status = ST_STOP;
          // 停止ボタンを有効にする
          toolbar.selectStopButton();
        }
        break;
        
      case RQ_PAUSE:
        // 一時停止状態に遷移
        status = ST_SUSPENDED;
        // 要求を完遂
        request = RQ_NONE;
        break;
        
      case RQ_STOP:
        // 停止状態に遷移
        status = ST_STOP;
        // 要求を完遂
        request = RQ_NONE;
        break;

      case RQ_MOVE:
        // ロボット移動状態に遷移
        status = ST_MOVING;
        // 要求を完遂
        request = RQ_NONE;
        break;
      }
      
      // 要求に変更があるまで待機する
      try {
        while (request == RQ_NONE)
          wait();
      } catch (InterruptedException e) {
        // 何もしない
      }
    }
  }

  /**
   * 速度調整＆描画更新
   */
  protected synchronized void delay() throws InterruptedException
  {
    // 描画更新
    if (showView)
      view.repaint();
    // 一定時間眠る
    sleep(delay);
  }

  /**
   * 処理の実行を開始する
   * @return 要求が受け入れられた場合 true を返す
   */
  public boolean requestPlay()
  {
    // 実行開始を要求する
    request = RQ_PLAY;
    // 待機状態にあるかもしれないので起こす
    interrupt();
    return true;
  }

  /**
   * 処理を一時停止する
   * @return 要求が受け入れられた場合 true を返す
   */
  public boolean requestPause()
  {
    if (status == ST_RUNNING) {
      // 一時停止を要求する
      request = RQ_PAUSE;
      // 待機状態にあるかもしれないので起こす
      interrupt();
      return true;
    }
    return false;
  }

  /**
   * 処理を停止する
   * @return 要求が受け入れられた場合 true を返す
   */
  public boolean requestStop()
  {
    if (status == ST_RUNNING || status == ST_SUSPENDED) {
      // 停止を要求する
      request = RQ_STOP;
      // 待機状態にあるかもしれないので起こす
      interrupt();
      return true;
    }
    return false;
  }

  /**
   * 実行速度変更の通知を受け取る
   * @param e イベントオブジェクト
   */
  public void stateChanged(ChangeEvent e) 
  {
    JSlider slider = (JSlider)e.getSource();
    delay = (long)(slider.getMaximum() * 10 - Math.log(slider.getValue() * 10)/Math.log(10) * 333);
  }

  /**
   * ロボットを拾い上げる
   */
  public void pickUpRobot()
  {
    // ロボットを移動中にする
    request = RQ_MOVE;
    // 現在の実行状態をバックアップする
    statusBackup = status;
    // 待機状態にあるかもしれないので起こす
    interrupt();
  }
  
  /**
   * ロボットの向きを設定する
   * @param angle ロボットの向き（度）
   */
  public void setRobotDir(double angle)
  {
    robotDir = angle;
    // 描画更新
    if (showView)
      view.repaint();
    // 状態を移動中にする（開始時に init() を実行されることを防ぐため）
    if (status == ST_STOP)
      status = ST_MOVING;
  }
  
  /**
   * ロボットを指定座標に移動させる
   * @param x ロボットの新しいＸ座標
   * @param y ロボットの新しいＹ座標
   */
  public void moveRobot(int x, int y)
  {
    // ロボットを移動
    robotX = x;
    robotY = y;
    // 描画更新
    if (showView)
      view.repaint();
  }
  
  /**
   * ロボットを指定座標におろす
   * @param x ロボットの新しいＸ座標
   * @param y ロボットの新しいＹ座標
   */
  public void putDownRobot(int x, int y)
  {
    // ロボットを移動
    moveRobot(x, y);
    // 元の状態に応じて復帰させる
    switch (statusBackup) {
    case ST_RUNNING:   requestPlay();  break;
    case ST_SUSPENDED: requestPause(); break;
    case ST_STOP:      requestStop();  break;      
    }
  }
  
  /**
   * 画面を描画するかどうか指定する
   * @param flag 描画する場合は true を指定する
   */
  public void setVisibility(boolean flag)
  {
    // 描画するかどうかを保存する
    showView = flag;
    // 描画するかどうかを設定する
    view.setVisibility(flag);
    // 画面を消すために一度だけ描画する
    view.repaint();  
  }
  
  /**
   * 制御用のツールバーを登録
   */
  public void setToolbar(ControlToolBar toolbar)
  {
    this.toolbar = toolbar;
  }
  
  /**
   * 描画用のビューを登録
   */
  public void setView(View view)
  {
    this.view = view;
  }
  
  /**
   * マップ画像データを取得する
   * @return マップ画像データ
   */
  public Image getMapImage()
  {
    return mapImage;
  }

  /**
   * ロボットの画像データを取得する
   * @return ロボットの画像データ
   */
  public Image getRobotImage()
  {
    return robotImage;
  }

  /**
   * ロボットのサイズを取得する（ロボット画像は縦横が等しいとする）
   * @return ロボットのサイズ
   */
  public int getRobotSize()
  {
    return robotImage.getWidth();
  }

  /**
   * ロボットの X 座標を取得する
   * @return ロボットの X 座標
   */
  public double getRobotX()
  {
    return robotX;
  }
  
  /**
   * ロボットの Y 座標を取得する
   * @return ロボットの Y 座標
   */
  public double getRobotY()
  {
    return robotY;
  }
  
  /**
   * ロボットの向きを取得する
   * @return ロボットの向き（１２時方向が 0 度）
   */
  public double getRobotDir()
  {
    return robotDir;
  }

  /**
   * ロボットの走行距離を取得する
   * @return ロボットの走行距離(cm)
   */
  public double getRobotRun()
  {
    return robotRun;
  }
  
  /**
   * ロボットのミスをした走行距離を取得する
   * @return ロボットのミスをした走行距離(cm)
   */
  public double getRobotMiss()
  {
    return robotMiss;
  }
  
  /**
   * ロボットを指定距離前進させる
   * @param cm 指定距離(cm)
   */
  public void forwardRobot(double cm)
  {
    // 向きをラジアンに直す
    double rad = Math.toRadians(robotDir);

    // 仮想環境上での移動距離
    double px = cm / cmPerPixel;
    
    // X 軸方向の移動量決定
    double x = Math.sin(rad) * px;
    // Y 軸方向の移動量決定
    double y = Math.cos(rad) * px;

    // 1cm 刻みで移動させてライン上かどうかチェックする
    double unit = (cm > 0.0) ? +1.0 : -1.0;
    double curr = 0.0;

    // 実際には，誤差を減らすためいっきに移動させるので元座標を保存
    double orgX = robotX;  
    double orgY = robotY;

    while (true) {
      // 単位距離移動する．もし指定距離を以上ならば指定距離ちょうどにする
      if (Math.abs(curr + unit) < Math.abs(cm)) {
        curr += unit;
      }
      else {
        unit = cm - curr;
        curr = cm;
      }
      robotX += Math.sin(rad) * (unit / cmPerPixel);
      robotY -= Math.cos(rad) * (unit / cmPerPixel);

      if (!isOnLine())
        robotMiss += unit;

      if (curr == cm)
        break;
    }
    
    // 移動する
    robotX = orgX + x;
    robotY = orgY - y;

    // 移動距離を記録
    robotRun += cm;
  }
  
  /**
   * ロボットを回転させる
   * @param angle 回転角度(度)．時計回転の場合は正の値を，半時計回転の場合は負の値を指定す
   * る
   */
  public void rotateRobot(double angle)
  {
    // 回転する
    robotDir += angle;

    // 移動距離を記録（ロボットの回転半径は 5.5cm）
    double circum = (2 * Math.PI * 5.5) * (Math.abs(angle) / 360);
    robotRun += circum;

    // ライン上でなければ，ミスとして記録
    if (!isOnLine())
      robotMiss += circum;
  }
  
  /**
   * ロボットの光センサを使って色を読み取る
   * @param 光センサ番号
   * @return 色
   */
  public int getColor(int lightNo)
  {
    // ロボットの中心を基点に回転するアフィン変換の生成
    AffineTransform xform = new AffineTransform();
    xform.translate(robotX, robotY);
    xform.rotate(Math.toRadians(robotDir));

    // 各センサの位置
    Point2D pos = null;
    switch (lightNo) {
    case Robot.LIGHT_A: pos = new Point2D.Double(+10, -20); break;
    case Robot.LIGHT_B: pos = new Point2D.Double(  0, -20); break;
    case Robot.LIGHT_C: pos = new Point2D.Double(-10, -20); break;
    }

    // 各センサ位置の算出
    xform.transform(pos, pos);
    int x = (int)pos.getX();
    int y = (int)pos.getY();
    
    // 指定位置の色を取得
    return getColor(x, y);
  }
  
  /**
   * 指定座標の色を取得する
   * @param x Ｘ座標
   * @param y Ｙ座標
   * @return 色
   */
  private int getColor(int x, int y)
  {
    // 範囲外の場合は白とする
    if (x < 0 || x >= mapWidth ||
        y < 0 || y >= mapHeight)
      return Robot.WHITE;

    // ARGB を取得
    int argb = mapImage.getRGB(x, y);
    // RGB を抽出
    int r = (argb & 0x00ff0000) >> 16;
    int g = (argb & 0x0000ff00) >>  8;
    int b = (argb & 0x000000ff);

    // 誤差修正
    int rgb = 0;
    if (r >= 128) rgb |= 0xff0000;
    if (g >= 128) rgb |= 0x00ff00;
    if (b >= 128) rgb |= 0x0000ff;

    // 色判定
    switch (rgb) {
    case 0x000000: return Robot.BLACK;
    case 0xffffff: return Robot.WHITE;
    case 0x00ff00: return Robot.GREEN;
    }
      
    return Robot.UNKNOWN_COLOR;
  }
  
  /**
   * ロボットがライン上か判定する
   * @return ロボットがライン上の場合は true を返す
   */
  public boolean isOnLine()
  {
    // ロボットの大きさは 60x60 （画像は 70x70 だが周りは余白）．中心を原点として，
    // (1) (-30,0) 〜 (+30,0) まで５ドット刻み
    // のいずれかがライン上であれば OK とする
    for (int dx=-30; dx != +30; dx+=5) {

      // 本来はロボットの向きに合わせて座標を回転させる必要があるが，
      // 境界の判定なのでよしとする
      int x = (int)(robotX + dx);
      int y = (int)(robotY +  0);
      
      // 色が黒ならライン上
      if (getColor(x, y) == Robot.BLACK)
        return true;
    }

    // ロボットの大きさは 60x60 （画像は 70x70 だが周りは余白）．中心を原点として，
    // (2) (0,+30) 〜 (0,-30) まで５ドット刻み
    // のいずれかがライン上であれば OK とする
    for (int dy=-30; dy != +30; dy+=5) {

      // 本来はロボットの向きに合わせて座標を回転させる必要があるが，
      // 境界の判定なのでよしとする
      int x = (int)(robotX +  0);
      int y = (int)(robotY + dy);
      
      // 色が黒ならライン上
      if (getColor(x, y) == Robot.BLACK)
        return true;
    }

    return false;    
  }
  
  /** ロボットオブジェクトへの参照 */
  private Robot robot = null;

  /** マップファイルの名前 */
  private String mapName = null;
  /** マップの画像データ */
  private BufferedImage mapImage = null;
  /** マップの高さ */
  private int mapHeight = 0;
  /** マップの横幅 */
  private int mapWidth = 0;

  /** マップの名前配列 */
  private String[] mapNames = { "map1-rect.png", "map2-circ.png", "map3-grid.png", "map4-grid.png",
                                "map5-motegi.png", "map6-monte.png", "map7-fuji.png", "map8-suzuka.png" };
  /** マップごとのロボットの開始Ｘ座標 */
  private double[] startX = { 330, 450,  88, 135, 410, 94, 410, 580 };
  /** マップごとのロボットの開始Ｙ座標 */
  private double[] startY = { 130, 138, 450, 480, 435, 320, 205, 117 };
  /** マップごとのロボットの開始時の向き */
  private double[] startDir = { 90, 110, 0, 90, 95, 10, 90, 90 };
    
  /** ロボットの画像データ */
  private BufferedImage robotImage = null;
  /** ロボットの高さ */
  private int robotHeight = 0;
  /** ロボットの横幅 */
  private int robotWidth = 0;
  /** ロボットの X 座標 */
  private double robotX = 0.0;
  /** ロボットの Y 座標 */
  private double robotY = 0.0;
  /** ロボットの向き（１２時方向が 0 度） */
  private double robotDir = 0.0;
  /** ロボットの走行距離 */
  private double robotRun = 0.0;
  /** ロボットのミスをした走行距離 */
  private double robotMiss = 0.0;
  
  /** 描画用オブジェクト */
  private View view = null;
  /** 画面を描画するかどうか */
  private boolean showView = true;

  /** 実環境から仮想環境での単位変換 (cm/px) */
  private final static double cmPerPixel = 0.225;
  
  /** 制御用ツールバーオブジェクト */
  private ControlToolBar toolbar = null;

  /** 実行状態：実行中 */
  private final static int ST_RUNNING = 1;
  /** 実行状態：一時停止中 */
  private final static int ST_SUSPENDED = 2;
  /** 実行状態：停止中 */
  private final static int ST_STOP = 3;
  /** 実行状態：ロボット移動中 */
  private final static int ST_MOVING = 4;
  /** 実行状態 */
  private int status = ST_STOP;
  /** 実行状態のバックアップ */
  private int statusBackup = ST_STOP;
  
  /** 要求状態：なし */
  private final static int RQ_NONE = 0;
  /** 要求状態：実行せよ */
  private final static int RQ_PLAY = 1;
  /** 要求状態：一時停止せよ */
  private final static int RQ_PAUSE = 2;
  /** 要求状態：停止せよ */
  private final static int RQ_STOP = 3;
  /** 要求状態：ロボットを移動させよ */
  private final static int RQ_MOVE = 4;
  /** 要求状態 */
  private int request = RQ_NONE;
  
  /** 遅延時間 (msec) */
  private long delay = 100;
}

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * 描画用のビュークラス
 */
public class View extends JPanel 
{
  /**
   * 描画用オブジェクトの生成
   * @param model モデルオブジェクト
   */
  public View(Model model)
  {
    // モデルデータへの参照を保存
    this.model = model;

    // 背景画像の取得
    mapImage = model.getMapImage();
    // ロボット画像の取得
    robotImage = model.getRobotImage();
    // ロボットの大きさ取得（縦横等しいとする）
    robotSize = model.getRobotSize();

    // ロボットカーソル（ライン上）の画像生成
    onLineImage = new BufferedImage(robotSize, robotSize, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = onLineImage.createGraphics();
    // 円の生成
    Ellipse2D circle = new Ellipse2D.Float(5, 5, robotSize-10, robotSize-10);
    // ストロークの生成
    Stroke stroke = new BasicStroke(8);
    // ストロークの設定
    g.setStroke(stroke);
    // 色の設定
    g.setPaint(new Color(0, 0, 255, 150));
    // 描画
    g.draw(circle);
    
    // ロボットカーソル（ライン外）の画像生成
    offLineImage = new BufferedImage(robotSize, robotSize, BufferedImage.TYPE_INT_ARGB);
    g = offLineImage.createGraphics();
    // ストロークの設定
    g.setStroke(stroke);
    // 色の設定
    g.setPaint(new Color(255, 0, 0, 150));
    // 描画
    g.draw(circle);
    
    // ビューの適切なサイズを設定する
    width  = mapImage.getWidth(this);
    height = mapImage.getHeight(this);
    setPreferredSize(new Dimension(width, height));

    // マウスイベントのリスナを登録する
    EventChecker checker = new EventChecker();
    addMouseListener(checker);
    addMouseMotionListener(checker);
  }

  /**
   * 画面を描画するかどうか指定する
   * @param flag 描画する場合は true を指定する
   */
  public void setVisibility(boolean flag)
  {
    visibility = flag;
  }
  
  /**
   * 描画する
   * @param g グラフィックスオブジェクト
   */
  public void paintComponent(Graphics g)
  {
    // 描画しない場合は，白紙にする
    if (!visibility) {
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, width, height);
      return;
    }
    
    // マップ画像データを描画
    g.drawImage(mapImage, 0, 0, this);

    //
    // ロボット画像データを描画
    //

    // ロボットの現在座標と向きを取得
    double x   = model.getRobotX();
    double y   = model.getRobotY();
    double dir = model.getRobotDir();

    // アフィン変換オブジェクトの生成
    AffineTransform xform = new AffineTransform();

    // ロボットの描画位置（左上座標）へ移動
    xform.translate(x - (robotSize>>1), y - (robotSize>>1));

    // ロボットの向きに合わせて回転
    xform.rotate(Math.toRadians(dir), (robotSize>>1), (robotSize>>1));

    // カーソルの描画
    if (model.isOnLine())
      ((Graphics2D)g).drawImage(onLineImage, xform, this);
    else
      ((Graphics2D)g).drawImage(offLineImage, xform, this);

    // MindStorms画像の描画
    ((Graphics2D)g).drawImage(robotImage, xform, this);

    // 移動距離の表示
    double run  = (int)(model.getRobotRun()  * 10) / 10.0;
    double miss = (int)(model.getRobotMiss() * 10) / 10.0;
    g.setFont(font);
    //g.setColor(fontColor);
    g.setColor(Color.BLUE);
    g.drawString("Run: " + run + "cm" , 10, 40);
    g.setColor(Color.RED);
    g.drawString("Miss: " + miss + "cm", 10, 80);
    
//     // デバッグ：中心座標の描画
//     g.setColor(Color.YELLOW);
//     g.fillOval((int)x - 3, (int)y - 3, 6, 6);

//     // デバッグ：センサー位置の描画
//     xform = new AffineTransform();
//     xform.translate(x, y);
//     xform.rotate(Math.toRadians(dir));
//     Point2D posA = new Point2D.Double(-10, -20);
//     Point2D posB = new Point2D.Double(  0, -20);
//     Point2D posC = new Point2D.Double(+10, -20);
//     xform.transform(posA, posA);
//     xform.transform(posB, posB);
//     xform.transform(posC, posC);
//     g.setColor(Color.CYAN);
//     g.fillOval((int)posA.getX() - 3, (int)posA.getY() - 3, 6, 6);
//     g.setColor(Color.RED);
//     g.fillOval((int)posB.getX() - 3, (int)posB.getY() - 3, 6, 6);
//     g.setColor(Color.PINK);
//     g.fillOval((int)posC.getX() - 3, (int)posC.getY() - 3, 6, 6);
  }

  /**
   * マウスイベントを扱う内部クラス
   */
  class EventChecker extends MouseInputAdapter
  {
    /**
     * マウスボタンが押された場合に呼び出される．左クリックの場合，もしマウスがロボットの位
     * 置にあればロボットを拾い上げる．右クリックの場合，ロボットを回転させる．
     * @param e マウスイベント
     */
    public void mousePressed(MouseEvent e) {
      int mx = e.getX();
      int my = e.getY();
      int rx = (int)model.getRobotX();
      int ry = (int)model.getRobotY();

      // 右クリックの場合，角度指定
      if (e.getButton() == MouseEvent.BUTTON3) {
        // ロボットを回転させる
        model.setRobotDir(Math.toDegrees(Math.atan2(mx - rx, ry - my)));
        // ロボットを回転させている
        rotated = true;
      }

      // 左クリックの場合，かつ，マウスカーソルがロボットの位置にある場合
      if (e.getButton() == MouseEvent.BUTTON1 &&
          Math.abs(rx - mx) < robotSize/2 && Math.abs(ry - my) < robotSize/2) {
        // マウスカーソルとロボット中心との誤差を記録
        diffX = rx - mx;
        diffY = ry - my;
        // モデルにロボットを拾い上げることを通知
        model.pickUpRobot();
        // ロボットを拾い上げ中
        pickedUp = true;
      }
    }

    /**
     * ドラッグ中に呼び出される
     * @param e マウスイベント
     */
    public void mouseDragged(MouseEvent e) {
      // ロボットを拾い上げている場合
      if (pickedUp) {
        // ロボットを移動させる
        model.moveRobot(e.getX() + diffX, e.getY() + diffY);
      }
      // ロボットを回転させている場合
      if (rotated) {
        // ロボットを回転させる
        int mx = e.getX();
        int my = e.getY();
        int rx = (int)model.getRobotX();
        int ry = (int)model.getRobotY();
        model.setRobotDir(Math.toDegrees(Math.atan2(mx - rx, ry - my)));
        //System.out.println("angle = " + angle);
      }
    }

    /**
     * マウスボタンが離れたときに呼び出される
     * @param e マウスイベント
     */
    public void mouseReleased(MouseEvent e) {
      // ロボットを拾い上げている場合
      if (e.getButton() == MouseEvent.BUTTON1 && pickedUp) {
        // ロボットをおろす
        model.putDownRobot(e.getX() + diffX, e.getY() + diffY);
        // ロボットをおろした
        pickedUp = false;
      }
      // ロボットを回転させている場合
      if (rotated) {
        // ロボットを回転させる
        int mx = e.getX();
        int my = e.getY();
        int rx = (int)model.getRobotX();
        int ry = (int)model.getRobotY();
        model.setRobotDir(Math.toDegrees(Math.atan2(mx - rx, ry - my)));
        // ロボットの回転終了
        rotated = false;
      }
      
    }

    /** ロボットを拾い上げているかどうか */
    private boolean pickedUp = false;
    /** ロボットを拾い上げたときのロボット中心とマウスカーソルとの誤差（Ｘ座標） */
    private int diffX = 0;
    /** ロボットを拾い上げたときのロボット中心とマウスカーソルとの誤差（Ｙ座標） */
    private int diffY = 0;
    /** ロボットを回転させているかどうか */
    private boolean rotated = false;
  }  
  
  /** モデルデータ */
  private Model model = null;

  /** マップの画像データ */
  private Image mapImage = null;
  /** マップの横方向のサイズ */
  private int width = 0;
  /** マップの縦方向のサイズ */
  private int height = 0;

  /** ロボットの画像データ */
  private Image robotImage = null;
  /** ロボットのサイズ */
  private int robotSize = 0;
  /** ロボットカーソルの画像データ（ライン上） */
  private BufferedImage onLineImage = null;
  /** ロボットカーソルの画像データ（ライン外） */
  private BufferedImage offLineImage = null;

  /** 描画用のフォント */
  private Font font = new Font("SansSerif", Font.BOLD, 30);
  /** 描画用のフォントの色 */
  private Color fontColor = new Color(0, 200, 0);
  
  /** 画面を描画するかどうか */
  private boolean visibility = true;
}

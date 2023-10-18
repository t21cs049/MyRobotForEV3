/**
 * ロボットを表す抽象クラス
 */
public abstract class Robot
{

  /**
   * ロボットオブジェクトの生成
   */
  public Robot()
  {
  }

  /**
   * モデルを登録する
   * @param model モデルオブジェクト
   */
  public void setModel(Model model)
  {
    this.model = model;
  }
  
  /**
   * 実行前の初期化用関数
   */
  public void init()
  {
    model.init();
  }
  
  /**
   * 実行用関数
   */
  public abstract void run() throws InterruptedException;
  
  /**
   * 一定時間眠る
   */
  public void delay() throws InterruptedException
  {
    model.delay();
  }
  
  /**
   * 指定距離前進する
   * @param cm 指定距離(cm)
   */
  public void forward(double cm)
  {
    model.forwardRobot(cm);
  }
  
  /**
   * 指定距離後進する
   * @param cm 指定距離(cm)
   */
  public void backward(double cm)
  {
    model.forwardRobot(-cm);
  }
  
  /**
   * 回転する
   * @param angle 回転角度(度)．時計回転の場合は正の値を，半時計回転の場合は負の値を指定す
   * る
   */
  public void rotate(double angle)
  {
    model.rotateRobot(angle);
  }
  
  /**
   * 右に回転する
   * @param angle 回転角度(度)．正の値を指定する
   */
  public void rotateRight(double angle)
  {
    model.rotateRobot(angle);
  }
  
  /**
   * 左に回転する
   * @param angle 回転角度(度)．正の値を指定する
   */    
  public void rotateLeft(double angle)
  {
    model.rotateRobot(-angle);
  }
  
  /**
   * 光センサを使って色を読み取る
   * @param 光センサ番号
   * @return 色番号
   */
  public int getColor(int lightNo)
  {
    return model.getColor(lightNo);
  }

  /**
   * ゴールに到達したか判定する
   * @return ゴールに到達している場合 true を返す
   */
  public boolean isOnGoal()
  {
    boolean ongoal = getColor(LIGHT_A) == GREEN || getColor(LIGHT_B) == GREEN || getColor(LIGHT_C) == GREEN;

    // もしゴールしたならば，走行距離とミスを表示する
    if (ongoal) {
      double run  = (int)(model.getRobotRun()  * 10) / 10.0;
      double miss = (int)(model.getRobotMiss() * 10) / 10.0;
      System.out.println(" Run: " + run + "cm");
      System.out.println(" Miss: " + miss + "cm");
    }
    
    return ongoal;
  }
  
  /** 光センサＡを表す定数 */
  public final static int LIGHT_A = 0;
  /** 光センサＢを表す定数 */
  public final static int LIGHT_B = 1;
  /** 光センサＣを表す定数 */
  public final static int LIGHT_C = 2;

  /** 白色を表す定数 */
  public final static int WHITE = 0;
  /** 緑色を表す定数 */
  public final static int GREEN = 1;
  /** 黒色を表す定数 */
  public final static int BLACK = 2;
  /** 不明な色を表す定数 */
  public final static int UNKNOWN_COLOR = 5;

  /** モデルオブジェクトへの参照 */
  private Model model = null;
}

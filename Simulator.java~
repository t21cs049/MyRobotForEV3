import java.awt.*;
import javax.swing.*;
import java.lang.reflect.*;


/**
 * シミュレータクラス
 */
public class Simulator extends JFrame
{
  /**
   * シミュレータの初期設定.
   * 各パネルの生成及びリンク.
   * @param className 動作プログラム名(クラス名)
   * @param mapName   マップ画像のファイル名
   */
  public Simulator(String className, String mapName) {
    
    // ウィンドウタイトルの登録
    setTitle("MindStorms NXT Simulator");
    // ウィンドウを閉じたときに終了する
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    try {

      // 指定されたクラス名からクラスを生成
      Class c = Class.forName(className);
      // クラスからロボットオブジェクトを生成
      Robot robot = (Robot)c.newInstance();

      // ライントレーサーモデルオブジェクトの生成
      Model model = new Model(robot, mapName);

      // 描画画面の生成
      View view = new View(model);
      // モデルに描画オブジェクトを登録
      model.setView(view);
      // 描画画面を中央に配置
      getContentPane().add(view, BorderLayout.CENTER);

      // 制御ツールバーを生成
      ControlToolBar toolbar = new ControlToolBar(model);
      // モデルにツールバーオブジェクトを登録
      model.setToolbar(toolbar);
      // ツールバーを上部に配置
      getContentPane().add(toolbar, BorderLayout.NORTH);

      // サブコンポーネントの推奨サイズに合わせて、ウィンドウをリサイズ
      pack();
      
      // ウィンドウを表示する
      setVisible(true);

      // モデルを並行して実行する
      model.start();

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }    
  }
  
  /**
   * 起動用 main 関数
   * @param arg[] コマンドライン引数の配列
   */
  public static void main(String[] args) {

    // コマンドライン引数のチェック
    if (args.length == 0) {
      System.out.println("Usage: java Simulator ROBOT_CLASSNAME MAPFILE");
      System.exit(-1);
    }

    // シミュレータの生成＆実行
    Simulator sim = new Simulator(args[0],args[1]);
  }
  
}

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * 制御用ツールバー
 */
public class ControlToolBar extends JToolBar {

  /**
   * ツールバーを生成する
   * @param model モデルオブジェクト
   */
  public ControlToolBar(Model model)
  {
    // モデルデータへの参照を保存
    this.model = model;

    Class     c = getClass();
    ImageIcon icon;
    Insets    space = new java.awt.Insets(1,1,1,1);
    
    // 実行ボタン
    icon = new ImageIcon(c.getResource("icon-play.png"));
    play = new JToggleButton(icon);
    play.setMargin(space);
    play.setToolTipText("Start");
    play.addActionListener(buttonListener);
    play.setSelected(false);
    add(play);
    // 一時停止ボタン
    icon = new ImageIcon(c.getResource("icon-pause.png"));
    pause = new JToggleButton(icon);
    pause.setToolTipText("Pause");
    pause.setMargin(space);
    pause.addActionListener(buttonListener);
    pause.setSelected(false);
    add(pause);
    // 停止ボタン
    icon = new ImageIcon(c.getResource("icon-stop.png"));
    stop = new JToggleButton(icon);
    stop.setToolTipText("Stop");
    stop.setMargin(space);
    stop.addActionListener(buttonListener);
    stop.setSelected(true);
    add(stop);
    // 実行速度変更スライダー
    speedController = new JSlider(1, 100);
    speedController.setMajorTickSpacing(25);
    speedController.setMinorTickSpacing(5);
    speedController.setPaintTicks(true);
    speedController.addChangeListener(model);
    add(speedController);
    // 画面表示ボタン
    icon = new ImageIcon(c.getResource("icon-hide.png"));
    show = new JToggleButton(icon);
    icon = new ImageIcon(c.getResource("icon-show.png"));
    show.setSelectedIcon(icon);
    show.setToolTipText("Show");
    show.setMargin(space);
    show.addActionListener(buttonListener);
    show.setSelected(true);
    add(show);
  }
  
  /**
   * ストップボタンを選択状態にする．これは，実行したプログラムが終了
   * し停止することがあるため．イベントは発生しない．
   */
  public void selectStopButton()
  {
    play.setSelected(false);
    pause.setSelected(false);
    stop.setSelected(true);
    status = ST_STOP;
  }

  /**
   * ボタンイベントリスナー
   */
  class ButtonListener implements ActionListener {

    /** 
     * イベント処理
     * @param e イベント
     */
    public void actionPerformed(ActionEvent e)
    {
      Object  src       = e.getSource();
      boolean successed = false;

      // メッセージの通知
      if (src == play && model.requestPlay()) 
        status = ST_PLAY;
      else if (src == pause && model.requestPause()) 
        status = ST_PAUSE;
      else if (src == stop && model.requestStop()) 
        status = ST_STOP;
      else if (src == show) {
        if (show.isSelected()) {
          // 描画する場合
          show.setSelected(true);
          model.setVisibility(true);
          // 実行速度を元に戻す
          if (speedController.getValue() == speedController.getMaximum())
            speedController.setValue(speedBackup);
        }
        else {
          // 描画しない場合
          show.setSelected(false);
          model.setVisibility(false);
          // 実行速度をバックアップし，最速に設定する
          speedBackup = speedController.getValue();
          speedController.setValue(speedController.getMaximum());
        }
      }
      
      // 排他的 Toggle 処理
      switch (status) {
      case ST_PLAY:
        play.setSelected(true);
        pause.setSelected(false);
        stop.setSelected(false);
        break;

      case ST_PAUSE:
        play.setSelected(false);
        pause.setSelected(true);
        stop.setSelected(false);
        break;

      case ST_STOP:
        play.setSelected(false);
        pause.setSelected(false);
        stop.setSelected(true);
        break;
      }
    }
  }

  /** 状態 */
  private int status = ST_STOP;
  /** 実行速度のバックアップ */
  private int speedBackup = 0;

  /** 状態：実行状態 */
  private final static int ST_PLAY = 1;
  /** 状態：一時停止状態 */
  private final static int ST_PAUSE = 2;
  /** 状態：停止状態 */
  private final static int ST_STOP = 3;

  /** 実行ボタン */
  private JToggleButton play = null;
  /** 一時停止ボタン */
  private JToggleButton pause = null;
  /** 停止ボタン */
  private JToggleButton stop = null;
  /** 実行速度変更スライダー */
  private JSlider speedController = null;
  /** 画面表示ボタン */
  private JToggleButton show = null;

  /** ボタンアクションリスナー */
  private ActionListener buttonListener = new ButtonListener();

  /** モデルデータ */
  private Model model = null;
}

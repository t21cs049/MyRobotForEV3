/**
 * ロボットクラスの作成例：単純なライントレーサーロボット
 */
public class MyRobot extends Robot {
	/**
	 * 実行用関数
	 */
	private int lastDirect = 0; // A -> 1 C -> 2

	public void run() throws InterruptedException {
		while (true) {
			// 線を見失ったとき用に最後のLIGHTの情報を保持する

			// デバッグ用
			System.out.println("A:" + getColor(LIGHT_A) + " B:" + getColor(LIGHT_B) + " C:" + getColor(LIGHT_C));

			// 右センサの色に応じて分岐
			switch (getColor(LIGHT_A)) {

			case BLACK:
				// 黒を検知 => 右回転 => 前進
				lastDirect = 1;
				rotateRight(10);
//        forward(1);
				break;

//      case WHITE:
//        // 白を検知 => 左回転 => 前進
//        rotateLeft(10);
//        forward(1);
//        break;

			}

			// 右センサの色に応じて分岐
			switch (getColor(LIGHT_C)) {

			case BLACK:
				// 黒を検知 => 左回転 => 前進

				lastDirect = 2;
				rotateLeft(10);
				break;

			}

			if (getColor(LIGHT_A) == WHITE && getColor(LIGHT_B) == WHITE && getColor(LIGHT_C) == WHITE) {
				switch (lastDirect) {

				case 1:
					rotateRight(30);
					break;
					
				case 2:
					rotateLeft(30);
					break;

				}
				forward(10);
			}

			forward(1);

			// 速度調整＆画面描画
			delay();

			// ゴールに到達すれば終了
			if (isOnGoal())
				return;
		}
	}

}

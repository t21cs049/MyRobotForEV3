/**
 * ロボットクラスの作成例：単純なライントレーサーロボット
 */
public class MyRobot extends Robot {
	/**
	 * 実行用関数
	 */
	private int statesNumber = 8;
	private int actionNumber = 7;

	public void run() throws InterruptedException {
		try {
			// step 1: Q学習する
			QLearning q1 = new QLearning(statesNumber, actionNumber, 0.5, 0.5);

			int trials = 100; // 強化学習の試行回数
			int steps = 1000; // １試行あたりの最大ステップ数
			for (int t = 1; t <= trials; t++) { // 試行回数だけ繰り返し
				/* ロボットを初期位置に戻す */
				init();

				for (int s = 0; s < steps; s++) { // ステップ数だけ繰り返し
					/* ε-Greedy 法により行動を選択 */

					// ロボットとラインの位置関係から状態情報を取得する
					int state = judgeState();
					// epsilonを設定する
					double epsilon = 0.5;
					// 今のロボットの状態から適切な行動を選択する
					int action = q1.selectAction(state, epsilon, actionNumber);
					/* 選択した行動を実行 (ロボットを移動する) */
					doAction(action);
					/* 新しい状態を観測＆報酬を得る */

					// 更新した後のロボットの座標における状態状態を取得する
					int after = judgeState();
					// 選択した行動における報酬を報酬関数から得る
					int reward = judgeReward();
					/* Q 値を更新 */

					// デバック用：学習状況を分かりやすいように出力
					// System.out.println("s:" + s + " t:" + t);

					// 時間差分方程式によってQＴａｂｌｅを更新する
					q1.update(state, action, after, reward);

					/* もし時間差分誤差が十分小さくなれば終了 */

				}
			}
			System.out.println("Learning ENDS!");
			// step 2: 学習したQテーブルの最適政策に基づいて
			// スタート位置からゴール位置まで移動
			/* ロボットを初期位置に戻す */
			init();
			q1.showQTable();
			while (true) {
				// 線を見失ったとき用に最後のLIGHTの情報を保持する

				// デバッグ用
//				System.out.println("A:" + getColor(LIGHT_A) + " B:" + getColor(LIGHT_B) + " C:" + getColor(LIGHT_C));

				// 右センサの色に応じて分岐
				doAction(q1.selectAction(judgeState()));

				forward(1);

				// 速度調整＆画面描画
				delay();

				// ゴールに到達すれば終了
				if (isOnGoal())
					return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	// 報酬関数を定義
	private int judgeReward() {
		// 現在の座標がブロック上であれば、今後選択しないようにマイナスの大きい値を与える
		if (isOnGoal())
			return 1000;
		// 現在の座標がゴール上であれば、ゴールへは最優先で向かってほしいため、非常に大きい正の値を与える
		if (getColor(LIGHT_A) == WHITE && getColor(LIGHT_B) == WHITE && getColor(LIGHT_C) == WHITE)
			return -100;
		
		if(getColor(LIGHT_A)+getColor(LIGHT_B)+getColor(LIGHT_C) <= 2 && getColor(LIGHT_B) == WHITE)
			return 1;

		// ゴールと現在位置のユークリッド距離から、ゴールに近ければ報酬は大きく、遠くなれば報酬は小さくなるような関数を設定
		// 分子の３００は報酬の大きさを調整
		// 分母の「＋１」は分母が０になることを防ぐため
		return 10;
	}

	// 選択された行動に応じたロボットの座標更新を行う
	private void doAction(int action) {
		// マップの外の座標に更新しないように設定
		if (action == 0)
			rotateRight(10);
		if (action == 1)
			rotateRight(20);
		if (action == 2)
			rotateRight(30);
		if (action == 3)
			rotateLeft(10);
		if (action == 4)
			rotateLeft(20);
		if (action == 5)
			rotateLeft(30);
		if (action == 6)
			;
	}

	// 光センサーの値の組み合わせを一意の状態に対応付ける為の関数
	private int judgeState() {
		///
		int sum = 0;
		if(getColor(LIGHT_C) == BLACK)
			sum += 4;
		if(getColor(LIGHT_B) == BLACK)
			sum += 2;
		if(getColor(LIGHT_A) == BLACK)
			sum += 1;
		
		return sum;
	}

}

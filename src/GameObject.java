import java.awt.Graphics;

public abstract class GameObject implements Updatable, Renderable {
    // 座標の管理（外部から勝手に書き換えられないよう private にする）
    private double currentX, currentY;
    private double prevX, prevY;

    // 子クラスが実装する純粋なゲームロジック
    protected abstract void onUpdate();

    protected abstract void onDraw(Graphics g);

    @Override
    public final void update() {
        // ① まず、今の位置を「過去の位置」として保存する
        prevX = currentX;
        prevY = currentY;

        // ② 子クラス独自のロジック（移動など）を実行させる
        onUpdate();
    }

    @Override
    public final void draw(Graphics g, double alpha) {
        // ① 親クラスが自動でLerp（線形補間）座標を計算する
        double drawX = prevX + (currentX - prevX) * alpha;
        double drawY = prevY + (currentY - prevY) * alpha;

        // ② キャンバスの原点(0, 0)を、今計算したLerp座標にワープさせる（超重要！）
        g.translate((int) drawX, (int) drawY);

        try {
            // ③ 子クラスの描画処理を呼び出す
            onDraw(g);
        } finally {
            // ④ 他のオブジェクトに影響が出ないよう、原点を元の位置に戻す
            g.translate(-(int) drawX, -(int) drawY);
        }
    }

    // --- 子クラスが座標を操作するための便利なメソッド ---
    public void move(double dx, double dy) {
        this.currentX += dx;
        this.currentY += dy;
    }

    public void setPosition(double x, double y) {
        this.currentX = x;
        this.currentY = y;
        this.prevX = x;
        this.prevY = y; // 生まれた瞬間にLerpが暴れないよう同期する
    }
}
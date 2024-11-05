package fun.feellmoose.core;

import java.util.List;

public interface IGame {
    IUnit[][] units();  // 获取单元格
    List<Step> steps();  // 历史记录
    List<Step> mines();  // 获取雷   游戏结束调用
    List<Step> flags();  // 获取哪些插过旗

    boolean onTyped(int x, int y);  // 普通点击

    boolean onFlag(int x, int y);  // 插旗

    Status status();  // 游戏状态

    boolean isWin();  // 游戏是否胜利

    enum Status {
        Init, Running, End
    }
}

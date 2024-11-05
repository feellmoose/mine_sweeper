package fun.feellmoose.core;

public interface IUnit {
    int num();

    Status status();
    
    // 被点击后用户可以看到数字大于 0，空格是 -1，旗子是 -2。
    default int getFilteredNum() {
        return switch (status()) {
            case Typed -> num();
            case None -> -1;
            case Flag -> -2;
        };
    }

    enum Status {
        Typed, Flag, None
    }
}

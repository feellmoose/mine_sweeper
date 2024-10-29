package fun.feellmoose.core;

import lombok.Getter;
import lombok.Setter;


public class Unit implements IUnit{
    private final int num;
    @Getter
    private final boolean mine;
    @Setter
    private Status status;

    private Unit(int num, boolean mine, Status status) {
        this.num = num;
        this.mine = mine;
        this.status = status;
    }

    public static Unit init(int num, boolean mine) {
        return new Unit(num, mine, Status.None);
    }

    @Override
    public int num() {
        return this.num;
    }

    @Override
    public Status status() {
        return this.status;
    }


}


package com.qingyou.core;

public interface IUnit {
    int num();

    Status status();

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

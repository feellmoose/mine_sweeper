package com.qingyou;

import com.qingyou.cli.GameCli;
import com.qingyou.core.Game;

public class Main {

    public static void main(String[] args) {
        GameCli.start(Game.init(100, 100, 1));
    }
}
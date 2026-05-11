package com.github.paopaoyue.metrics.data;

import com.github.paopaoyue.metrics.proto.MetricsProto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class CardPickV2 extends CardPick {

    private int act;  // the act of the dungeon

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }

}

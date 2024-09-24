package com.github.paopaoyue.metrics;

import com.github.paopaoyue.metrics.api.IMetricsCaller;
import com.github.paopaoyue.metrics.proto.MetricsProto;
import io.github.paopaoyue.mesh.rpc.api.CallOption;
import io.github.paopaoyue.mesh.rpc.util.RespBaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MetricsApplicationTests {

    private static final Logger logger = LogManager.getLogger(MetricsApplicationTests.class);

    @Autowired
    IMetricsCaller metricsCaller;

    @Test
    void addNewCardPick() {
        var response = metricsCaller.mCreateCardPick(
                MetricsProto.MCreateCardPickRequest.newBuilder()
                        .addPicked(MetricsProto.CardPick.newBuilder()
                                .setCardIdentifier(
                                        MetricsProto.CardIdentifier.newBuilder()
                                                .setClasspath("com.github.paopaoyue.mock-mod.card.MockCard1")
                                                .setCardId("mock-mod::MockCard1")
                                                .setUpgraded(false)
                                                .build()
                                )
                                .setCardType("attack")
                                .setCardRarity("common")
                                .setCardCost(1)
                                .setNumInDeck(0)
                                .build())
                        .addUnpicked(MetricsProto.CardPick.newBuilder()
                                .setCardIdentifier(
                                        MetricsProto.CardIdentifier.newBuilder()
                                                .setClasspath("com.github.paopaoyue.mock-mod.card.MockCard2")
                                                .setCardId("mock-mod::MockCard2")
                                                .setUpgraded(false)
                                                .build()
                                )
                                .setCardType("skill")
                                .setCardRarity("uncommon")
                                .setCardCost(1)
                                .setNumInDeck(0)
                                .build())
                        .addUnpicked(MetricsProto.CardPick.newBuilder()
                                .setCardIdentifier(
                                        MetricsProto.CardIdentifier.newBuilder()
                                                .setClasspath("com.github.paopaoyue.mock-mod.card.MockCard3")
                                                .setCardId("mock-mod::MockCard3")
                                                .setUpgraded(true)
                                                .build()
                                )
                                .setCardType("power")
                                .setCardRarity("rare")
                                .setCardCost(1)
                                .setNumInDeck(0)
                                .build())
                        .setLevel(10)
                        .setAscension(0)
                        .setUserName("mock_user")
                        .setCharacterName("mock_character")
                        .setRegion("cn")
                        .setTimestamp((int) (System.currentTimeMillis() / 1000))
                        .build(),

                new CallOption()
        );
        logger.info("response: {}", response);
        assertThat(RespBaseUtil.isOK(response.getBase())).isTrue();
    }

    @Test
    void queryCardPick() {
        logger.info("queryCardPick");
        var response = metricsCaller.mGetCardPickStat(
                MetricsProto.MGetCardPickStatRequest.newBuilder()
                        .addCardIdentifiers(
                                MetricsProto.CardIdentifier.newBuilder()
                                        .setClasspath("com.github.paopaoyue.mock-mod.card.MockCard1")
                                        .setCardId("mock-mod::MockCard1")
                                        .setUpgraded(false)
                                        .build()
                        )
                        .addCardIdentifiers(
                                MetricsProto.CardIdentifier.newBuilder()
                                        .setClasspath("com.github.paopaoyue.mock-mod.card.MockCard2")
                                        .setCardId("mock-mod::MockCard2")
                                        .setUpgraded(false)
                                        .build()
                        )
                        .addCardIdentifiers(
                                MetricsProto.CardIdentifier.newBuilder()
                                        .setClasspath("com.github.paopaoyue.mock-mod.card.MockCard3")
                                        .setCardId("mock-mod::MockCard3")
                                        .setUpgraded(false)
                                        .build()
                        )
                        .build(),

                new CallOption().setTimeout(Duration.ofSeconds(3))
        );
        logger.info("response: {}", response);
        assertThat(RespBaseUtil.isOK(response.getBase())).isTrue();
    }
}

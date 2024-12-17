package com.github.paopaoyue.metrics.service;

import com.alibaba.fastjson.JSON;
import com.clickhouse.client.api.query.GenericRecord;
import com.github.paopaoyue.metrics.config.Configuration;
import com.github.paopaoyue.metrics.data.CardPick;
import com.github.paopaoyue.metrics.data.CardPickExtra;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.paopaoyue.mesh.rpc.proto.Base;
import io.github.paopaoyue.mesh.rpc.service.RpcService;
import com.github.paopaoyue.metrics.proto.MetricsProto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.*;
import java.util.*;

@RpcService(serviceName = "metrics")
public class MetricsService implements IMetricsService {

    private static final Logger logger = LogManager.getLogger(MetricsService.class);

    private final ClickhouseService clickhouseService;

    private final RedisService redisService;

    private static final String CARD_PICK_STAT_SQL = """
            WITH card_data AS (
                SELECT
                    unique_id,
                    COUNT(*) AS total_encounters,                           -- Total encounters (samples)
                    SUM(picked) AS total_picks,                             -- Total times picked
                    COUNT(DISTINCT user_name) AS sample_players,            -- Unique players

                    -- First pick (num_in_deck = 0, picked = 1) count and total per level
                    countIf(level < 17 AND num_in_deck = 0 AND picked = 1) AS first_pick_f1_count,
                    countIf(level < 17 AND num_in_deck = 0) AS first_pick_f1_total,
                        
                    countIf(level >= 17 AND level < 34 AND num_in_deck = 0 AND picked = 1) AS first_pick_f2_count,
                    countIf(level >= 17 AND level < 34 AND num_in_deck = 0) AS first_pick_f2_total,
                        
                    countIf(level >= 34 AND num_in_deck = 0 AND picked = 1) AS first_pick_f3_count,
                    countIf(level >= 34 AND num_in_deck = 0) AS first_pick_f3_total,
                        
                    -- Duplicate pick (num_in_deck != 0) count and total per level
                    countIf(level < 17 AND num_in_deck != 0 AND picked = 1) AS duplicate_pick_f1_count,
                    countIf(level < 17 AND num_in_deck != 0) AS duplicate_pick_f1_total,
                        
                    countIf(level >= 17 AND level < 34 AND num_in_deck != 0 AND picked = 1) AS duplicate_pick_f2_count,
                    countIf(level >= 17 AND level < 34 AND num_in_deck != 0) AS duplicate_pick_f2_total,
                        
                    countIf(level >= 34 AND num_in_deck != 0 AND picked = 1) AS duplicate_pick_f3_count,
                    countIf(level >= 34 AND num_in_deck != 0) AS duplicate_pick_f3_total
                FROM card_pick
                WHERE unique_id IN (%s)
                %s
                GROUP BY unique_id
            )
                        
            SELECT
                unique_id,
                total_encounters,
                total_picks,
                        
                -- Overall pick rate
                total_picks / nullIf(total_encounters, 0) AS pick_rate,    -- Overall pick rate
                        
                -- First pick rates
                first_pick_f1_count / nullIf(first_pick_f1_total, 0) AS first_pick_rate_f1,
                first_pick_f2_count / nullIf(first_pick_f2_total, 0) AS first_pick_rate_f2,
                first_pick_f3_count / nullIf(first_pick_f3_total, 0) AS first_pick_rate_f3,
                        
                -- Duplicate pick rates
                duplicate_pick_f1_count / nullIf(duplicate_pick_f1_total, 0) AS duplicate_pick_rate_f1,
                duplicate_pick_f2_count / nullIf(duplicate_pick_f2_total, 0) AS duplicate_pick_rate_f2,
                duplicate_pick_f3_count / nullIf(duplicate_pick_f3_total, 0) AS duplicate_pick_rate_f3,
                        
                sample_players                                          -- Number of unique players
            FROM card_data;
            """;

    public MetricsService(ClickhouseService clickhouseService, RedisService redisService) {
        this.clickhouseService = clickhouseService;
        this.redisService = redisService;
    }

    @Override
    public MetricsProto.MGetCardPickStatResponse mGetCardPickStat(MetricsProto.MGetCardPickStatRequest request) {
        var builder = MetricsProto.MGetCardPickStatResponse.newBuilder();

        Map<String, MetricsProto.CardIdentifier> uniqueIdToIdentifierMap = new HashMap<>();
        for (MetricsProto.CardIdentifier identifier : request.getCardIdentifiersList()) {
            uniqueIdToIdentifierMap.put(CardPick.generateUniqueId(identifier), identifier);
        }
        // all unique ids need to fetch
        List<String> uniqueIds = new ArrayList<>(uniqueIdToIdentifierMap.keySet());

        // 1. fetch from redis
        Iterator<String> uniqueIdIterator = uniqueIds.iterator();
        while (uniqueIdIterator.hasNext()) {
            String uniqueId = uniqueIdIterator.next();
            byte[] value = redisService.get(getRedisKey(uniqueId, request));
            if (value != null) {
                try {
                    builder.addCardPickStats(MetricsProto.CardPickStat.parseFrom(value));
                    uniqueIdIterator.remove();
                } catch (InvalidProtocolBufferException e) {
                    logger.error("Failed to parse CardPickStat from Redis", e);
                }
            }
        }
        if (uniqueIds.isEmpty()) {
            return builder.build();
        }

        // 2. fetch from clickhouse
        String whereClauses = "";
        if ((request.getAscensionMax() > 0 || request.getAscensionMin() > 0) && (request.getAscensionMax() > request.getAscensionMin())) {
            whereClauses += String.format(" AND ascension BETWEEN %d AND %d", request.getAscensionMin(), request.getAscensionMax());
        }
//        if (request.getTimestampStart() > 0 && request.getTimestampEnd() > 0 && request.getTimestampEnd() > request.getTimestampStart()) {
//            whereClauses += String.format(" AND toUnixTimestamp(timestamp) BETWEEN %d AND %d", request.getTimestampStart(), request.getTimestampEnd());
//        }
//        if (!request.getRegionsList().isEmpty()) {
//            whereClauses += String.format(" AND region IN ('%s')", stringListToSqlIn(request.getRegionsList()));
//        }
        long timestamp = Instant.now().getEpochSecond();
        var sql = String.format(CARD_PICK_STAT_SQL, stringListToSqlIn(uniqueIds), whereClauses);
        clickhouseService.query(sql, record -> {
            String uniqueId = record.getString("unique_id");
            var cardPickStatbuilder = MetricsProto.CardPickStat.newBuilder()
                    .setCardIdentifier(uniqueIdToIdentifierMap.get(record.getString("unique_id")))
                    .setSamplePlayers(record.getBigInteger("sample_players").intValue())
                    .setSampleSize(record.getBigInteger("total_encounters").longValue())
                    .setTimeStamp(timestamp);

            if (record.hasValue("pick_rate"))
                cardPickStatbuilder.setPickRate(record.getDouble("pick_rate"));
            if (record.hasValue("first_pick_rate_f1"))
                cardPickStatbuilder.setFirstPickRateF1(record.getDouble("first_pick_rate_f1"));
            if (record.hasValue("first_pick_rate_f2"))
                cardPickStatbuilder.setFirstPickRateF2(record.getDouble("first_pick_rate_f2"));
            if (record.hasValue("first_pick_rate_f3"))
                cardPickStatbuilder.setFirstPickRateF3(record.getDouble("first_pick_rate_f3"));
            if (record.hasValue("duplicate_pick_rate_f1"))
                cardPickStatbuilder.setDuplicatePickRateF1(record.getDouble("duplicate_pick_rate_f1"));
            if (record.hasValue("duplicate_pick_rate_f2"))
                cardPickStatbuilder.setDuplicatePickRateF2(record.getDouble("duplicate_pick_rate_f2"));
            if (record.hasValue("duplicate_pick_rate_f3"))
                cardPickStatbuilder.setDuplicatePickRateF3(record.getDouble("duplicate_pick_rate_f3"));

            var cardPickStat = cardPickStatbuilder.build();
            builder.addCardPickStats(cardPickStat);
            uniqueIds.remove(uniqueId);
            redisService.set(getRedisKey(uniqueId, request), cardPickStat.toByteArray(),
                    Duration.ofSeconds(Configuration.getProp().getCardPickStatRedisExpire()));
        });

        // 3. fill the rest with empty stats
        for (var uniqueId : uniqueIds) {
            var cardPickStat = MetricsProto.CardPickStat.newBuilder()
                    .setCardIdentifier(uniqueIdToIdentifierMap.get(uniqueId))
                    .setSamplePlayers(0)
                    .setSampleSize(0)
                    .setTimeStamp(timestamp)
                    .build();
            builder.addCardPickStats(cardPickStat);
            redisService.set(getRedisKey(uniqueId, request), cardPickStat.toByteArray(),
                    Duration.ofSeconds(Configuration.getProp().getCardPickStatRedisExpire()));
        }

        return builder.build();
    }

    @Override
    public MetricsProto.MCreateCardPickResponse mCreateCardPick(MetricsProto.MCreateCardPickRequest request) {
        if (!validateCreateRequest(request)) {
            return MetricsProto.MCreateCardPickResponse.newBuilder()
                    .setBase(Base.RespBase.newBuilder().setCode(Base.StatusCode.INVALID_PARAM_ERROR_VALUE).setMessage("Invalid request"))
                    .build();
        }
        List<CardPick> picked = request.getPickedList().stream()
                .map(cp -> cardPickFromProto(cp, request, true))
                .toList();
        List<CardPick> unpicked = request.getUnpickedList().stream()
                .map(cp -> cardPickFromProto(cp, request, false))
                .toList();

        CardPickExtra extra = new CardPickExtra(
                picked.stream().map(CardPick::getUniqueId).toList(),
                unpicked.stream().map(CardPick::getUniqueId).toList()
        );
        String extraJson = JSON.toJSONString(extra);
        for (CardPick cp : picked) {
            cp.setExtra(extraJson);
            clickhouseService.insert(ClickhouseService.CARD_PICK_TABLE_NAME, cp);
        }
        for (CardPick cp : unpicked) {
            cp.setExtra(extraJson);
            clickhouseService.insert(ClickhouseService.CARD_PICK_TABLE_NAME, cp);
        }
        return MetricsProto.MCreateCardPickResponse.newBuilder().build();
    }

    private boolean validateCreateRequest(MetricsProto.MCreateCardPickRequest request) {
        if (request.getLevel() < 0 || request.getLevel() > 60 ||
                request.getAscension() < 0 || request.getAscension() > 30 ||
                request.getTimestamp() < 0) {
            logger.warn("Invalid create card pick request with level {}, ascension {}, timestamp {}",
                    request.getLevel(), request.getAscension(), request.getTimestamp());
            return false;
        }
        if (request.getPickedList().isEmpty() && request.getUnpickedList().isEmpty()) {
            logger.warn("Empty picked and unpicked list in create card pick request");
            return false;
        }
        if (request.getPickedList().stream().anyMatch(cp -> cp.getNumInDeck() < 0)) {
            logger.warn("Negative num in deck in picked list in create card pick request");
            return false;
        }
        if (request.getUnpickedList().stream().anyMatch(cp -> cp.getNumInDeck() < 0)) {
            logger.warn("Negative num in deck in unpicked list in create card pick request");
            return false;
        }
        return true;
    }

    private CardPick cardPickFromProto(MetricsProto.CardPick cp, MetricsProto.MCreateCardPickRequest request, boolean picked) {
        CardPick cardPick = new CardPick();
        cardPick.setUniqueId(CardPick.generateUniqueId(cp.getCardIdentifier()));
        cardPick.setCardId(cp.getCardIdentifier().getCardId());
        cardPick.setCardRarity(cp.getCardRarity());
        cardPick.setCardType(cp.getCardType());
        cardPick.setCardCost(cp.getCardCost());
        cardPick.setNumInDeck(cp.getNumInDeck());
        cardPick.setUpgraded(cp.getCardIdentifier().getUpgraded());
        cardPick.setPicked(picked);
        cardPick.setLevel(request.getLevel());
        cardPick.setAscension(request.getAscension());
        cardPick.setUserName(request.getUserName());
        cardPick.setCharacterName(request.getCharacterName());
        cardPick.setRegion(request.getRegion());
        cardPick.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochSecond(request.getTimestamp()), ZoneOffset.UTC));
        return cardPick;
    }

    private String getRedisKey(String uniqueId, MetricsProto.MGetCardPickStatRequest request) {
        String key = "card_pick_stat:" + uniqueId;
        if (request.getAscensionMax() > 0 || request.getAscensionMin() > 0) {
            return key + ":a" + request.getAscensionMin() + "-" + request.getAscensionMax();
        }
        return key;
    }

    private String stringListToSqlIn(List<String> list) {
        return String.join(",", list.stream().map(s -> String.format("'%s'", s)).toList());
    }
}

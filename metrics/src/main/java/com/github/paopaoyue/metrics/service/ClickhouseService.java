package com.github.paopaoyue.metrics.service;

import com.clickhouse.client.api.enums.Protocol;
import com.clickhouse.client.api.insert.InsertSettings;
import com.clickhouse.client.api.query.GenericRecord;
import com.clickhouse.client.api.query.Records;
import com.github.paopaoyue.metrics.config.Configuration;
import com.github.paopaoyue.metrics.config.Properties;
import com.github.paopaoyue.metrics.data.CardPick;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import com.clickhouse.client.api.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty(value = "sts-service.metrics.clickhouse-host")
@DependsOn("configuration")
public class ClickhouseService {

    public static final String CARD_PICK_TABLE_NAME = "card_pick";

    private static final Logger logger = LogManager.getLogger(ClickhouseService.class);

    private final Client client;

    private final Map<String, List<Object>> bufferMap = new HashMap<>();

    public ClickhouseService() {
        Properties prop = Configuration.getProp();
        logger.info("properties: {}", Configuration.getProp().getClickhouseHost());
        Client.Builder clientBuilder = new Client.Builder()
                .addEndpoint(Protocol.HTTP, prop.getClickhouseHost(), prop.getClickhousePort(), false)
                .setUsername(prop.getClickhouseUsername())
                .setPassword(prop.getClickhousePassword())
                .setServerTimeZone("UTC")
                .compressServerResponse(true)
                .setDefaultDatabase(prop.getClickhouseDatabase());
        this.client = clientBuilder.build();
        register();
    }

    private void register() {
        bufferMap.put(CARD_PICK_TABLE_NAME, new ArrayList<>());
        client.register(CardPick.class, client.getTableSchema(CARD_PICK_TABLE_NAME));
    }

    @PostConstruct
    public void init() {
        if (!isAlive()) {
            logger.error("Clickhouse service is not available");
        }
    }

    public boolean isAlive() {
        return client.ping();
    }

    public void query(String sql, Consumer<GenericRecord> consumer) {
        logger.debug("Querying data: {}", sql);
        try (Records records = client.queryRecords(sql).get(Configuration.getProp().getCardPickStatQueryTimeout(), TimeUnit.SECONDS);) {
            logger.debug("Data read successfully: {} ms", TimeUnit.NANOSECONDS.toMillis(records.getServerTime()));
            logger.debug("Total rows: {}", records.getResultRows());
            if (records.getResultRows() == 0)   {
                return;
            }
            for (GenericRecord record : records) {
                consumer.accept(record);
            }
        } catch (Exception e) {
            logger.error("Failed to read data", e);
        }
    }

    public <T> void insert(String table, T object) {
        var buffer = bufferMap.get(table);
        if (buffer == null) {
            logger.error("Table {} not found", table);
            return;
        }
        buffer.add(object);
        if (buffer.size() >= Configuration.getProp().getInsertBatchSize()) {
            flush(table, buffer);
            buffer.clear();
        }
    }

    private void flush(String table, List<Object> buffer) {
        try (var response = client.insert(table, buffer, new InsertSettings()).get(Configuration.getProp().getCardPickStatQueryTimeout(), TimeUnit.SECONDS)) {
            logger.info("Flushed {} records to table {}", buffer.size(), table);
        } catch (Exception e) {
            logger.error("Failed to flush data", e);
        }

    }

}

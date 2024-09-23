package com.github.paopaoyue.metrics.stub;

import io.github.paopaoyue.mesh.rpc.exception.HandlerException;
import io.github.paopaoyue.mesh.rpc.exception.HandlerNotFoundException;
import io.github.paopaoyue.mesh.rpc.proto.Protocol;
import io.github.paopaoyue.mesh.rpc.stub.IServerStub;
import io.github.paopaoyue.mesh.rpc.stub.ServiceServerStub;
import io.github.paopaoyue.mesh.rpc.util.Context;
import com.github.paopaoyue.metrics.proto.MetricsProto;
import com.github.paopaoyue.metrics.service.IMetricsService;
import com.google.protobuf.Any;

@ServiceServerStub(serviceName = "metrics")
public class MetricsServerStub implements IServerStub {

    private static final String SERVICE_NAME = "metrics";

    private IMetricsService service;

    @Override
    public Protocol.Packet process(Protocol.Packet packet) throws HandlerException, HandlerNotFoundException {
        Context context = new Context(packet);
        Context.setContext(context);

        if (!context.getService().equals(SERVICE_NAME)) {
            throw new HandlerNotFoundException(context.getService(), context.getHandler());
        }

        Any responseBody;
        try {
            switch (context.getHandler()) {
                case "mGetCardPickStat" ->
                    responseBody = Any.pack(service.mGetCardPickStat(packet.getBody().unpack(MetricsProto.MGetCardPickStatRequest.class)));
                case "mCreateCardPick" ->
                    responseBody = Any.pack(service.mCreateCardPick(packet.getBody().unpack(MetricsProto.MCreateCardPickRequest.class)));
                default -> throw new HandlerNotFoundException(context.getService(), context.getHandler());
            }
        } catch (Exception e) {
            throw new HandlerException("Handler error", e);
        }

        Protocol.Packet out = Protocol.Packet.newBuilder()
                .setHeader(packet.getHeader())
                .setTraceInfo(packet.getTraceInfo())
                .setBody(responseBody)
                .build();

        Context.removeContext();
        return out;
    }
}

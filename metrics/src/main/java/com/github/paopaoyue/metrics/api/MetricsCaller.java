package com.github.paopaoyue.metrics.api;

import io.github.paopaoyue.mesh.rpc.api.CallOption;
import io.github.paopaoyue.mesh.rpc.api.RpcCaller;
import io.github.paopaoyue.mesh.rpc.stub.IClientStub;
import com.github.paopaoyue.metrics.proto.MetricsProto;

@RpcCaller(serviceName = "metrics")
public class MetricsCaller implements IMetricsCaller {

    IClientStub clientStub;

    @Override
    public MetricsProto.MGetCardPickStatResponse mGetCardPickStat(MetricsProto.MGetCardPickStatRequest request, CallOption option) {
        return clientStub.process(MetricsProto.MGetCardPickStatResponse.class, request, option);
    }
    @Override
    public MetricsProto.MCreateCardPickResponse mCreateCardPick(MetricsProto.MCreateCardPickRequest request, CallOption option) {
        return clientStub.process(MetricsProto.MCreateCardPickResponse.class, request, option);
    }
}
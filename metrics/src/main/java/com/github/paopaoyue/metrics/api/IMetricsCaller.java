package com.github.paopaoyue.metrics.api;

import io.github.paopaoyue.mesh.rpc.api.CallOption;
import com.github.paopaoyue.metrics.proto.MetricsProto;

public interface IMetricsCaller {

    MetricsProto.MGetCardPickStatResponse mGetCardPickStat(MetricsProto.MGetCardPickStatRequest request, CallOption option);
    MetricsProto.MCreateCardPickResponse mCreateCardPick(MetricsProto.MCreateCardPickRequest request, CallOption option);
}

package com.github.paopaoyue.metrics.service;

import com.github.paopaoyue.metrics.proto.MetricsProto;

public interface IMetricsService {

    MetricsProto.MGetCardPickStatResponse mGetCardPickStat(MetricsProto.MGetCardPickStatRequest request);
    MetricsProto.MCreateCardPickResponse mCreateCardPick(MetricsProto.MCreateCardPickRequest request);
}

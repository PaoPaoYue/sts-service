package com.github.paopaoyue.demo.service;

import io.github.paopaoyue.mesh.rpc.service.RpcService;
import io.github.paopaoyue.mesh.rpc.util.Context;
import com.github.paopaoyue.demo.proto.DemoProto;

@RpcService(serviceName = "demo")
public class DemoService implements IDemoService {

    @Override
    public DemoProto.EchoResponse echo(DemoProto.EchoRequest request) {
         return DemoProto.EchoResponse.newBuilder().setText(request.getText()).build();
    }
}

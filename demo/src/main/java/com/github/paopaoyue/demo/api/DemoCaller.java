package com.github.paopaoyue.demo.api;

import io.github.paopaoyue.mesh.rpc.api.CallOption;
import io.github.paopaoyue.mesh.rpc.api.RpcCaller;
import io.github.paopaoyue.mesh.rpc.stub.IClientStub;
import com.github.paopaoyue.demo.proto.DemoProto;

@RpcCaller(serviceName = "demo")
public class DemoCaller implements IDemoCaller {

    IClientStub clientStub;

    @Override
    public DemoProto.EchoResponse echo(DemoProto.EchoRequest request, CallOption option) {
        return clientStub.process(DemoProto.EchoResponse.class, request, option);
    }
}
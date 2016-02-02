package com.ithinkrok.minigames.base.msm;

import com.google.common.net.HostAndPort;
import com.ithinkrok.msm.common.Packet;
import com.ithinkrok.msm.common.handler.MSMFrameDecoder;
import com.ithinkrok.msm.common.handler.MSMFrameEncoder;
import com.ithinkrok.msm.common.handler.MSMPacketDecoder;
import com.ithinkrok.msm.common.handler.MSMPacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * Created by paul on 01/02/16.
 */
public class MSMClient extends ChannelInboundHandlerAdapter {

    private final HostAndPort address;

    private volatile Channel channel;

    public MSMClient(HostAndPort address) {
        this.address = address;
    }

    public void start() {
        System.out.println("Connecting to MSM server: " + address);

        EventLoopGroup workerGroup = createNioEventLoopGroup();

        Bootstrap b = createBootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                setupPipeline(ch.pipeline());
            }
        });

        ChannelFuture future = b.connect(address.getHostText(), address.getPort());

        //noinspection unchecked
        future.addListeners(unused1 -> startRequest());

        future.channel().closeFuture().addListener(unused2 -> workerGroup.shutdownGracefully());
    }

    void startRequest() {
        System.out.println("Connected successfully and sending test packet");

        Packet test = new Packet((byte) 0, new MemoryConfiguration());

        channel.writeAndFlush(test);
    }

    NioEventLoopGroup createNioEventLoopGroup() {
        return new NioEventLoopGroup(1);
    }

    Bootstrap createBootstrap() {
        return new Bootstrap();
    }

    private void setupPipeline(ChannelPipeline pipeline) {
        //inbound
        pipeline.addLast("MSMFrameDecoder", new MSMFrameDecoder());
        pipeline.addLast("MSMPacketDecoder", new MSMPacketDecoder());

        //outbound
        pipeline.addLast("MSMFrameEncoder", new MSMFrameEncoder());
        pipeline.addLast("MSMPacketEncoder", new MSMPacketEncoder());

        pipeline.addLast("MSMClient", this);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
    }
}

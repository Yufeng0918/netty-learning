package io.netty.example.study.server.codec.handler;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Auther: daiyu
 * @Date: 16/2/20 20:08
 * @Description:
 */

@ChannelHandler.Sharable
public class MetricHandler extends ChannelDuplexHandler {

    private AtomicLong totalConnectionCount = new AtomicLong();

    {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.register("totalConnectionCount", (Gauge<Long>)() -> totalConnectionCount.longValue());

        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).build();
        consoleReporter.start(5, TimeUnit.SECONDS);

        JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
        jmxReporter.start();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        totalConnectionCount.incrementAndGet();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        totalConnectionCount.decrementAndGet();
    }
}

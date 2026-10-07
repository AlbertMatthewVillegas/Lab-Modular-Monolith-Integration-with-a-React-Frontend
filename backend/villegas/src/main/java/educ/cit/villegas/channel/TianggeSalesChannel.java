package educ.cit.villegas.channel;

import educ.cit.villegas.app.AppInstance;
import org.springframework.stereotype.Service;

@Service
class TianggeSalesChannel implements SalesChannel {

    private final AppInstance instance;
    private final TianggeConnection connection;
    private final FeedPoller poller;
    private final TianggeOutbox outbox;
    private final ChannelOrderRepository orders;

    TianggeSalesChannel(AppInstance instance, TianggeConnection connection, FeedPoller poller,
                        TianggeOutbox outbox, ChannelOrderRepository orders) {
        this.instance = instance;
        this.connection = connection;
        this.poller = poller;
        this.outbox = outbox;
        this.orders = orders;
    }

    @Override
    public ChannelStatus status() {
        return new ChannelStatus(instance.id(), poller.isLive(), connection.lastHeartbeat(),
                poller.lastCursor(), outbox.listedSkus(), orders.countUnsentReplies());
    }
}

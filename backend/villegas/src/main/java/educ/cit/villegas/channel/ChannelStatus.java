package educ.cit.villegas.channel;

import java.time.Instant;
import java.util.Set;

public record ChannelStatus(String instanceId,
                            boolean live,
                            Instant lastHeartbeat,
                            long feedCursor,
                            Set<String> listedProducts,
                            long unsentReplies) {
}

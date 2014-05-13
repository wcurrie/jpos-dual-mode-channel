Spike a [jpos](http://jpos.org) channel that allows clients to be migrated from XMLChannel to something less byte hungry.

Current channel implementations:

* [XMLChannel](https://github.com/jpos/jPOS/blob/master/jpos/src/main/java/org/jpos/iso/channel/XMLChannel.java) delimits messages by counting start and end &lt;isomsg&gt; xml elements.
* [GZIPChannel](https://github.com/jpos/jPOS/blob/master/jpos/src/main/java/org/jpos/iso/channel/GZIPChannel.java) uses a two byte length prefix.

The idea is to use [DualModeXmlChannel](https://github.com/wcurrie/jpos-dual-mode-channel/blob/master/src/main/java/x/DualModeXmlChannel.java) on the server and roll out clients using [VersionedGzipChannel](https://github.com/wcurrie/jpos-dual-mode-channel/blob/master/src/main/java/x/VersionedGzipChannel.java). Existing clients continue to use XMLChannel.

VersionedGzipChannel differs from GZIPChannel by prefixing the message length with a rather arbitrary fixed pattern (#01). The fixed prefix means a reader (the server) can always tell what message format the client speaks.

The only scenario catered for is where all servers are upgraded before any client and the client is the first party to send in a conversation. The format of the first message received by the server determines the format the server will use for the session.

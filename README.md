# JSteamKit
### The library requires the use of JCE Unlimited Strength Jurisdiction Policy Files

Connecting and logging in
```java
SteamClient steamClient = new SteamClient();
 /*
  * Verbosity argument
  * true = announce when an EMsg is received
  * false = only announce debug/informational messages
  */
steamClient.connect(true);

/* 
 * Add a listener for the EMsg "ChannelEncryptResult" so that the client 
 * logs in just after encryption has been installed
 */
steamClient.registerEventHandler(EMsg.ChannelEncryptResult, (d) -> {
    LoginCredentials credentials = new LoginCredentials();
    credentials.username = "somethingnifty";
    credentials.password = "somethingsecure";
    steamClient.login(credentials);
});
```

Sending an EMsg to the server (in this example for adding a friend)
```java
// Construct a MsgProtoBuf with a body of CMsgClientAddFriend
SteammessagesBase.CMsgProtoBufHeader.Builder proto
        = SteammessagesBase.CMsgProtoBufHeader.newBuilder();
MsgHeaderProtoBuf header = new MsgHeaderProtoBuf(proto);
SteammessagesClientserver.CMsgClientAddFriend.Builder body
        = SteammessagesClientserver.CMsgClientAddFriend.newBuilder();
MsgProtoBuf msg = new MsgProtoBuf(header, body);

// Set details about the packet
header.msg = EMsg.ClientAddFriend;
proto.setSteamid(steamClient.steamId);
body.setAccountnameOrEmailToAdd("gabelogannewell");

// Encode the packet and send it away
byte[] encodedMsg = msg.encode();
steamClient.connection.sendPacket(encodedMsg);
```

### Dependencies
Automatically used in the project through Gradle:
* [Protocol Buffers](https://github.com/google/protobuf/tree/master/java)
* [Google Guava](https://github.com/google/guava)
* [BouncyCastle Provider](http://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on)

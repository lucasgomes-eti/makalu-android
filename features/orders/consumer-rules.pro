# HiveMQ MQTT client (order tracking), as its Android documentation requires.
-keepclassmembernames class io.netty.** { *; }
-keepclassmembers class org.jctools.** { *; }

# Optional integrations Netty and the MQTT client reference but that are not on the
# classpath, and are never reached on Android: the native epoll transport, proxy
# support, OpenSSL bindings, log4j, and Jetty's ALPN/NPN.
-dontwarn io.netty.channel.epoll.**
-dontwarn io.netty.handler.proxy.**
-dontwarn io.netty.internal.tcnative.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.eclipse.jetty.alpn.**
-dontwarn org.eclipse.jetty.npn.**

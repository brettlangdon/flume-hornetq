Flume HornetQ Plugin
=======


## Overview
This plugin has been built for and tested with Cloudera's Flume v0.9.4 from CDH3 in order to provide interoperability with HornetQ Messaging queue.


## Install
* In order to install you will need to place the HornetQ jars from the `lib` directory of the download package into the Flume lib directory: `cp ./hornetq-2.2.14.Final/lib/*.jar /usr/lib/flume/lib/`.
* Download source: `git clone git://github.com/brettlangdon/flume-hornetq.git`
* Build plugin: `cd ./flume-hornetq` then `ant`
* Place `flume-hornetq.jar` into Flume lib directory: `cp flume-hornetq.jar /usr/lib/flume/lib`.
* Add `com.blangdon.flume.hornetq.HornetQJMSSink` to the `flume.plugin.classes` property in your Flume site config: `/etc/flume/cponf/flume-site/xml`.

## HornetQJMSSink
Provides a producer to send events to HornetQ

### Definition
```
hornetQJMSSink( queueName, [jnpHost, jnpPort] )
```

### Example
```
collector: autoCollectorSource() | hornetQJMSSink("/queue/name");
```

### TODO
* Auto Reconnect, right now if there is a disconnect between Flume and HornetQ then no more events will be Produced.
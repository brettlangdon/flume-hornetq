package com.blangdon.flume.hornetq;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.util.Pair;
import com.google.common.base.Preconditions;

/**
 * Sink that sends events to HornetQ queue
 */
public class HornetQJMSSink extends EventSink.Base {
    
    private Connection connection = null;
    private InitialContext initialContext = null;
    private Queue queue = null;
    private Session session = null;
    private MessageProducer producer = null;
    private String queueName = null;
    private String jnpHost = null;
    private String jnpPort = null;

    public HornetQJMSSink(String queue, String host, String port ){
	this.queueName = queue;
	this.jnpHost = host;
	this.jnpPort = port;
    }
	
    @Override
	public void open() throws IOException {
	// Initialized the sink

	try{
	    Properties props = new Properties();
	    props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
	    props.setProperty("java.naming.provider.url","jnp://" + this.jnpHost + ":" + this.jnpPort);
	    props.setProperty("java.naming.factory.url.pkgs","org.jboss.naming:org.jnp.interfaces");
	    
	    this.initialContext = new InitialContext(props);
	
	
	    this.queue = (Queue)this.initialContext.lookup(this.queueName);
	    ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("/ConnectionFactory");
	    
	    this.connection = cf.createConnection();
	    
	    this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	
	    this.producer = this.session.createProducer(queue);
	}catch(Exception e){
	    throw new IOException("HornetQ Exception: " + e);
	}
    }
    
    @Override
	public void append(Event e) throws IOException {
	//send to HornetQ

	if( this.session == null || this.producer == null )
	    throw new IllegalStateException("HornetQSink Not Initialized Properly: " + e);
	try{
	    TextMessage event = (TextMessage)session.createTextMessage(e.getBody().toString());
	    this.producer.send(event);
	}catch(Exception ex){
	    throw new IOException("HornetQ Exception: " + e);
	}
    }


    @Override
	public void close() throws IOException {
	// Cleanup
	try{
	    if( this.initialContext != null )
		this.initialContext.close();
	    if( this.connection != null )
		this.connection.close();
	}catch(Exception e){
	}
    }
    
    public static SinkBuilder builder() {
	return new SinkBuilder() {
	    // construct a new parameterized sink
	    @Override
		public EventSink build(Context context, String... argv) {
		Preconditions.checkArgument(argv.length > 0 && argv.length < 4,
					    "usage: hornetQJMSSink(queueName, [jnpHost, jnpPort])");

		//defaults
		String jnpHost = "127.0.0.1";
		String jnpPort = "1099";
		String queueName = argv[0];

		if( argv.length > 1 ){
		    jnpHost = argv[1];
		}

		if( argv.length > 2 ){
		    jnpPort = argv[2];
		}

		
		return new HornetQJMSSink(queueName,jnpHost,jnpPort);
	    }
	};
    }
    
    /**
     * This is a special function used by the SourceFactory to pull in this class
     * as a plugin sink.
     */
    public static List<Pair<String, SinkBuilder>> getSinkBuilders() {
	List<Pair<String, SinkBuilder>> builders =
	    new ArrayList<Pair<String, SinkBuilder>>();
	builders.add(new Pair<String, SinkBuilder>("hornetQJMSSink", builder()));
	return builders;
    }
}

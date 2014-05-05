package com.stratio.streaming.functions.ddl;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.exception.SiddhiPraserException;

import com.stratio.streaming.commons.constants.REPLY_CODES;
import com.stratio.streaming.commons.constants.STREAM_OPERATIONS;
import com.stratio.streaming.commons.messages.ColumnNameTypeValue;
import com.stratio.streaming.commons.messages.StratioStreamingMessage;
import com.stratio.streaming.functions.StratioStreamingBaseFunction;
import com.stratio.streaming.streams.StreamOperations;
import com.stratio.streaming.utils.SiddhiUtils;

public class CreateStreamFunction extends StratioStreamingBaseFunction {
	
	private static Logger logger = LoggerFactory.getLogger(CreateStreamFunction.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 7911766880059394316L;

	public CreateStreamFunction(SiddhiManager siddhiManager, String zookeeperCluster, String kafkaCluster) {
		super(siddhiManager, zookeeperCluster, kafkaCluster);
	}
	

	

	@Override
	public Void call(JavaRDD<StratioStreamingMessage> rdd) throws Exception {
		
		List<StratioStreamingMessage> requests = rdd.collect();
		
		for (StratioStreamingMessage request : requests) {
//			Siddhi doesn't throw an exception if stream exists and you try to recreate it in the same way
//			but if you try to redefine a stream, siddhi will throw a DifferentDefinitionAlreadyExistException
//			so, we avoid this scenario but checking first if stream exists
			
//			stream is allowed and NOT exists in siddhi
			if (SiddhiUtils.isStreamAllowedForThisOperation(request.getStreamName(), STREAM_OPERATIONS.DEFINITION.CREATE)
					&& getSiddhiManager().getStreamDefinition(request.getStreamName()) == null) {

				
				
				try {
										
					StreamOperations.createStream(request, getSiddhiManager());
					
//					ack OK back to the bus
					ackStreamingOperation(request, REPLY_CODES.OK);
					
					
				} catch (SiddhiPraserException spe) {
					logger.info("-_---->" + spe.getMessage());
					ackStreamingOperation(request, REPLY_CODES.KO_PARSER_ERROR);
				}
				
			}
			else {
				ackStreamingOperation(request, REPLY_CODES.KO_STREAM_ALREADY_EXISTS);
			}
		}
		
		return null;
	}
}

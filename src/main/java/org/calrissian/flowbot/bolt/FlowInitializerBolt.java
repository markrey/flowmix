package org.calrissian.flowbot.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.calrissian.flowbot.FlowbotTopology;
import org.calrissian.flowbot.model.Event;
import org.calrissian.flowbot.model.Flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.calrissian.flowbot.Constants.*;
import static org.calrissian.flowbot.spout.MockFlowLoaderSpout.FLOW_LOADER_STREAM;

public class FlowInitializerBolt extends BaseRichBolt {

    Map<String,Flow> flows;
    OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        flows = new HashMap<String, Flow>();
    }

    @Override
    public void execute(Tuple tuple) {

        if(FLOW_LOADER_STREAM.equals(tuple.getSourceStreamId())) {
            for(Flow flow : (Collection<Flow>)tuple.getValue(0))
                flows.put(flow.getId(), flow);
        } else {

            if(flows.size() > 0) {
                for(Flow flow : flows.values()) {
                    Collection<Event> events = (Collection<Event>) tuple.getValue(0);
                    for(Event event : events) {
                        String streamid = flow.getFlowOps().get(0).getComponentName();
                        collector.emit(streamid, new Values(flow.getId(), event, -1));
                    }
                }
            }

            collector.ack(tuple);
        }


    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        FlowbotTopology.declareOutputStreams(outputFieldsDeclarer);
    }
}
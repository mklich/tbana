package com.yolodata.tbana.hadoop.mapred.splunk;


import com.yolodata.tbana.hadoop.mapred.ArrayListTextWritable;
import com.yolodata.tbana.hadoop.mapred.CSVReader;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.splunk.*;

public abstract class SplunkRecordReader implements RecordReader<LongWritable, List<Text>> {

    protected final JobConf configuration;
    protected long currentPosition;
    protected long startPosition;
    protected long endPosition;

    protected Service splunkService;
    protected InputStream is;
    protected InputStreamReader in;
    protected CSVReader reader;


    public static final String SPLUNK_USERNAME = "splunk.username";
    public static final String SPLUNK_PASSWORD = "splunk.password";
    public static final String SPLUNK_HOST = "splunk.host";
    public static final String SPLUNK_PORT = "splunk.port";
    public static final String SPLUNK_SEARCH_QUERY = "splunk.search.query";
    public static final String SPLUNK_EARLIEST_TIME = "splunk.search.earliest_time";
    public static final String SPLUNK_LATEST_TIME = "splunk.search.latest_time";



    public SplunkRecordReader(JobConf configuration) throws IOException {

        this.configuration = configuration;
        validateConfiguration(this.configuration);
        setupService();
    }

    public abstract void initialize(InputSplit inputSplit) throws IOException;

    private void validateConfiguration(JobConf configuration) throws SplunkConfigurationException {
        if(configuration.get(SPLUNK_USERNAME) == null ||
                configuration.get(SPLUNK_PASSWORD) == null ||
                configuration.get(SPLUNK_HOST) == null ||
                configuration.get(SPLUNK_PORT) == null ||
                configuration.get(SPLUNK_SEARCH_QUERY) == null ||
                configuration.get(SPLUNK_EARLIEST_TIME) == null ||
                configuration.get(SPLUNK_LATEST_TIME) == null)
            throw new SplunkConfigurationException("Missing one or more of the following required configurations in JobConf:\n" +
                    SPLUNK_USERNAME + "\n" +
                    SPLUNK_PASSWORD + "\n" +
                    SPLUNK_HOST + "\n" +
                    SPLUNK_PORT + "\n" +
                    SPLUNK_SEARCH_QUERY + "\n" +
                    SPLUNK_EARLIEST_TIME + "\n" +
                    SPLUNK_LATEST_TIME + "\n");

    }

    private void setupService() {
        ServiceArgs serviceArgs = getLoginArgs();
        splunkService = Service.connect(serviceArgs);
    }

    protected void initPositions(SplunkSplit inputSplit) {
        startPosition = inputSplit.getStart();
        endPosition = inputSplit.getEnd();
        currentPosition = startPosition;
    }

    private ServiceArgs getLoginArgs() {
        ServiceArgs loginArgs = new ServiceArgs();
        loginArgs.setUsername(configuration.get(SPLUNK_USERNAME));
        loginArgs.setPassword(configuration.get(SPLUNK_PASSWORD));
        loginArgs.setHost(configuration.get(SPLUNK_HOST));
        loginArgs.setPort(configuration.getInt(SPLUNK_PORT, 8080));

        return loginArgs;

    }

    @Override
    public boolean next(LongWritable key, List<Text> value) throws IOException {
//        if(currentPosition == endPosition)
//            return false;

        reader = new CSVReader(in);

        if(key == null) key = createKey();
        if(value == null) value = createValue();

        int bytesRead = reader.readLine(value);

        if(bytesRead == 0) {
            key = null;
            value = null;
            return false;
        }

        key.set(currentPosition++);
        return true;
    }

    @Override
    public LongWritable createKey() {
        return new LongWritable();
    }

    @Override
    public List<Text> createValue() {
        return new ArrayListTextWritable();
    }

    @Override
    public long getPos() throws IOException {
        return currentPosition;
    }

    @Override
    public void close() throws IOException {
        if(is!=null) {
            is.close();
            is=null;
        }

        if(in!=null) {
            in.close();
            in=null;
        }
    }

    @Override
    public float getProgress() throws IOException {
        if (startPosition == endPosition) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (currentPosition - startPosition) / (float) (endPosition - startPosition));
        }
    }

    private class SplunkConfigurationException extends RuntimeException {
        public SplunkConfigurationException(String message) {
            super(message);
        }
    }
}

package com.yolodata.tbana.hadoop.mapred.splunk;

import com.google.common.primitives.Bytes;
import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.mapred.InputSplit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SplunkSplit implements InputSplit{

    private String jobID;
    private long start;
    private long end;

    public SplunkSplit() {
    }

    public SplunkSplit(long start, long end) {
        this.start = start;
        this.end = end;
        this.jobID = "NONE";
    }

    public SplunkSplit(String jobID, long start, long end) {
        this(start,end);
        this.jobID = jobID;
    }

    @Override
    public long getLength() throws IOException {
        return end-start;
    }

    @Override
    public String[] getLocations() throws IOException {
        return new String[] {String.valueOf(start), String.valueOf(end)};
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(jobID);
        dataOutput.writeLong(start);
        dataOutput.writeLong(end);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.jobID = dataInput.readUTF();
        this.start = dataInput.readLong();
        this.end = dataInput.readLong();
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }

    public String getJobID() {
        return this.jobID;
    }
}

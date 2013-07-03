package com.yolodata.tbana.hadoop.mapred.shuttl;

import com.yolodata.tbana.TestUtils;
import com.yolodata.tbana.hadoop.mapred.FileContentProvider;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShuttlCSVInputFormatTest {

    FileSystem fs;

    @Before
    public void setUp() throws IOException {
        fs = FileSystem.get(new Configuration());
        fs.delete(new Path(TestUtils.TEST_FILE_PATH),true);
    }

    @After
    public void tearDown() throws IOException {
        fs.delete(new Path(TestUtils.TEST_FILE_PATH),true);
    }

    @Test
    public void testReadSingleFileNoMultiline() throws Exception {

        String[] header = {"header", "_raw"};
        String content = FileContentProvider.getRandomContent(header,5);
        Path inputPath = new Path(TestUtils.getRandomTestFilepath());
        TestUtils.createFileWithContent(fs, inputPath,content);

        Path outputPath = new Path(TestUtils.getRandomTestFilepath());
        assertTrue(runJob(new Configuration(), new String[] {inputPath.toString(),outputPath.toString()}));

        String result = TestUtils.readMapReduceOutputFile(fs,outputPath);

        List<String> linesFromExpected = TestUtils.getLinesFromString(content);
        addOffsetToEachLine(linesFromExpected, new int [] {0,12,38,64,90,116});

        List<String> linesFromActual = TestUtils.getLinesFromString(result);
        assertEquals(linesFromActual,linesFromExpected);
    }

    private void addOffsetToEachLine(List<String> linesFromExpected, int [] offsets) {
        for(int i=0;i<linesFromExpected.size();i++)
            linesFromExpected.set(i,(offsets[i]+"\t").concat(linesFromExpected.get(i)));
    }

    private boolean runJob(Configuration conf, String [] args) throws Exception {
        return (ToolRunner.run(conf, new ShuttlTestJob(conf),args) == 0);
    }


}
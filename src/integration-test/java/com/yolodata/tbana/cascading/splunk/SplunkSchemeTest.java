/*
 * Copyright (c) 2013 Yolodata, LLC,  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yolodata.tbana.cascading.splunk;

import cascading.CascadingTestCase;
import cascading.flow.Flow;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.pipe.Pipe;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntryIterator;
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkConf;
import com.yolodata.tbana.testutils.TestConfigurations;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class SplunkSchemeTest extends CascadingTestCase {

    String testData = "src/test/resources/multilineCSV.csv";
    String outputPath = "build/testTMP";

    SplunkConf conf;

    @Before
    public void setUp() {
        conf = TestConfigurations.getConfigurationWithSplunkConfigured();
    }

    @Test
    public void testSplunkScheme() throws IOException {

        runSplunkScheme("csvtest", testData);
    }

    public void runSplunkScheme(String path, String inputData) throws IOException
    {
        Properties properties = TestConfigurations.getSplunkLoginAsProperties();
        SplunkScheme inputScheme = new SplunkScheme(TestConfigurations.getSplunkSearch());
        TextLine outputScheme = new TextLine();

        SplunkTap input = new SplunkTap(properties,inputScheme);
        Hfs output = new Hfs( outputScheme, outputPath + "/quoted/" + path, SinkMode.REPLACE );

        Pipe pipe = new Pipe( "test" );
        Flow flow = new HadoopFlowConnector().connect( input, output, pipe );

        flow.complete();

        validateLength( flow, 10, 2 );

        TupleEntryIterator iterator = flow.openSource();

        // TODO: Header information not used in SplunkScheme yet
        // verifyHeader(iterator.getFields());

        verifyContent(iterator);
    }

    private void verifyHeader(Fields actual) {
        Fields expected = new Fields("offset","sourcetype","_raw");
        assertEquals(expected,actual);
    }

    private void verifyContent(TupleEntryIterator iterator) throws IOException {

        String [] expectedRows = new String[] {
                "1,count=4",
                "2,count=3",
                "3,count=2",
                "4,count=1",
                "5,count=0"
        };
        for(String expectedRow : expectedRows)
            checkResults(iterator.next().getTuple(), expectedRow);
    }

    private void checkResults(Tuple actual, String row) {
        String [] rowValues = row.split(",");
        Tuple expected = new Tuple(new LongWritable(Long.parseLong(rowValues[0])),
                new Text(rowValues[1]));
        assertEquals(expected, new Tuple(actual.get(new int[]{0,4})));
    }
}

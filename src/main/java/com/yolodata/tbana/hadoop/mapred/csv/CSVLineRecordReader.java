package com.yolodata.tbana.hadoop.mapred.csv;

import com.yolodata.tbana.hadoop.mapred.util.ArrayListTextWritable;
import com.yolodata.tbana.hadoop.mapred.util.CSVReader;
import com.yolodata.tbana.hadoop.mapred.util.LongWritableSerializable;
import com.yolodata.tbana.hadoop.mapred.util.TextSerializable;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;

import java.io.*;
import java.util.List;
import java.util.zip.ZipInputStream;

public class CSVLineRecordReader implements RecordReader<LongWritableSerializable, ArrayListTextWritable> {

	public static final String FORMAT_DELIMITER = "mapred.csvinput.delimiter";
	public static final String FORMAT_SEPARATOR = "mapred.csvinput.separator";
	public static final String IS_ZIPFILE = "mapred.csvinput.zipfile";
	public static final String DEFAULT_DELIMITER = "\"";
	public static final String DEFAULT_SEPARATOR = ",";
	public static final boolean DEFAULT_ZIP = true;

	private boolean isZipFile;
	protected InputStream is;
	protected CSVReader reader;
	

	private CompressionCodecFactory compressionCodecs = null;
	private long start;
	private long pos;
	private long end;

	public CSVLineRecordReader() {
		
	}
	
	public CSVLineRecordReader(InputStream is, JobConf conf) throws IOException {
		init(is, conf);
	}
	
	public void init(InputStream is, JobConf conf) throws IOException {
		this.isZipFile = conf.getBoolean(IS_ZIPFILE, DEFAULT_ZIP);
		
		if(isZipFile) {
			@SuppressWarnings("resource")
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			zis.getNextEntry();
			is = zis;
		}
		
		this.is = is;
		this.reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
	}

    public int readLine(ArrayListTextWritable values) throws IOException {
        return this.reader.readLine(values);
    }
	public void initialize(InputSplit genericSplit, JobConf conf) throws IOException {
		FileSplit split = (FileSplit) genericSplit;

		start = split.getStart();
		end = start + split.getLength();
		final Path file = split.getPath();
		compressionCodecs = new CompressionCodecFactory(conf);
		final CompressionCodec codec = compressionCodecs.getCodec(file);

		// open the file and seek to the start of the split
		FileSystem fs = file.getFileSystem(conf);
		FSDataInputStream fileIn = fs.open(split.getPath());

		if (codec != null) {
			is = codec.createInputStream(fileIn);
			end = Long.MAX_VALUE;
		} else {
			if (start != 0) {
				fileIn.seek(start);
			}
			is = fileIn;
		}

		this.pos = start;
		init(is, conf);
	}

    @Override
	public float getProgress() {
		if (start == end) {
			return 0.0f;
		} else {
			return Math.min(1.0f, (pos - start) / (float) (end - start));
		}
	}

	public synchronized void close() throws IOException {
		if (reader != null) {
			reader.close();
            reader = null;
		}
		if (is != null) {
			is.close();
			is = null;
		}
	}

	@Override
	public LongWritableSerializable createKey() {
		return new LongWritableSerializable();
	}

	@Override
	public ArrayListTextWritable createValue() {
		return new ArrayListTextWritable();
	}

	@Override
	public long getPos() throws IOException {
		return pos;
	}

	@Override
	public boolean next(LongWritableSerializable key, ArrayListTextWritable value) throws IOException {
		if (key == null) {
			key = new LongWritableSerializable();
		}
		key.set(pos);

		if (value == null) {
			value = new ArrayListTextWritable();
		}
		while (true) {
			if (pos >= end)
				return false;
			int newSize = 0;
			newSize = reader.readLine(value);
			pos += newSize;
			if (newSize == 0) {
				if (isZipFile) {
					ZipInputStream zis = (ZipInputStream) is;
					if (zis.getNextEntry() != null) {
						is = zis;
						reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
						continue;
					}
				}
				key = null;
				value = null;
				return false;
			} else {
                removeNewLineOnLastColumn(value);
				return true;
			}
		}
	}

    private void removeNewLineOnLastColumn(ArrayListTextWritable value) {
        String lastColumn = value.get(value.size()-1).toString();
        if(lastColumn.endsWith("\n"))
            value.set(value.size()-1, new TextSerializable(lastColumn.substring(0,lastColumn.length()-1)));
    }
}

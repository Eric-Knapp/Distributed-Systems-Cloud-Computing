package edu.stevens.cs549.hadoop.pagerank;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;



public class FinMapper extends Mapper<LongWritable, Text, DoubleWritable, Text> {

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException, IllegalArgumentException {
		String line = value.toString(); // Converts Line to a String
		/*
		 * TODO output key:-rank, value: node
		 * See IterMapper for hints on parsing the output of IterReducer.
		 */
		
		System.out.println("line =" + line + "++++++++++");
		String[] secTokens = line.split("\t"); 
        String[] node_RankToks = secTokens[0].split("\\+");
        
        
		String node = node_RankToks[0];
		double rank = Double.valueOf(node_RankToks[1]);
		
		
		context.write(new DoubleWritable ( (-1)*rank), new Text (node));
	}
}
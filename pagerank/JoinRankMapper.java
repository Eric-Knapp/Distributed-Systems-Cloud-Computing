package edu.stevens.cs549.hadoop.pagerank;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class JoinRankMapper extends Mapper<LongWritable, Text, TextPair, Text> {

	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		
		String line = value.toString(); // Converts Line to a String
		String[] secTokens = line.split("\\t"); // Splits it into two parts. Part 1: node | Part 2: rank
		String[] secTokens1 = secTokens[0].split("\\+"); // added
		
		
		context.write(new TextPair(secTokens1[0], "1"), new Text(secTokens1[1]));

	}
}


/*		String[] secTokens = sections[0].split("\\+");
String node = secTokens[0];
double rank = Double.valueOf(secTokens[1]);
String[] adjList = sections[1].toString().trim().split(" ");
double computedWeight = 1.0*rank/adjList.length;
for(int i = 0; i < adjList.length; i++ ) {
	context.write(new Text (adjList[i]), new Text (String.valueOf(computedWeight)));
}
context.write(new Text (node), new Text (PageRankDriver.ADJ_MARKER + sections[1]));

*/
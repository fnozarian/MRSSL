package com.aut.ceit.clustering.mrssl

import org.apache.commons.cli.Options
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.BasicParser

class CommandLine(args: Array[String]) {

  val parser: CommandLineParser = new BasicParser()

  OptionBuilder.withArgName("master")
  OptionBuilder.hasArg()
  OptionBuilder.withDescription("spark master. It can be 'local' 'local[n]' or 'spark://masterIp:Port'")
  val master = OptionBuilder.create("master")
  
  OptionBuilder.withArgName("path")
  OptionBuilder.hasArg()
  OptionBuilder.withDescription("The CSV input data set")
  val input = OptionBuilder.create("input")

  OptionBuilder.withArgName("epsilon")
  OptionBuilder.hasArg()
  OptionBuilder.withDescription("The epsilon radii of data sphere")
  val epsilon = OptionBuilder.create("epsilon")

  OptionBuilder.withArgName("numClusters")
  OptionBuilder.hasArg()
  OptionBuilder.withDescription("number of desired clusters")
  val numClusters = OptionBuilder.create("numClusters")

  OptionBuilder.withArgName("numPoints")
  OptionBuilder.hasArg()
  OptionBuilder.withDescription("number of points in each Box")
  val numPoints = OptionBuilder.create("numPoints")

  OptionBuilder.withArgName("hasClusterLabel")
  OptionBuilder.withDescription("if TRUE then the last column of input is considered as a Cluster of point")
  val hasClusterLabel = OptionBuilder.create("hasClusterLabel")

  OptionBuilder.withArgName("alphaFactor")
  OptionBuilder.hasArg()
  OptionBuilder.withDescription("a factor that affect in tightness of spiral circle of pFollower")
  val alphaFactor = OptionBuilder.create("alphaFactor")
  
  OptionBuilder.withArgName("maxCores")
  OptionBuilder.hasArg()
  OptionBuilder.withDescription("maximum number of cluster cores to be used")
  val maxCores = OptionBuilder.create("maxCores")
  
  val options = new Options()

  options.addOption(master)
  options.addOption(input)
  options.addOption(epsilon)
  options.addOption(numClusters)
  options.addOption(numPoints)
  options.addOption(hasClusterLabel)
  options.addOption(alphaFactor)
  options.addOption(maxCores)
  
  var commandLine = parser.parse(options, args)

  def hasOption(option:String):Boolean={
    commandLine.hasOption(option)
  }
  
  def getOptionValue(option:String):String={
    commandLine.getOptionValue(option)
  }
}
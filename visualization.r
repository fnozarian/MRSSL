require(plotrix)

drawBox <- function (box) {
  lowerLeftX<-box[1]
  lowerLeftY<-box[2]
  upperRightX<-box[3]
  upperRightY<-box[4]
  
  lines (x=c(lowerLeftX, lowerLeftX), y=c(lowerLeftY, upperRightY))
  lines (x=c(lowerLeftX, upperRightX), y=c(upperRightY, upperRightY))
  lines (x=c(upperRightX, upperRightX), y=c(upperRightY, lowerLeftY))
  lines (x=c(upperRightX, lowerLeftX), y=c(lowerLeftY, lowerLeftY))
}

data<-read.csv ("C:\Users\Farzad\cassini1k.org", header=FALSE)
#dsWithExtend <- read.csv("/home/fnozarian/mrssl-debug/DsWithExtend/part-00000",header=FALSE)

colnames(data)<-c("x", "y", "cluster")
cluster.ids<-sort (unique(data$cluster))
scatterplot.colors<-numeric(nrow(data))
barplot.colors<-numeric(length(cluster.ids))
myColors = c(153,652,12,26,31,20,33,43,46,47,51,53,76,84,118,137,188,38,64,29,70)
for (i in 1:length(cluster.ids)) {
  scatterplot.colors[data$cluster == cluster.ids[i]] <- colors()[myColors[i]] 
  barplot.colors[i] <- colors()[myColors[i]]
}
#colnames(dsWithExtend) <- c("x","y","extend")

par (mfrow=c(1,2))

plot (x=data$x, y=data$y, col=scatterplot.colors, asp=1,pch=19, cex=0.08, xlab="X", ylab="Y", main="Clustered points")


#draw.circle(dsWithExtend$x,dsWithExtend$y, 0.08,border="red")
#draw.circle(dsWithExtend$x,dsWithExtend$y,dsWithExtend$extend,border="blue",lty=2)

t<-table(data$cluster)
names(t)[which(names(t)=="0")]<-"Noise"
barplot (t, col=barplot.colors, main="Number of points in each cluster", xlab="Cluster ID", ylab="Number of points")
par(mar=c(0,0,0,0))
legend ("topleft", legend=t, col=barplot.colors, pch=19)

if (file.exists ("/home/fnozarian/mrssl-debug/Boxes/part-00000")) {
  boxes<-read.csv ("/home/fnozarian/mrssl-debug/Boxes/part-00000", header=FALSE)
  for (row in 1:nrow(boxes)) {
    drawBox (boxes[row,])
  }
}
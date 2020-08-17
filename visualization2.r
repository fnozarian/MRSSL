require(plotrix)
data<-read.csv ("/home/fnozarian/9_1M.csv", header=FALSE)
colnames(data)<-c("x", "y")
plot (x=data$x, y=data$y, asp=1,pch=19, cex=0.08, xlab="X", ylab="Y", main="Clustered points")
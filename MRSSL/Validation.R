clustered  <- read.csv("/home/fnozarian/mrssl-debug/ClusteredData/part-00000")
colnames(clustered) <- c("x","y","class","label")
randIX <- RRand(clustered$class,clustered$label)

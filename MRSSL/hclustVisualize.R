x <- read.csv("C:\\Users\\Farzad\\cassini1k.org", header = FALSE)
disX  <- dist(x[sample(nrow(x),500),])
plot(hclust(disX, method = "single"), labels = FALSE)

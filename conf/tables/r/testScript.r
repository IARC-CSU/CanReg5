# This is just a script to test if R is properly installed.
Args <- commandArgs(TRUE)

# Find the directory of the script
#############################################################
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
#############################################################

script.basename <- dirname(script.name)

## Load dependencies
source(paste(sep="/", script.basename, "load.fun.R"))
source(paste(sep="/", script.basename, "checkArgs.R"))
source(paste(sep="/", script.basename, "makeFile.R"))
##

## helper-function
is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 
##

fileType <- checkArgs(Args, "-ft")
out <- checkArgs(Args, "-out")	
	
filename <- makeFile(out, fileType)

# call proper function
# in this test we call the rainbow wheel thingy stolen from the demo(graphics)
par(bg = "gray")
pie(rep(1,24), col = rainbow(24), radius = 0.9)
dev.off()

# write the name of any file created by R to out
cat(paste("-outFile",filename,sep=":"))
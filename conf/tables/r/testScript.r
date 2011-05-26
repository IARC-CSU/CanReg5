
Args <- commandArgs(TRUE)

# Find the directory of the script
#############################################################
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
#############################################################

script.basename <- dirname(script.name)

## Load dependencies
source(paste(sep="/", script.basename, "checkArgs.R"))
##

## helper-function
is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 
##

fileType <- checkArgs(Args, "-ft")
outFileGraphs <- checkArgs(Args, "-outGraph")	
	
if (fileType == "png") {
    filename <- paste( outFileGraphs, ".png", sep = "" )
    png(file=filename, bg="transparent")
} else if (fileType == "pdf") { 
    filename <- paste( outFileGraphs, ".pdf" , sep = "")
    pdf(file=filename) 
} else if (fileType == "svg") { 
	filename <- paste( outFileGraphs, ".svg" , sep = "")
	# svg needs the RSvgDevice library installed
	if(!is.installed("RSvgDevice")){
		load.fun("RSvgDevice")
	}
	require(RSvgDevice)
    devSVG(file=filename, onefile=TRUE)
} else if (fileType == "ps") { 
    filename <- paste( outFileGraphs, ".ps" , sep = "")
    postscript(file=filename) 
} else if (fileType == "html") { 
    outFileTable <- paste( outFileGraphs, ".html" , sep = "")
    # postscript(file=filename) 
} else if (fileType == "wmf") { 
	# This only works on windows
    filename <- paste( outFileGraphs, ".wmf" , sep = "")
    win.metafile(file=filename) 
} else { 
	# defaults to pdf
    filename <- paste(outFileGraphs, ".pdf" , sep = "")
    pdf(file=filename) 
}


# call proper function
# in this test we call the rainbow wheel thingy stolen from the demo(graphics)
par(bg = "gray")
pie(rep(1,24), col = rainbow(24), radius = 0.9)
dev.off()

# write the name of any file created by R to out
cat(filename)

# cat("\n")
# cat(Args[3])
# cat("\n")
# cat(Args[4])
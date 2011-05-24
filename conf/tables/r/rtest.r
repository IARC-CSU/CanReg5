Args <- commandArgs(TRUE)

# 1st argument is either pdf, png, ps or svg
filetype <- Args[1]

# 2nd argument is report file name - or output file name if you want
filename <- Args[2]
outFileTable <- paste(filename, ".html" , sep = "")

# 3rd argument is population file name
filePop <- Args[3]

# 4th argument is incidence file name
fileInc <- Args[4]

#The variable log has to be declared as TRUE or False wether or not the users wants figures with log rates
#Default is FALSE
logr = FALSE

plotOnePage=TRUE

## helper-function
is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 
##

if (filetype == "png") {
    filename <- paste( filename, ".png", sep = "" )
    png(file=filename, bg="transparent")
} else if (filetype == "pdf") { 
    filename <- paste( filename, ".pdf" , sep = "")
    pdf(file=filename) 
} else if (filetype == "svg") { 
	filename <- paste( filename, ".svg" , sep = "")
	# svg needs the RSvgDevice library installed
	if(!is.installed("RSvgDevice")){
		load.fun("RSvgDevice")
	}
	require(RSvgDevice)
    devSVG(file=filename, onefile=TRUE)
} else if (filetype == "ps") { 
    filename <- paste( filename, ".ps" , sep = "")
    postscript(file=filename) 
} else if (filetype == "html") { 
    filename <- paste( filename, ".html" , sep = "")
    # postscript(file=filename) 
} else if (filetype == "wmf") { 
	# This only works on windows
    filename <- paste( filename, ".wmf" , sep = "")
    win.metafile(file=filename) 
} else { 
	# defaults to pdf
    filename <- paste( filename, ".pdf" , sep = "")
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
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
logr = TRUE

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

# Find the directory of the script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

# Load dependencies
source(paste(sep="/", script.basename, "figure_AgeSpecificIncidenceRates.R"))
source(paste(sep="/", script.basename, "subsetSex.R"))	
source(paste(sep="/", script.basename, "mergePeriods.R"))
source(paste(sep="/", script.basename, "makeageSpecIncRates.R"))
source(paste(sep="/", script.basename, "subsetSite.R"))
source(paste(sep="/", script.basename, "plotAgeSpecIncRates.R"))
source(paste(sep="/", script.basename, "plotLogAgeSpecIncRates.r"))
source(paste(sep="/", script.basename, "load.fun.R"))
source(paste(sep="/", script.basename, "makeTable.R"))

dataInc <- read.table(fileInc, header=TRUE)

dataPop <- read.table(filePop, header=TRUE)

if(plotOnePage){	
	op <-	par(mfrow = c(4, 3), no.readonly = FALSE)
	#split.screen(c(3, 3))
}

figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage, outFileTable)
	
if(plotOnePage){
	par(op)
}

figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage, outFileTable)

dev.off()

# write the name of any file created by R to out
cat(filename)

# cat("\n")
# cat(Args[3])
# cat("\n")
# cat(Args[4])
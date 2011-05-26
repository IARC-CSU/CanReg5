############################################################# 
# callAgeSpecificIncidenceRates <- function(Args){
############################################################# 

############################################################# 
Args <- commandArgs(TRUE)
#############################################################
# print(Args)
# Find the directory of the script

#############################################################
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
#############################################################

# script.basename <- dirname("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/hei")
script.basename <- dirname(script.name)

## Load dependencies
source(paste(sep="/", script.basename, "figure_AgeSpecificIncidenceRates.R"))
source(paste(sep="/", script.basename, "subsetSex.R"))	
source(paste(sep="/", script.basename, "mergePeriods.R"))
source(paste(sep="/", script.basename, "makeageSpecIncRates.R"))
source(paste(sep="/", script.basename, "subsetSite.R"))
source(paste(sep="/", script.basename, "plotAgeSpecIncRates.R"))
source(paste(sep="/", script.basename, "plotLogAgeSpecIncRates.r"))
source(paste(sep="/", script.basename, "load.fun.R"))
source(paste(sep="/", script.basename, "makeTable.R"))
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
	# svg needs the RSvgDevice or RSVGTipsDevice library installed
	if(!is.installed("RSVGTipsDevice")){
		load.fun("RSVGTipsDevice")
	}
	require(RSVGTipsDevice)
    devSVGTips(file=filename)
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


	##Incidence file
	fileInc <- checkArgs(Args, "-inc")
	dataInc <- read.table(fileInc, header=TRUE)
	
	##Population file
	filePop <- checkArgs(Args, "-pop")
	dataPop <- read.table(filePop, header=TRUE)
	
	##If and where to plot the tables
	outFileTable <- checkArgs(Args, "-outTable")

	##If plot all the graphs in one window
	plotOnePage <- checkArgs(Args, "-onePage")

	##The variable logr has to be declared as TRUE/FALSE wether or not the users wants figures with log rates
	logr <- checkArgs(Args, "-logr")
	


if(plotOnePage){	
	op <-	par(mfrow = c(4, 3), oma=c(1, 1, 1, 1))
	#split.screen(c(3, 3))
}

figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage, outFileTable)
	
if(plotOnePage){
	par(op)
	figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage = FALSE, outFileTable)
}



dev.off()

# write the name of any file created by R to out
cat(filename)

# cat("\n")
# cat(Args[3])
# cat("\n")
# cat(Args[4])

############################################################# 
#}#End function
############################################################# 
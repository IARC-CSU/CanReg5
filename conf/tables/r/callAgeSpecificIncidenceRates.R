#remove(list = ls())

#source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/callAgeSpecificIncidenceRates.R")
#callAgeSpecificIncidenceRates(c("-ft=pdf", "-war=C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/warning.txt", "-out=C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/test", "-pop=C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/pop1404377193814741326.tsv", "-inc=C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/inc5404848408254658419.tsv", "-logr"))
#callAgeSpecificIncidenceRates(c("-ft=pdf", "-out=C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/test", "-pop=C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/pop1404377193814741326.tsv", "-inc=C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/inc5404848408254658419.tsv"))



############################################################# 
#callAgeSpecificIncidenceRates <- function(Args){
############################################################# 

############################################################# 
Args <- commandArgs(TRUE)
#############################################################

# Find the directory of the script

#############################################################
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)
#############################################################
#script.basename <- dirname("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/hei")
##############################################################

## Load dependencies
source(paste(sep="/", script.basename, "figure_AgeSpecificIncidenceRates.R"))
source(paste(sep="/", script.basename, "subsetSex.R"))	
source(paste(sep="/", script.basename, "mergePeriods.R"))
source(paste(sep="/", script.basename, "makeageSpecIncRates.R"))
source(paste(sep="/", script.basename, "subsetSite.R"))
source(paste(sep="/", script.basename, "plotAgeSpecIncRates.R"))
source(paste(sep="/", script.basename, "plotLogAgeSpecIncRates.r"))
source(paste(sep="/", script.basename, "makeTable.R"))
source(paste(sep="/", script.basename, "checkArgs.R"))
source(paste(sep="/", script.basename, "load.fun.R"))
##

## helper-function
is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 
##



##OutFile
out <- checkArgs(Args, "-out")


fileType <- checkArgs(Args, "-ft")

if(fileType %in% c("txt", "html")){
filename <- paste(out, ".", sep = "" )
filename <- paste(filename, fileType, sep = "" )
plotTables <- TRUE
}else{
plotTables <- FALSE
outFile <- "noFile"
}


if(!plotTables){
	if (fileType == "png") {
		filename <- paste( out, "%03d.png", sep = "" )
		print(paste( out, "001.png", sep = "" ))
		png(file=filename, bg="transparent")
	} else if (fileType == "pdf") { 
		filename <- paste( out, ".pdf" , sep = "")
		pdf(file=filename) 	
	} else if (fileType == "ps") { 
		filename <- paste( out, ".ps" , sep = "")
		postscript(file=filename, onefile = TRUE) 
	} else if (fileType == "html") { 
		filename <- paste( out, ".html" , sep = "")
	} else if (fileType == "wmf") { 
		# This only works on windows
		filename <- paste( out, ".wmf" , sep = "")
		win.metafile(file=filename) 
	} else { 
		# defaults to pdf
		filename <- paste(out, ".pdf" , sep = "")
		pdf(file=filename) 
	}
}#End if !plotTables

	##Incidence file
	fileInc <- checkArgs(Args, "-inc")
	dataInc <- read.table(fileInc, header=TRUE)
	
	##Population file
	filePop <- checkArgs(Args, "-pop")
	dataPop <- read.table(filePop, header=TRUE)
	

	##If plot all the graphs in one window
	plotOnePage <- checkArgs(Args, "-onePage")

	##The variable logr has to be declared as TRUE/FALSE wether or not the users wants figures with log rates
	logr <- checkArgs(Args, "-logr")
	


if(plotOnePage){	
	op <-	par(mfrow = c(4, 3), oma=c(1, 1, 1, 1))
	#split.screen(c(3, 3))
}

##WarningFile - file for warning messages
warningFile <-  paste(out, "WARNING", sep = "")
warningFile <-  paste(warningFile, ".txt", sep = "")


figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage, filename, warningFile, plotTables, fileType)
	
if(plotOnePage){
	par(op)
	figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage = FALSE, filename, warningFile, plotTables, fileType)
}


#if(!plotTables){
dev.off()
#}

# write the name of any file created by R to out
cat(filename)

# cat("\n")
# cat(Args[3])
# cat("\n")
# cat(Args[4])

############################################################# 
#}#End function
############################################################# 
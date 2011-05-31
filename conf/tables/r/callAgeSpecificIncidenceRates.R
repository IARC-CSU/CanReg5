Args <- commandArgs(TRUE)

## Find directory of script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

##Load dependencies
source(paste(sep="/", script.basename, "figure_AgeSpecificIncidenceRates.R"))
source(paste(sep="/", script.basename, "subsetSex.R"))	
source(paste(sep="/", script.basename, "mergePeriods.R"))
source(paste(sep="/", script.basename, "makeageSpecIncRates.R"))
source(paste(sep="/", script.basename, "subsetSite.R"))
source(paste(sep="/", script.basename, "plotAgeSpecIncRates.R"))
source(paste(sep="/", script.basename, "plotLogAgeSpecIncRates.r"))
source(paste(sep="/", script.basename, "makeTable.R"))
source(paste(sep="/", script.basename, "checkArgs.R"))
source(paste(sep="/", script.basename, "load.fun.R")) # makeFile needs this
source(paste(sep="/", script.basename, "makeFile.R"))

##OutFile
out <- checkArgs(Args, "-out")

fileType <- checkArgs(Args, "-ft")

##If fileType is "txt" or "html" tables will be built
if(fileType %in% c("txt", "html")){
	filename <- paste(out, ".", sep = "" )
	filename <- paste(filename, fileType, sep = "" )
	plotTables <- TRUE
}else{
	plotTables <- FALSE
	outFile <- "noFile"
}

##If tables should not be built, figures will be made
if(!plotTables){

	filename <- makeFile(out, fileType)
	
}##End if !plotTables

##Incidence file
fileInc <- checkArgs(Args, "-inc")
dataInc <- read.table(fileInc, header=TRUE)
	
##Population file
filePop <- checkArgs(Args, "-pop")
dataPop <- read.table(filePop, header=TRUE)

##If (plot all figures in one window) with the following row and col numbers to split the page
plotOnePage <- checkArgs(Args, "-onePage")
	
if(!(plotOnePage == FALSE)){
	onePageRow <- as.numeric(strsplit(plotOnePage, split = "x")[[1]][1])
	onePageCol <- as.numeric(strsplit(plotOnePage, split = "x")[[1]][2])
	plotOnePage <-  TRUE
}

##If variable "-logr" is given as an argument, rates are given on log-scale
logr <- checkArgs(Args, "-logr")

if(plotOnePage){	
	op <-	par(mfrow = c(onePageRow, onePageCol), oma=c(1, 1, 1, 1))
}

##WarningFile - file for warning messages
warningFile <-  paste(out, "WARNING", sep = "")
warningFile <-  paste(warningFile, ".txt", sep = "")

figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage, filename, warningFile, plotTables, fileType)
	
if(plotOnePage){
    par(op)
    if (fileType != "svg") {
	figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage = FALSE, filename, warningFile, plotTables, fileType)
    }
}

if(!plotTables){
dev.off()
}

##Write the name the file created by R to out
cat(paste("-outFile",filename,sep=":"))

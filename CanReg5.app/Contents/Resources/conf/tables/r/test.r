
test <- function(Args){

print(Args)

	fileType <- checkArgs(Args, "fileType")

	print("fileType")
	print(fileType)
	
	outFileGraphs <- checkArgs(Args, "outFileGraphs")	
	
	print(outFileGraphs)
	
	if (fileType == "png") {
	    filename <- paste( outFileGraphs, ".png", sep = "" )
	    png(file=filename, bg="transparent")
	print(filename)
	
	} else if (fileType == "pdf") { 
	    filename <- paste( outFileGraphs, ".pdf" , sep = "")
	print(filename)
	    pdf(file=filename) 

	
	} else { 
	    filename <- paste( outFileGraphs, ".png" , sep = "")
	    png(file=filename, bg="transparent") 
	}


	
	fileInc <- checkArgs(Args, "fileInc")

	dataInc <- read.table(fileInc, header=TRUE)
	
	
	filePop <- checkArgs(Args, "filePop")

	dataPop <- read.table(filePop, header=TRUE)
	

	outFileTable <- checkArgs(Args, "outFileTable")
	plotOnePage <- checkArgs(Args, "plotOnePage")

	#The variable logr has to be declared as TRUE/FALSE wether or not the users wants figures with log rates
	logr <- checkArgs(Args, "logr")
	plotTable <- checkArgs(Args, "plotTable")
	#outFileTable = "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/filename3.html"
	#outFileGraphs <- "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/test.pdf"


	#fileInc <- "C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/inc5404848408254658419.tsv"		
	#filePop <- "C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/pop1404377193814741326.tsv"
	#dataInc <- read.table(fileInc, header=TRUE)
	#dataPop <- read.table(filePop, header=TRUE)

	#pdf(outFileGraphs)

	if(plotOnePage){
	
	op <-	par(mfrow = c(4, 3), oma=c(1, 1, 1, 1))

	}

 	figure_AgeSpecificIncidenceRates(dataInc, dataPop, logr, plotOnePage, outFileTable)
	

	if(plotOnePage){

	par(op)

	}

	dev.off()	

}	
		
	



#checkArgs(c(plotOnePage = TRUE, logr = TRUE, outFileGraphs = "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/test", 
#outFileTable = "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/filename3.html",
#fileInc = "C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/inc5404848408254658419.tsv", 
#filePop = "C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/pop1404377193814741326.tsv", fileType = "pdf"), "fileType")#




remove(list = ls())

	

	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/figure_AgeSpecificIncidenceRates.R")
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/subsetSex.R")	
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/mergePeriods.R")
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/makeageSpecIncRates.R")
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/subsetSite.R")
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/plotAgeSpecIncRates.R")
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/plotLogAgeSpecIncRates.R")
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/makeTable.R")	
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/checkArgs.R")
	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/test.R")

test(c(plotOnePage = TRUE, logr = TRUE, outFileGraphs = "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/test1", 
outFileTable = "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/filename3.html",
fileInc = "C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/inc5404848408254658419.tsv", 
filePop = "C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/pop1404377193814741326.tsv", fileType = "pdf"))
 
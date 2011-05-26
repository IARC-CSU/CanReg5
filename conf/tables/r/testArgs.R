

remove(list = ls())

	

	source("C:/Documents and Settings/CinUser/My Documents/Anahita/Rcode/callAgeSpecificIncidenceRates.R")

callAgeSpecificIncidenceRates (c(plotOnePage = FALSE, logr = FALSE, outFileGraphs = "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/test1", 
outFileTable = "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/filename3.html",
fileInc = "C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/inc5404848408254658419.tsv", 
filePop = "C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/pop1404377193814741326.tsv", fileType = "pdf"))
 
#"--args -ft pdf -out "C:\Documents and Settings\ervikm\Desktop\test\New Folder\test" -pop "C:\DOCUME~1\ervikm\LOCALS~1\Temp\pop2629626338195694907.tsv" -inc "C:\DOCUME~1\ervikm\LOCALS~1\Temp\inc1365393216629071330.tsv" -logr TRUE -onepage TRUE  


system(paste(commandArgs(), collapse=" "))

Args <- commandArgs(--args -ft pdf -out "C:\Documents and Settings\ervikm\Desktop\test\New Folder\test" -pop "C:\DOCUME~1\ervikm\LOCALS~1\Temp\pop2629626338195694907.tsv" -inc "C:\DOCUME~1\ervikm\LOCALS~1\Temp\inc1365393216629071330.tsv" -logr TRUE -onepage TRUE)
 


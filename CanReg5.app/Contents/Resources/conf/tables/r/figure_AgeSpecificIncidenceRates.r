figure_AgeSpecificIncidenceRates <- function(dataInc, dataPop, logr, plotOnePage, outFile, warningFile, plotTables, fileType){

if(file.exists(warningFile)){

	file.remove(warningFile)

}
	
dataInc <- subset(dataInc, select = c(YEAR, ICD10GROUP, ICD10GROUPLABEL, SEX, AGE_GROUP, CASES))

if(plotOnePage){
	makeTitleOnce <<- TRUE
}else{
	makeTitleOnce <<- FALSE
}
	
##Number of sites given in the file
nrSites <- nlevels(dataInc$ICD10GROUP)
##Number of agegroups
nrOfAgeGroups <- nlevels(as.factor(dataPop$AGE_GROUP))
##List of sites
listSites <- levels(dataInc$ICD10GROUP)

##Pop-data for males
dataPopMale <- subsetSex(dataPop, 1)
dataPopMale <- mergePeriods(dataPopMale, "Pop", nrOfAgeGroups)
##Pop-data for females
dataPopFemale <- subsetSex(dataPop, 2)	
dataPopFemale <- mergePeriods(dataPopFemale, "Pop", nrOfAgeGroups)

for(i in 1:nrSites){
	
	site <- listSites[i]

	siteLabel <- as.vector(dataInc$ICD10GROUPLABEL[which(dataInc$ICD10GROUP == site)])[1]	
	
	#Extract the first two characters from a string
	siteCx <- substr(siteLabel, 1, 2)
	siteName <- substr(siteLabel, 4, nchar(siteLabel))

	makeAgeSpecIncRates(dataInc, dataPopMale, dataPopFemale, site, siteCx, nrOfAgeGroups, logr, outFile, siteName, plotOnePage, warningFile, plotTables, i, fileType)	

}#End for nrSites	
	
}#End function
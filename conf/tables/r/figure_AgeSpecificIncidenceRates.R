#Script for graphs of age specific incidence rates per 100 000.

figure_AgeSpecificIncidenceRates <- function(dataInc, dataPop, logr){

	
	dataInc <- subset(dataInc, select = c(YEAR, ICD10GROUP, ICD10GROUPLABEL, SEX, AGE_GROUP, CASES))
	
	#One frame with the number sites given in the file
	nrSites <- nlevels(dataInc$ICD10GROUP)

	##############################################split.screen(c(ceiling(nrSites/2), floor(nrSites/2)))

	#Number of agegroups
	nrOfAgeGroups <- nlevels(as.factor(dataPop$AGE_GROUP))

	#List of the sites
	listSites <- levels(dataInc$ICD10GROUP)
	siteLabel <- levels(dataInc$ICD10GROUPLABEL)

	
	dataPopMale <- subsetSex(dataPop, 1)
	dataPopMale <- mergePeriods(dataPopMale, "Pop", nrOfAgeGroups)

	dataPopFemale <- subsetSex(dataPop, 2)	
	dataPopFemale <- mergePeriods(dataPopFemale, "Pop", nrOfAgeGroups)
	
	for(i in 1:nrSites){
	
	#site <- substr(listSites[i], 4, nchar(listSites[i]))
	site <- listSites[i]
	
	#Extract the first two characters from a string
	siteCx <- substr(siteLabel[i], 1, 2)

	makeAgeSpecIncRates(dataInc, dataPopMale, dataPopFemale, site, siteCx, nrOfAgeGroups, logr)	
	
	}#End for nrSites	
		
	##############################################close.screen(all = TRUE)  


}#End function
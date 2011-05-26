#Script for graphs of age specific incidence rates per 100 000.

figure_AgeSpecificIncidenceRates <- function(dataInc, dataPop, logr, plotOnePage, outFileTable){

	
	dataInc <- subset(dataInc, select = c(YEAR, ICD10GROUP, ICD10GROUPLABEL, SEX, AGE_GROUP, CASES))


	if(plotOnePage){
	makeTitleOnce <<- TRUE
	}else{
	makeTitleOnce <<- FALSE
	}
	
	#One frame with the number sites given in the file
	nrSites <- nlevels(dataInc$ICD10GROUP)

	#if(plotOnePage){
	
	#par(no.readonly = TRUE)


	#split.screen(c(3, 3))
	#}
	
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
	
	#if(plotOnePage){
	#screen(i)
	#}
	#site <- substr(listSites[i], 4, nchar(listSites[i]))
	site <- listSites[i]
	
	#Extract the first two characters from a string
	siteCx <- substr(siteLabel[i], 1, 2)
	siteName <- substr(siteLabel[i], 4, nchar(siteLabel[i]))
	
	# print(siteName)
	makeAgeSpecIncRates(dataInc, dataPopMale, dataPopFemale, site, siteCx, nrOfAgeGroups, logr, outFileTable, siteName, plotOnePage)	
	
	}#End for nrSites	
		
	##############################################close.screen(all = TRUE)  

	#if(plotOnePage){
	#close.screen(all = TRUE)
	#}
	
}#End function
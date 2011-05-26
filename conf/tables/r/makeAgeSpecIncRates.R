makeAgeSpecIncRates <- function(dataInc, dataPopMale, dataPopFemale, site, siteCx, nrOfAgeGroups, logr, outFile, siteName, plotOnePage, warningFile, plotTables, i, fileType){
			

      ageGrLabel <- dataPopFemale$AGE_GROUP_LABEL

	#Subset site
	dataInc <- subsetSite(dataInc, site)

	if(siteCx == "01"){#Plotter kun kvinner
	
	dataIncFemale <- subsetSex(dataInc, 2)
	dataIncFemale <- mergePeriods(dataIncFemale,"Inc", nrOfAgeGroups)

if( sum(dataIncFemale$CASES)<10 ){cat( c("WARNING: sparse data for", siteName, "(", site, ")", "for females.\n"), file = warningFile , append = TRUE) }

	
	dataFemaleRates <- cbind(dataIncFemale, dataPopFemale$COUNT, as.numeric(round((dataIncFemale$CASES/dataPopFemale$COUNT), 4)), as.numeric(round((dataIncFemale$CASES/dataPopFemale$COUNT)*100000,4)))
	colnames(dataFemaleRates)  <- (c(colnames(dataIncFemale), "COUNTS", "RATES", "RATESper100000"))
	
	if(logr){
	plotLogAgeSpecIncRates("0", dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
	} else {	
	plotAgeSpecIncRates("0", dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)
	}
	
	}else if(siteCx == "10"){
	
	dataIncMale <- subsetSex(dataInc, 1)
	dataIncMale <- mergePeriods(dataIncMale, "Inc", nrOfAgeGroups)
	dataMaleRates <- cbind(dataIncMale, dataPopMale$COUNT, as.numeric(round(dataIncMale$CASES/dataPopMale$COUNT, 4)), as.numeric(round((dataIncMale$CASES/dataPopMale$COUNT)*100000, 4)))
	colnames(dataMaleRates)  <- (c(colnames(dataIncMale), "COUNTS", "RATES", "RATESper100000"))

if( sum(dataIncMale$CASES)<10 ){cat( c("WARNING: sparse data for", siteName, "(", site, ")", "for males.\n"), file = warningFile , append = TRUE) }


	if(logr){
	plotLogAgeSpecIncRates(dataMaleRates, "0", site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
	} else {	
	plotAgeSpecIncRates(dataMaleRates, "0", site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)
	}

	}else if(siteCx == "11"){
	

	dataIncMale <- subsetSex(dataInc, 1)
	dataIncFemale <- subsetSex(dataInc, 2)
	dataIncFemale <- mergePeriods(dataIncFemale,"Inc", nrOfAgeGroups)
	dataIncMale <- mergePeriods(dataIncMale, "Inc", nrOfAgeGroups)

	dataMaleRates <- cbind(dataIncMale, dataPopMale$COUNT, as.numeric(round(dataIncMale$CASES/dataPopMale$COUNT, 4)), as.numeric(round((dataIncMale$CASES/dataPopMale$COUNT)*100000, 4)))
	colnames(dataMaleRates)  <- (c(colnames(dataIncMale), "COUNTS", "RATES", "RATESper100000"))

	dataFemaleRates <- cbind(dataIncFemale, dataPopFemale$COUNT, as.numeric(round(dataIncFemale$CASES/dataPopFemale$COUNT, 4)),as.numeric(round((dataIncFemale$CASES/dataPopFemale$COUNT)*100000, 4)))
	colnames(dataFemaleRates)  <- (c(colnames(dataIncFemale), "COUNTS", "RATES", "RATESper100000"))

if( sum(dataIncMale$CASES)<10 ){cat( c("WARNING: sparse data for", siteName, "(", site, ")", "for males.\n"), file = warningFile , append = TRUE) }
if( sum(dataIncFemale$CASES)<10 ){cat( c("WARNING: sparse data for", siteName, "(", site, ")", "for females.\n"), file = warningFile , append = TRUE) }
	
	if(logr){
	plotLogAgeSpecIncRates(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
	} else {	
	plotAgeSpecIncRates(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	

	}

#)

	}#End else if sitex
}#End function makeAgeSpecIncRates
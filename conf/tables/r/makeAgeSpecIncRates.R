makeAgeSpecIncRates <- function(dataInc, dataPopMale, dataPopFemale, site, siteCx, nrOfAgeGroups, logr, outFileTable){




	ageGrLabel <- dataPopFemale$AGE_GROUP_LABEL

	#Subset site
	dataInc <- subsetSite(dataInc, site)

	if(siteCx == "01"){#Plotter kun kvinner
	
	dataIncFemale <- subsetSex(dataInc, 2)
	dataIncFemale <- mergePeriods(dataIncFemale,"Inc", nrOfAgeGroups)
	dataFemaleRates <- cbind(dataIncFemale, dataPopFemale$COUNT, (dataIncFemale$CASES/dataPopFemale$COUNT),(dataIncFemale$CASES/dataPopFemale$COUNT)*100000)
	colnames(dataFemaleRates)  <- (c(colnames(dataIncFemale), "COUNTS", "RATES", "RATESper100000"))
	
	if(logr){
	plotLogAgeSpecIncRates("0", dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFileTable)	
	} else {	
	plotAgeSpecIncRates("0", dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFileTable)
	}
	
	}else if(siteCx == "10"){
	
	dataIncMale <- subsetSex(dataInc, 1)
	dataIncMale <- mergePeriods(dataIncMale, "Inc", nrOfAgeGroups)
	dataMaleRates <- cbind(dataIncMale, dataPopMale$COUNT, (dataIncMale$CASES/dataPopMale$COUNT), (dataIncMale$CASES/dataPopMale$COUNT)*100000)
	colnames(dataMaleRates)  <- (c(colnames(dataIncMale), "COUNTS", "RATES", "RATESper100000"))

	if(logr){
	plotLogAgeSpecIncRates(dataMaleRates, "0", site, nrOfAgeGroups, ageGrLabel, outFileTable)	
	} else {	
	plotAgeSpecIncRates(dataMaleRates, "0", site, nrOfAgeGroups, ageGrLabel, outFileTable)
	}

	}else if(siteCx == "11"){
	
	dataIncMale <- subsetSex(dataInc, 1)
	dataIncFemale <- subsetSex(dataInc, 2)
	dataIncFemale <- mergePeriods(dataIncFemale,"Inc", nrOfAgeGroups)
	dataIncMale <- mergePeriods(dataIncMale, "Inc", nrOfAgeGroups)

	dataMaleRates <- cbind(dataIncMale, dataPopMale$COUNT, (dataIncMale$CASES/dataPopMale$COUNT),(dataIncMale$CASES/dataPopMale$COUNT)*100000)
	colnames(dataMaleRates)  <- (c(colnames(dataIncMale), "COUNTS", "RATES", "RATESper100000"))

	dataFemaleRates <- cbind(dataIncFemale, dataPopFemale$COUNT, (dataIncFemale$CASES/dataPopFemale$COUNT),(dataIncFemale$CASES/dataPopFemale$COUNT)*100000)
	colnames(dataFemaleRates)  <- (c(colnames(dataIncFemale), "COUNTS", "RATES", "RATESper100000"))

	if(logr){
	plotLogAgeSpecIncRates(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFileTable)	
	} else {	
	plotAgeSpecIncRates(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFileTable)	

	}

#)

	}#End else if sitex
}#End function makeAgeSpecIncRates
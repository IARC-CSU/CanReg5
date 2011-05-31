makeAgeSpecIncRates <- function(dataInc, dataPopMale, dataPopFemale, site, siteCx, nrOfAgeGroups, logr, outFile, siteName, plotOnePage, warningFile, plotTables, i, fileType){
			
ageGrLabel <- dataPopFemale$AGE_GROUP_LABEL
##Subset site
dataInc <- subsetSite(dataInc, site)

if(siteCx == "01"){##Figures for women only
	
	dataIncFemale <- subsetSex(dataInc, 2)
	dataIncFemale <- mergePeriods(dataIncFemale,"Inc", nrOfAgeGroups)
	dataIncFemale$YEAR <- dataPopFemale$YEAR

	if( sum(dataIncFemale$CASES)<1 ){##If data is empty, figures is not created and warningmsg. is created

		cat( c("WARNING: 0 cases for", siteName, "(", site, ")", "for females.\n"), file = warningFile , append = TRUE) 

	}else{
	
	dataFemaleRates <- cbind(dataIncFemale, dataPopFemale$COUNT, as.numeric(round((dataIncFemale$CASES/dataPopFemale$COUNT), 4)), as.numeric(round((dataIncFemale$CASES/dataPopFemale$COUNT)*100000,4)))
	colnames(dataFemaleRates)  <- (c(colnames(dataIncFemale), "COUNTS", "RATES", "RATESper100000"))
	dataFemaleRates <- subset(dataFemaleRates, select = -RATES)

		if(logr){
			plotLogAgeSpecIncRates("0", dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
		} else {	
			plotAgeSpecIncRates("0", dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)
		}##End if logr

	}
	
}else if(siteCx == "10"){##Figures for men only
	
	dataIncMale <- subsetSex(dataInc, 1)
	dataIncMale <- mergePeriods(dataIncMale, "Inc", nrOfAgeGroups)
	dataIncMale$YEAR <- dataPopMale$YEAR
	dataMaleRates <- cbind(dataIncMale, dataPopMale$COUNT, as.numeric(round(dataIncMale$CASES/dataPopMale$COUNT, 4)), as.numeric(round((dataIncMale$CASES/dataPopMale$COUNT)*100000, 4)))
	colnames(dataMaleRates)  <- (c(colnames(dataIncMale), "COUNTS", "RATES", "RATESper100000"))
	dataMaleRates <- subset(dataMaleRates, select = -RATES)

	if( sum(dataIncMale$CASES)<1 ){
	
		cat( c("WARNING: 0 cases for", siteName, "(", site, ")", "for males.\n"), file = warningFile , append = TRUE) 
	
	}else{
	
		if(logr){
			plotLogAgeSpecIncRates(dataMaleRates, "0", site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
		} else {	
			plotAgeSpecIncRates(dataMaleRates, "0", site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)
		}

	}#End else

}else if(siteCx == "11"){##Figures for both sexes
	
	dataIncMale <- subsetSex(dataInc, 1)
	dataIncFemale <- subsetSex(dataInc, 2)
	dataIncFemale <- mergePeriods(dataIncFemale,"Inc", nrOfAgeGroups)
	dataIncFemale$YEAR <- dataPopFemale$YEAR
	dataIncMale <- mergePeriods(dataIncMale, "Inc", nrOfAgeGroups)
	dataIncMale$YEAR <- dataPopMale$YEAR

	dataMaleRates <- cbind(dataIncMale, dataPopMale$COUNT, as.numeric(round(dataIncMale$CASES/dataPopMale$COUNT, 4)), as.numeric(round((dataIncMale$CASES/dataPopMale$COUNT)*100000, 4)))
	colnames(dataMaleRates)  <- (c(colnames(dataIncMale), "COUNTS", "RATES", "RATESper100000"))
	dataMaleRates <- subset(dataMaleRates, select = -RATES)
	
	dataFemaleRates <- cbind(dataIncFemale, dataPopFemale$COUNT, as.numeric(round(dataIncFemale$CASES/dataPopFemale$COUNT, 4)),as.numeric(round((dataIncFemale$CASES/dataPopFemale$COUNT)*100000, 4)))
	colnames(dataFemaleRates)  <- (c(colnames(dataIncFemale), "COUNTS", "RATES", "RATESper100000"))
	dataFemaleRates <- subset(dataFemaleRates, select = -RATES)

		if( sum(dataIncMale$CASES)<1 & sum(dataIncFemale$CASES)>1){

			cat( c("WARNING: 0 cases for", siteName, "(", site, ")", "for males.\n"), file = warningFile , append = TRUE) 

			if(logr){
				plotLogAgeSpecIncRates("0", dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
			} else {	
				plotAgeSpecIncRates("0", dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
			}

		}else if( sum(dataIncFemale$CASES)<1 & sum(dataIncMale$CASES)>1){

		cat( c("WARNING: 0 cases for", siteName, "(", site, ")", "for females.\n"), file = warningFile , append = TRUE)

			if(logr){
				plotLogAgeSpecIncRates(dataMaleRates, "0", site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
			} else {	
				plotAgeSpecIncRates(dataMaleRates, "0", site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
			}

		}else if( sum(dataIncFemale$CASES)<1 & sum(dataIncMale$CASES)<1){

			cat( c("WARNING: 0 cases for", siteName, "(", site, ")", "for males.\n"), file = warningFile , append = TRUE) 
			cat( c("WARNING: 0 cases for", siteName, "(", site, ")", "for females.\n"), file = warningFile , append = TRUE)

		}else if( sum(dataIncFemale$CASES)>1 & sum(dataIncMale$CASES)>1){

			if(logr){
				plotLogAgeSpecIncRates(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
			} else {	
				plotAgeSpecIncRates(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFile, siteName, plotOnePage, plotTables, i, fileType)	
			}		

		}			

	}#End else if sitex
}#End function makeAgeSpecIncRates
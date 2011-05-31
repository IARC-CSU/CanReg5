mergePeriods <- function(data, popOrInc, nrOfAgeGroups){

##Periods given in file
periods <- levels(as.factor(data$YEAR))
nrOfPeriods <- length(periods)

period1 <- periods[1]

	if(nrOfPeriods == 1){
		timePeriod <- period1
	}else{
		period2 <- periods[nrOfPeriods]
		timePeriod <- paste(period1, "-", period2)
	}#End if else period1=period2
	
#Number of cols and rows 
nrCol <- ncol(data)
nrRow <- nrOfAgeGroups

newData <- as.data.frame(matrix(ncol = nrCol, nrow = nrRow))
colnames(newData) <- colnames(data)

newData$YEAR <- timePeriod
newData$SEX <- data$SEX[1]
newData$AGE_GROUP <- c(0:(nrOfAgeGroups-1))

	if(popOrInc=="Inc"){
		newData$ICD10GROUP <- data$ICD10GROUP[1]
		newData$SEX <- data$SEX[1]
		newData$ICD10GROUPLABEL <- data$ICD10GROUPLABEL[1]
	}else{
		newData$AGE_GROUP_LABEL <- as.vector(data$AGE_GROUP_LABEL[1:nrOfAgeGroups])
	}

	for(i in 0:(nrOfAgeGroups-1)){

	tempData <- subset(data, data$AGE_GROUP == i)

	
	##If no cases, the nr. of cases are given a value 0
	if(popOrInc == "Inc"){
	
		if(!is.na(sum(tempData$CASES[tempData$AGE_GROUP == i]))){	

			newNrCases <- sum(tempData$CASES)	
			newData$CASES[i+1] <- newNrCases 

		}else {

			newData$COUNT[i+1] <- 0

		}#end if is.na(sum..
	
	}else if(popOrInc == "Pop"){

	newNrCounts <- sum(tempData$COUNT)
	newData$COUNT[i+1] <- newNrCounts

	}##End if, popOrInc

}##End for age

return(newData)

}##End funtion

mergePeriods <- function(data, popOrInc){

# print(data)
#Nr of periods 
nrPeriods <- length(levels(as.factor(data$YEAR)))

#What are the periods
periods <- levels(as.factor(data$YEAR))
nrOfPeriods <- length(periods)
period1 <- periods[1]
period2 <- periods[nrOfPeriods]
#print(period1)
#print(period2)
#print("HER BLIR DET FEIL")	
	if(is.na(period2)){
		timePeriod <- period1
	}else{
		timePeriod <- paste(period1, "-", period2)
	}#End if else period1=period2
	
#print("HER BLIR DET FEIL")	
#Create a new data file with merged periods

#Number of colomns and rows (There will be 17 rows (17 age groups)
nrCol <- ncol(data)
nrRow <- 17
newData <- as.data.frame(matrix(ncol = nrCol, nrow = nrRow))
colnames(newData) <- colnames(data)
#newData <- data[1:nrRow, 1:nrCol]
newData$YEAR <- timePeriod
newData$SEX <- data$SEX[1]

	if(popOrInc=="Inc"){
	newData$ICD10GROUP <- data$ICD10GROUP[1]
	newData$SEX <- data$SEX[1]
	newData$AGEGROUP <- c(0: 16)
	}else{
	newData$AGE_GROUP <- c(0: 16)
	}
	

#Det number of ageGroups should be 0-16 representing 5 year intervals 0-4, 5-9, ..., 80+ 
for(i in 0:16){

tempData <- subset(data, data$AGE == i)
#print(data)

	#Make sure there exists cases for the specific agegroup
	#if not the nr. of cases are given a value 0
	if(!is.na(sum(tempData$CASES[tempData$AGE == i]))){	

	if(popOrInc == "Inc"){

	newNrCases <- sum(tempData$CASES)
	newData$CASES[i+1] <- newNrCases 

	}else if(popOrInc == "Pop"){

	newNrCounts <- sum(tempData$COUNT)
	newData$COUNT[i+1] <- newNrCounts


	}#End if, else tempData

}else {

		newData$COUNT[i+1] <- 0

}

}#End for age
#print(newData)
return(newData)

}#End funtion

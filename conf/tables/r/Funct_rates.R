
# Calculates age specific incidence rates for all sites
CalcAgeSpecRates <- function(dataInc, dataPop){

	# Restricting files to used variables
		dataInc <- subset(dataInc, select = c("ICD10GROUP", "ICD10GROUPLABEL", "SEX", "AGE_GROUP", "CASES"))
		dataPop <- subset(dataPop, select = c("AGE_GROUP", "SEX", "COUNT"))
		
	# Formatting variables	
		dataInc$CASES <- as.numeric(dataInc$CASES)
		dataPop$COUNT <- as.numeric(dataPop$COUNT)	
			
	## AGE SPECIFIC RATES	
			
	# Aggregating the number of cases in the incidence/population files
		dataInc <- aggregate(dataInc$CASES, by=list(dataInc$ICD10GROUP,dataInc$ICD10GROUPLABEL,dataInc$SEX,dataInc$AGE_GROUP), FUN=sum)
		colnames(dataInc) <- c("ICD10GROUP", "ICD10GROUPLABEL", "SEX", "AGE_GROUP", "CASES")
		
		dataPop <- aggregate(dataPop$COUNT, by=list(dataPop$AGE_GROUP,dataPop$SEX), FUN=sum)
		colnames(dataPop) <- c("AGE_GROUP", "SEX", "COUNT")
		
	# Merging incidence and population
		data <- merge(dataInc,dataPop,by=c("AGE_GROUP","SEX"))
		
	# Calculate Age Spec Rates
		data$RATE <- round((data$CASES / data$COUNT) * 100000,2)
			
	# Return data frame		
		return(data)

}

# Calculates CRUDE incidence rates for all sites
CalcCrudeRates <- function(dataInc, dataPop){

	## CRUDE RATES	
		
	# Aggregating the number of cases in the incidence/population files
		dataInc <- subset(dataInc, select = c("ICD10GROUP", "ICD10GROUPLABEL", "SEX", "CASES"))
		dataInc <- aggregate(dataInc$CASES, by=list(dataInc$ICD10GROUP,dataInc$ICD10GROUPLABEL,dataInc$SEX), FUN=sum)
		colnames(dataInc) <- c("ICD10GROUP", "ICD10GROUPLABEL", "SEX", "CASES")
		
		dataPop <- subset(dataPop, select = c("SEX", "COUNT"))
		dataPop <- aggregate(dataPop$COUNT, by=list(dataPop$SEX), FUN=sum)
		colnames(dataPop) <- c("SEX", "COUNT")	
		
	# Merging incidence and population
		dataCR <- merge(dataInc,dataPop,by=c("SEX"))
		
	# Calculate Age Spec Rates
		dataCR$CRUDE <- round((dataCR$CASES / dataCR$COUNT) * 100000,2)	
	
	# Sorting by sex and rate desc
		dataCR <- dataCR[order(dataCR$SEX, -dataCR$CRUDE), ]
	
	# Return data frame		
		return(dataCR)
	
}

# Calculates ASR for all sites
CalcASR <- function(dataInc, dataPop, standpop, strat=c("SEX")){

  # Aggregating the incidence data 
		stratInc <- c("ICD10GROUP",strat,"AGE_GROUP")
		dataInc <- as.data.frame(aggregate(dataInc$CASES,by=eval(parse(text=paste("list(",paste(paste("dataInc$",stratInc,sep=""),collapse=","),")",sep=""))),FUN=sum, na.rm=TRUE))
		colnames(dataInc) <- c(stratInc,"CASES")

	# Aggregating the population data 
		stratPop <- c(strat,"AGE_GROUP")
		dataPop <- as.data.frame(aggregate(dataPop$COUNT,by=eval(parse(text=paste("list(",paste(paste("dataPop$",stratPop,sep=""),collapse=","),")",sep=""))),FUN=sum, na.rm=TRUE))
		colnames(dataPop) <- c(stratPop,"COUNT")
	   
	# Merge population, incidence data and standard population data
		data <- merge(dataInc,dataPop,by=stratPop)	
		data <- merge(data,standpop,by=c("AGE_GROUP"))	
  
	# Calculations	
		data$exp <- data$CASES * data$REFERENCE_COUNT / data$COUNT
		data$var <- data$CASES * ((data$REFERENCE_COUNT / data$COUNT) ^ 2)  
  
		stratF <- c("ICD10GROUP",strat)
		data <- aggregate(list(data$CASES,data$exp,data$var), by=eval(parse(text=paste("list(",paste(paste("data$",stratF,sep=""),collapse=","),")",sep=""))), FUN=sum, na.rm=TRUE)
		colnames(data) <- c(stratF,"N","asr","var")
		data$se <- sqrt(data$var)
		data$lci <- round(data$asr - 1.96*data$se,2) 
		data$uci <- round(data$asr + 1.96*data$se,2) 

  # Return value  
		return(data)
}
############################################################################################################
## COMPARISON OF RATES OVER 2 PERIODS + AAPC
############################################################################################################

## Catching the arguments sent to the script by CanReg
	Args <- commandArgs(TRUE)
		
## Directory of the script
	initial.options <- commandArgs(trailingOnly = FALSE)
	file.arg.name <- "--file="
	script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
	script.basename <- dirname(script.name)

## Loading dependencies
	source(paste(sep="/", script.basename, "checkArgs.R")) # Apparently this returns the arguments
	source(paste(sep="/", script.basename, "Funct_rates.R"))  # Rates calculation functions
	source(paste(sep="/", script.basename, "Funct_misc.R"))  # Misc functions
	
## Filename & File type
	out <- checkArgs(Args, "-out")
	# This is in case the filename already contains .pdf, .svg or .png
	if(substr(out,nchar(out)-3,nchar(out)) %in% c(".csv",".pdf",".txt")){out <- substr(out,1,nchar(out)-4)}
	fileType <- checkArgs(Args, "-ft")
	
## Is the output file a table or a picture		
	if(fileType %in% c("csv")){
		plotTables <- TRUE
	}else{
		plotTables <- FALSE
	}	
	
## Getting and Formatting INCIDENCE data
	fileInc <- checkArgs(Args, "-inc")
	dataInc <- read.table(fileInc, header=TRUE)
		
## Getting POPULATION data
	filePop <- checkArgs(Args, "-pop")
	dataPop <- read.table(filePop, header=TRUE)	
	
## Getting age group labels
	agegrs <- unique(dataPop[,c("AGE_GROUP","AGE_GROUP_LABEL")])
  standpop <- GetStandPop(dataPop)
	
## Adding data for All Sites
	dataAll <- dataInc[which(dataInc$ICD10GROUP!="C44"),]			
	dataAll$ICD10GROUP <- "ALLbC44"
	dataAll$ICD10GROUPLABEL <- "11 All sites but C44"
	dataAll <- aggregate(dataAll$CASES, by=list(dataAll$YEAR,dataAll$ICD10GROUP,dataAll$ICD10GROUPLABEL,dataAll$SEX,dataAll$AGE_GROUP, dataAll$MORPHOLOGY ,dataAll$BEHAVIOUR,dataAll$BASIS), FUN=sum)
	colnames(dataAll) <- c("YEAR", "ICD10GROUP","ICD10GROUPLABEL" ,"SEX", "AGE_GROUP","MORPHOLOGY","BEHAVIOUR" ,"BASIS","CASES")
	dataInc <- rbind(dataInc,dataAll)	

## Restricting to age groups selected
	groups <- checkArgs(Args, "-agegroup")
	groups <- strsplit(groups,"-")[[1]]
	dataInc <- dataInc[which(dataInc$AGE_GROUP>=as.integer(groups[1]) & dataInc$AGE_GROUP<=as.integer(groups[2])),]	
	
## Calculating ASR for the two periods
	period <- as.integer(checkArgs(Args, "-period"))
	minYr <- min(dataInc$YEAR)
	maxYr <- max(dataInc$YEAR)
		
	dataP1 <- CalcASR(dataInc[which(dataInc$YEAR<=minYr+period-1),], dataPop[which(dataPop$YEAR<=minYr+period-1),], standpop)	
	dataP2 <- CalcASR(dataInc[which(dataInc$YEAR>=maxYr-period+1),], dataPop[which(dataPop$YEAR>=maxYr-period+1),], standpop)	
	
## Rounding results
	dataP1$asr <- format( round(dataP1$asr,2), format='f', digits=2)
	dataP2$asr <- format( round(dataP2$asr,2), format='f', digits=2)
	dataP1$se <- format( round(dataP1$se,2), format='f', digits=2)
	dataP2$se <- format( round(dataP2$se,2), format='f', digits=2)
	
## labels
	malesP1 <- dataP1[dataP1$SEX==1,]
	malesP2 <- dataP2[dataP2$SEX==1,]
	mlabs <- GetSiteLabels(dataInc,1)
	dataM <- merge(malesP1,malesP2,by=c("ICD10GROUP","SEX"), sort=F)
	dataM <- merge(dataM,mlabs,by=c("ICD10GROUP"), sort=F)
	dataM$P1 <- paste(minYr,"-",minYr+period-1)
	dataM$P2 <- paste(maxYr-period+1,"-",maxYr)
	colnames(dataM) <- c("ICD10GROUP","SEX","PERIOD1_N","PERIOD1_ASR","PERIOD1_VAR","PERIOD1_SE","PERIOD1_LCI","PERIOD1_UCI","PERIOD2_N","PERIOD2_ASR","PERIOD2_VAR","PERIOD2_SE","PERIOD2_LCI","PERIOD2_UCI","ICD10GROUPLABEL","P1_YEARS","P2_YEARS")
		
	femalesP1 <- dataP1[dataP1$SEX==2,]
	femalesP2 <- dataP2[dataP2$SEX==2,]
	flabs <- GetSiteLabels(dataInc,2)
	dataF <- merge(femalesP1,femalesP2,by=c("ICD10GROUP","SEX"), sort=F)
	dataF <- merge(dataF,flabs,by=c("ICD10GROUP"), sort=F)
	dataF$P1 <- paste(minYr,"-",minYr+period-1)
	dataF$P2 <- paste(maxYr-period+1,"-",maxYr)
	colnames(dataF) <- c("ICD10GROUP","SEX","PERIOD1_N","PERIOD1_ASR","PERIOD1_VAR","PERIOD1_SE","PERIOD1_LCI","PERIOD1_UCI","PERIOD2_N","PERIOD2_ASR","PERIOD2_VAR","PERIOD2_SE","PERIOD2_LCI","PERIOD2_UCI","ICD10GROUPLABEL","P1_YEARS","P2_YEARS")

# SRR	
	dataM$SRR <- CalcSRR(dataM$PERIOD2_ASR,dataM$PERIOD2_VAR,dataM$PERIOD1_ASR,dataM$PERIOD1_VAR)
	dataF$SRR <- CalcSRR(dataF$PERIOD2_ASR,dataF$PERIOD2_VAR,dataF$PERIOD1_ASR,dataF$PERIOD1_VAR)
	
	dataM <- dataM[,c("ICD10GROUP","ICD10GROUPLABEL","P1_YEARS","PERIOD1_N","PERIOD1_ASR","P2_YEARS","PERIOD2_N","PERIOD2_ASR","SRR")]
	dataF <- dataF[,c("ICD10GROUP","ICD10GROUPLABEL","P1_YEARS","PERIOD1_N","PERIOD1_ASR","P2_YEARS","PERIOD2_N","PERIOD2_ASR","SRR")]
	
		
	
	## If the file type is a figure (not used here)
	if(plotTables==FALSE){
		
		# filename for output  
	    filename <- paste(out,".", fileType, sep = "" ) 
        
		# Finalizing and opening graph
        cat(paste("Nothing happens",sep=""))
  
	
	}else{
	
	## If a table, we create a CSV table file	
	
		filename1 <- paste(out,"-Males.", fileType, sep = "" )
		write.table(dataM, filename1, sep = ",", row.names = F) 
		
		filename2 <- paste(out,"-Females.", fileType, sep = "" )
		write.table(dataF, filename2, sep = ",", row.names = F) 
		
		cat(paste("-outFile",filename1,sep=":"))
		cat("\n")
		cat(paste("-outFile",filename2,sep=":"))		
		
	}



	
	
	
	
	
	
	
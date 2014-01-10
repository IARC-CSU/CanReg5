############################################################################################################
## DATA QUALITY INDICATORS
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
	standpop <- unique(dataPop[,c("AGE_GROUP","REFERENCE_COUNT")])
	standpop$REFERENCE_COUNT <- standpop$REFERENCE_COUNT*100
	
## Calculating ASR
	data <- CalcASR(dataInc, dataPop, standpop)
	
##	Getting basis of diagnosis distribution
	dataB <- GetBasisDist(dataInc)
	
## Merging data frames
	data <- merge(data,dataB,by=c("ICD10GROUP","SEX"))	

## Calculating percentages
	data$DCO <- round(data$"0" * 100 / data$N,2)
	data$CLIN <- round(data$"1" * 100 / data$N,2)
	data$MV <- round(data$"7" * 100 / data$N,2)
	data$UNK <- round(data$"9" * 100 / data$N,2)
	
## labels
	males <- data[data$SEX==1,]
	mlabs <- GetSiteLabels(dataInc,1)
	dataM <- merge(males,mlabs,by=c("ICD10GROUP"))
	
	females <- data[data$SEX==2,]
	flabs <- GetSiteLabels(dataInc,2)
	dataF <- merge(females,flabs,by=c("ICD10GROUP"))

	
	
	
## If the file type is a figure
	if(plotTables==FALSE){
		
		
		
		
		
		
	

			
						
	}else{
	
		# If not a table, we create a CSV table file	
		filename1 <- paste(out,"-Males.", fileType, sep = "" )
		write.table(dataM, filename1, sep = ",", row.names = F) 
		
		filename2 <- paste(out,"-Females.", fileType, sep = "" )
		write.table(dataF, filename2, sep = ",", row.names = F) 
		
		cat(paste("-outFile",filename1,sep=":"))
		cat("\n")
		cat(paste("-outFile",filename2,sep=":"))		
		
	}

		#write.table(flabs, "ANTONI.csv", sep = ",", row.names = F) 


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	